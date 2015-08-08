package autojson.type

import autojson.core.Either
import autojson.getClassNameFromFieldName
import autojson.node.*
import autojson.type.Namespace
import autojson.type.Type
import java.util.*

/**
 * Created by omochi on 15/07/31.
 */

class TypeResolver {
    constructor() {
    }

    fun decodeFile(
            file: SourceFileNode
    ): Either<Exception, Namespace> {
        val namespace = Namespace(null, TypeName("root"))

        decodeTypesEntryNames(file.types, namespace).toRight {
            return Either.left(Exception("decode types entry names failed", it))
        }

        for (name in namespace.getEntryNames()) {
            val decodeRet = getDecodedNamespaceEntry(namespace, name).toRight {
                return Either.left(Exception("get decoded namespace entry failed: " +
                        "name=$name, namespace=$namespace", it))
            }
        }

        return Either.right(namespace)
    }

    fun decodeTypesEntryNames(
            types: Map<String, Node>,
            destNamespace: Namespace
    ): Either<Exception, Unit> {
        for ((name, typeNode) in types) {
            val type = NodeType(typeNode)
            val typeName = TypeName(name)
            updateNamespaceEntry(destNamespace, typeName, type).toRight {
                return Either.left(Exception("update namespace entry failed: " +
                        "namespace=$destNamespace, name=$typeName", it))
            }
        }
        return Either.right(Unit)
    }

    fun getDecodedNamespaceEntry(
            sourceNamespace: Namespace,
            name: TypeName
    ): Either<Exception, TupleNamespaceType> {
        val resolveRet = sourceNamespace.resolve(name) ?:
                return Either.left(Exception("name not found: " +
                        "name=$name, sourceNamespace=$sourceNamespace"))
        var (entryNamespace, entryType) = resolveRet

        when (entryType) {
            is NodeType -> {
                val nodeType = entryType
                val decodedType = decodeNamespaceEntry(entryNamespace, name, nodeType.node).toRight {
                    return Either.left(Exception("decode type failed: " +
                            "name=$name, sourceNamespace=$sourceNamespace, " +
                            "entryNamespace=$entryNamespace", it))
                }
                entryType = decodedType
            }
        }

        return Either.right(TupleNamespaceType(entryNamespace, entryType))
    }

    fun updateNamespaceEntry(
            namespace: Namespace,
            name: TypeName,
            entry: Type
    ): Either<Exception, Unit> {
        val oldType = namespace.getEntry(name)
        when (oldType) {
            null -> {}
            is NodeType -> {}
            else -> {
                return Either.left(Exception("entry conflicted: " +
                        "oldType=${oldType.javaClass}, newType=${entry.javaClass}"))
            }
        }

        print("updateNamespaceEntry: namespace=$namespace\n")
        print("  newType=${entry.toDebugString()}")

        namespace.setEntry(name, entry)
        return Either.right(Unit)
    }

    fun decodeNamespaceEntry(
            namespace: Namespace,
            entryName: TypeName,
            node: Node
    ): Either<Exception, Type> {
        val desc = "namespace=$namespace, " +
                "entryName=$entryName, " +
                "node pos=${node.pos}"
        when (node) {
            is RefNode -> {
                val alias = AliasType(
                        RefType(namespace, TypeName(node.name))
                )
                updateNamespaceEntry(namespace, entryName, alias).toRight {
                    return Either.left(Exception("update namespace entry failed: " +
                            "namespace=$namespace, name=$entryName", it))
                }
                return Either.right(alias)
            }
            is ClassDefNode -> {
                val classType = decodeClassDef(
                        entryName,
                        node,
                        namespace
                ).toRight {
                    return Either.left(Exception("decode class def failed: " +
                            "$desc", it))
                }
                if (entryName != classType.name) {
                    return Either.left(Exception("decoded class name is conflicted: " +
                            "decoded=${classType.name}, $desc"))
                }
                return Either.right(classType)
            }
            is ApplyNode -> {
                val appliedType = decodeApply(
                        entryName,
                        node,
                        namespace
                ).toRight {
                    return Either.left(Exception("decode apply failed: " +
                            "$desc", it))
                }
                return Either.right(AliasType(
                        RefType(namespace, appliedType.name)
                ))
            }
            else -> {
                return Either.left(Exception("invalid node type: " +
                        "node type=${node.javaClass}, $desc"))
            }
        }
    }

    fun decodeClassDef (
            hintName: TypeName,
            classDefNode: ClassDefNode,
            parentNamespace: Namespace
    ): Either<Exception, ClassType> {
        val className = classDefNode.name?.let { TypeName(it) } ?: hintName

        val params = LinkedHashMap<String, Type>()
        for (name in classDefNode.let) {
            val paramType = PolyType()
            params[name] = paramType
        }

        val classNamespace = Namespace(parentNamespace, className)

        val fields = LinkedHashMap<String, Type>()
        for ((name, field) in classDefNode.fields) {
            val fieldType = decodeClassField(
                    name,
                    field,
                    classNamespace
            ).toRight {
                return Either.left(Exception(
                        "decode field type failed: " +
                                "fieldName=$name, " +
                                "className=$className, " +
                                "namespace=$parentNamespace", it
                ))
            }
            fields[name] = fieldType
        }

        val classType = ClassType(
                className,
                params,
                classNamespace,
                fields
        )
        updateNamespaceEntry(parentNamespace, className, classType).toRight {
            return Either.left(Exception("update namespace entry failed: " +
                    "namespace=$parentNamespace, name=$className", it))
        }
        return Either.right(classType)
    }

    fun decodeTypeNode(
            node: Node,
            namespace: Namespace,
            classHintName: TypeName
    ): Either<Exception, Type> {
        val desc="node pos=${node.pos}, namespace=$namespace, classHintName=$classHintName"
        when (node) {
            is RefNode -> {
                return Either.right(RefType(namespace, TypeName(node.name)))
            }
            is ClassDefNode -> {
                val classType = decodeClassDef(
                        classHintName,
                        node,
                        namespace
                ).toRight {
                    return Either.left(Exception("decode class def failed: " +
                            "$desc", it))
                }
                return Either.right(classType)
            }
            is ApplyNode -> {
                val appliedType = decodeApply(classHintName, node, namespace).toRight {
                    return Either.left(Exception("decode apply failed: " +
                            "$desc", it))
                }
                return Either.right(appliedType)
            }
            else -> {
                return Either.left(Exception("invalid node type: " +
                        "type=${node.javaClass}, $desc"))
            }
        }
    }

    fun decodeClassField(
            fieldName: String,
            fieldNode: Node,
            classNamespace: Namespace
    ): Either<Exception, Type> {
        val hintName = TypeName(getClassNameFromFieldName(fieldName)!!)

        val decoded = decodeTypeNode(fieldNode, classNamespace,
                hintName).toRight {
            return Either.left(Exception("decode type node failed: " +
                    "fieldName=$fieldName, " +
                    "fieldNode pos=${fieldNode.pos}, " +
                    "classNamespace=$classNamespace", it))
        }
        return Either.right(decoded)
    }

    private fun decodeApply(
            classHintName: TypeName,
            applyNode: ApplyNode,
            parentNamespace: Namespace
    ): Either<Exception, ClassType> {
        var desc="classHintName=$classHintName, " +
                "applyNode pos=${applyNode.pos}, " +
                "parentNamespace=$parentNamespace"

        val targetTuple = decodeApplyTarget(
                classHintName,
                applyNode.target, parentNamespace
        ).toRight {
            return Either.left(Exception("decode apply target failed: " +
                    "$desc", it))
        }
        val targetNamespace = targetTuple.namespace
        val targetType = targetTuple.type

        val params = LinkedHashMap<String, Type>()
        for ((paramName, paramNode) in applyNode.params) {
            val param = decodeApplyParam(
                    classHintName,
                    paramName,
                    paramNode,
                    parentNamespace
            ).toRight {
                return Either.left(it)
            }
            println("decodeApply classHintName=$classHintName, name=$paramName, param=${param.toDebugString()}")
            params[paramName] = param
        }

        when (targetType) {
            is ClassType -> {
                val targetClass = targetType

                desc = "targetClass=${targetClass.name}," +
                        "params=$params, " +
                        "$desc"

                val appliedClassName = getAppliedClassName(targetClass, params).toRight {
                    return Either.left(Exception("get applied class name failed: " +
                            "$desc", it))
                }
                val definedApplied = targetNamespace.getEntry(appliedClassName)
                if (definedApplied != null) {
                    if (definedApplied !is ClassType) {
                        throw Exception("defined applied class is not class type: " +
                                "${definedApplied.javaClass}, $desc")
                    }
                    return Either.right(definedApplied)
                }

                val applied = evalApply(targetClass, params, parentNamespace).toRight {
                    return Either.left(Exception("eval apply failed: " +
                            "target=${targetClass.name}, " +
                            "params=${params}, $desc", it))
                }

                updateNamespaceEntry(targetNamespace, applied.name, applied).toRight {
                    return Either.left(Exception("update namespace entry failed: " +
                            "namespace=$targetNamespace, name=${applied.name}, " +
                            "appliedClassName=$appliedClassName", it))
                }

                return Either.right(applied)
            }
            else -> {
                return Either.left(Exception("invalid target type: " +
                        "type=${targetType.javaClass}, $desc"))
            }
        }
    }

    //  TODO: classHintName: ApplyTargetの匿名定義ができる場合に備えて
    fun decodeApplyTarget(
            classHintName: TypeName,
            node: Node,
            namespace: Namespace
    ): Either<Exception, TupleNamespaceType> {
        when (node) {
            is RefNode -> {
                val firstRefName = TypeName(node.name)
                val firstDereferRet = getDecodedNamespaceEntry(namespace, firstRefName).toRight {
                    return Either.left(Exception("get decoded namespace entry failed: " +
                            "namespace=$namespace, name=$firstRefName", it))
                }

                val dereferRet = dereferApplyTarget(
                        firstDereferRet.type,
                        firstDereferRet.namespace
                ).toRight { return Either.left(it) }
                return Either.right(dereferRet)
            }
            else -> {
                return Either.left(Exception("invalid node type: " +
                        "namespace=$namespace, node type=${node.javaClass}, node pos=${node.pos}"))
            }
        }
    }

    fun dereferApplyTarget(
            sourceType: Type,
            sourceNamespace: Namespace
    ): Either<Exception, TupleNamespaceType> {
        var currentType = sourceType
        var currentNamespace = sourceNamespace
        loop@ while (true) {
            when (currentType) {
                is ClassType -> {
                    break@loop
                }
                is AliasType -> {
                    val alias = currentType
                    val decodeRet = getDecodedNamespaceEntry(alias.ref.namespace, alias.ref.name).toRight {
                        return Either.left(Exception("get decoded namespace entry failed: " +
                                "namespace=${alias.ref.namespace}, name=${alias.ref.name}"))
                    }
                    currentType = decodeRet.type
                    currentNamespace = decodeRet.namespace
                }
                else -> {
                    return Either.left(Exception("invalid type: " +
                            "type=${currentType.javaClass}, " +
                            "sourceNamespace=$sourceNamespace, " +
                            "currentNamespace=$currentNamespace"))
                }
            }
        }
        return Either.right(TupleNamespaceType(currentNamespace, currentType))
    }

    fun decodeApplyParam(
            targetName: TypeName,
            paramName: String,
            paramNode: Node,
            namespace: Namespace
    ): Either<Exception, Type> {
        val hintName = TypeName(targetName.name + getClassNameFromFieldName(paramName)!!)

        val decoded = decodeTypeNode(paramNode, namespace,
                hintName).toRight {
            return Either.left(Exception("decode type node failed: " +
                    "targetName=$targetName, " +
                    "paramName=$paramName," +
                    "paramNode pos=${paramNode.pos}, " +
                    "namespace=$namespace"))
        }
        return Either.right(decoded)
    }

    fun getApplyParamTypeName(
            type: Type
    ): Either<Exception, TypeName> {
        when (type) {
            is ClassType -> { return Either.right(type.name) }
            is RefType -> { return Either.right(type.name) }
            else -> {
                return Either.left(Exception("invalid type: type=${type.javaClass}"))
            }
        }
    }

    fun getAppliedClassName(
            target: ClassType,
            params: Map<String, Type>
    ): Either<Exception, TypeName> {
        val desc="target=${target.name}, params=$params"

        val paramTypeNames = LinkedHashMap<String, TypeName>()
        for (paramName in target.params.keySet()) {
            val param = params[paramName]!!
            val paramTypeName = getApplyParamTypeName(param).toRight {
                return Either.left(Exception(
                        "get apply apram type name failed: " +
                                "paramName=$paramName, $desc", it
                ))
            }
            paramTypeNames[paramName] = paramTypeName
        }

        return Either.right(TypeName(target.name.name, paramTypeNames))
    }

    fun evalApply(
            target: ClassType,
            params: Map<String, Type>,
            namespace: Namespace
    ): Either<Exception, ClassType> {
        var desc="target name=${target.name}, " +
                "params=$params, " +
                "namespace=$namespace"

        for (targetParam in target.params.keySet()) {
            if (targetParam !in params.keySet()) {
                return Either.left(Exception(
                        "target param not specified: " +
                        "target param=$targetParam, $desc"))
            }
        }

        val appliedClassName = getAppliedClassName(target, params).toRight {
            return Either.left(Exception("get applied class name failed: $desc", it))
        }
        desc = "appliedClassName=$appliedClassName, $desc"

        val classNamespace = Namespace(namespace, appliedClassName)

        val appliedFields = LinkedHashMap<String, Type>()
        for ((fieldName, field) in target.fields) {
            val appliedField = evalApplyToField(
                    target,
                    field,
                    params,
                    classNamespace
            ).toRight {
                return Either.left(Exception("eval apply to field failed: " +
                        "fieldName=$fieldName, $desc", it))
            }
            appliedFields[fieldName] = appliedField
        }

        return Either.right(
                ClassType(
                        appliedClassName,
                        emptyMap(),
                        classNamespace,
                        appliedFields
                )
        )
    }

    fun evalApplyToField(
            targetClass: ClassType,
            field: Type,
            params: Map<String, Type>,
            classNamespace: Namespace
    ): Either<Exception, Type> {
        val desc = "field type=${field.javaClass}, params=$params, " +
                "classNamespace=$classNamespace"
        when (field) {
            is RefType -> {
                val refField = field

                println("evalApplyToField: " +
                        "targetClass=${targetClass.name}, " +
                        "targetClassNamespace=${targetClass.namespace}, " +
                        "refField=(${refField.namespace}, ${refField.name}), " +
                        "params=$params")

                if (refField.namespace == targetClass.namespace)  {
                    if (refField.name.params.size() == 0) {
                        val replaced = params[refField.name.name]
                        if (replaced != null) {
                            println("replaced = ${replaced.toDebugString()}")
                            return Either.right(replaced)
                        } else {
                            println("params is null")
                        }
                    }
                } else {
                    println("namespace not equal")
                }
            }
            is ClassType -> {
                val applied = evalApply(
                        field,
                        params,
                        classNamespace
                ).toRight {
                    return Either.left(Exception(
                            "eval apply failed: " +
                                    "field class=${field.name}, $desc", it
                    ))
                }

                updateNamespaceEntry(classNamespace, applied.name, applied).toRight {
                    return Either.left(Exception("update namespace entry failed: " +
                            "namespace=$classNamespace, name=${applied.name}", it))
                }

                return Either.right(applied)
            }
            else -> {}
        }
        return Either.right(field)
    }

}