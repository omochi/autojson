package autojson.type

import autojson.DebugWriter
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
        val namespace = Namespace(null, TypeName("root", false))

        for (name in listOf("Null", "Bool", "Int", "Long", "Float", "Double", "String")) {
            addBuiltInScalarType(namespace, name).toRight {
                return Either.left(it)
            }
        }

        decodeTypesEntryNames(file.types, namespace).toRight {
            return Either.left(Exception("decode types entry names failed", it))
        }

        for (name in ArrayList(namespace.table.keySet())) {
            val decodeRet = getDecodedNamespaceEntry(namespace, name).toRight {
                return Either.left(Exception("get decoded namespace entry failed: " +
                        "name=$name, namespace=$namespace", it))
            }
        }

        return Either.right(namespace)
    }

    fun addBuiltInScalarType(namespace: Namespace, name: String): Either<Exception, Unit> {
        val typeName = TypeName(name, false)
        updateNamespaceEntry(namespace, typeName, ClassType(
                typeName, true, LinkedHashMap(), Namespace(namespace, typeName), emptyMap()
        )).toRight {
            return Either.left(Exception("add built in scalar type failed: " +
                    "namespace=$namespace, name=$name", it))
        }
        return Either.right(Unit)
    }

    fun decodeTypesEntryNames(
            types: Map<String, Node>,
            destNamespace: Namespace
    ): Either<Exception, Unit> {
        for ((name, typeNode) in types) {
            val type = NodeType(typeNode)
            val typeName = TypeName(name, false)
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
        val oldType = namespace.table[name]
        when (oldType) {
            null -> {}
            is NodeType -> {}
            else -> {
                return Either.left(Exception("entry conflicted: " +
                        "oldType=${oldType.javaClass}, newType=${entry.javaClass}"))
            }
        }

        val writer = DebugWriter()
        writer.writeLine("updateNamespaceEntry")
        writer.push()
        writer.writeLine("namespace=$namespace")
        writer.writeLine("name=$name")
        writer.writeLine("newType=", false)
        writer.writeObject(entry)
        print(writer.getString())

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
                val ref = decodeRef(namespace, node.typeName).toRight {
                    return Either.left(Exception("decode ref failed: " +
                            "name=${node.name}, $desc", it))
                }
                updateNamespaceEntry(namespace, entryName, ref).toRight {
                    return Either.left(Exception("update namespace entry failed: " +
                            "namespace=$namespace, name=$entryName, $desc", it))
                }
                return Either.right(ref)
            }
            is ClassDefNode -> {
                val classType = decodeClassDef(
                        entryName.name,
                        false,
                        node,
                        namespace
                ).toRight {
                    return Either.left(Exception("decode class def failed: " +
                            "$desc", it))
                }
                if (classType is ClassType) {
                    if (entryName != classType.name) {
                        return Either.left(Exception("decoded class name is conflicted: " +
                                "decoded=${classType.name}, $desc"))
                    }
                }
                return Either.right(classType)
            }
            is ApplyNode -> {
                val applied = decodeApply(
                        entryName.name,
                        node,
                        namespace
                ).toRight {
                    return Either.left(Exception("decode apply failed: " +
                            "$desc", it))
                }
                val type = applied.type

                val ref = RefType(applied.namespace, type.name)
                updateNamespaceEntry(namespace, entryName, ref).toRight {
                    return Either.left(Exception("update namespace entry failed: " +
                            "name=$entryName, $desc", it))
                }
                return Either.right(ref)
            }
            else -> {
                return Either.left(Exception("invalid node type: " +
                        "node type=${node.javaClass}, $desc"))
            }
        }
    }

    fun decodeRef (
            namespace: Namespace,
            name: TypeName
    ): Either<Exception, RefType> {
        val resolveRet = namespace.resolve(name) ?:
                return Either.left(Exception("resolve failed: " +
                        "namespace=$namespace, name=${name}"))
        return Either.right(RefType(resolveRet.namespace, name))
    }

    fun decodeClassDef (
            hintName: String,
            anonymous: Boolean,
            classDefNode: ClassDefNode,
            parentNamespace: Namespace
    ): Either<Exception, ClassType> {
        val className = TypeName(classDefNode.name ?: hintName, anonymous)

        val desc = "className=$className, " +
                "anonymous=$anonymous, " +
                "classDefNode pos=${classDefNode.pos}, " +
                "parentNamespace=$parentNamespace"

        val classNamespace = Namespace(parentNamespace, className)

        val params = LinkedHashMap<String, Type>()
        for (name in classDefNode.let) {
            val paramType = PolyType()
            params[name] = paramType

            updateNamespaceEntry(classNamespace, TypeName(name, false), paramType).toRight {
                return Either.left(Exception(
                        "update namespace entry failed: " +
                                "classNamespace=$classNamespace, " +
                                "name=$name, $desc", it
                ))
            }
        }

        val fields = LinkedHashMap<String, Type>()
        for ((name, field) in classDefNode.fields) {
            val fieldType = decodeClassField(
                    name,
                    field,
                    classNamespace
            ).toRight {
                return Either.left(Exception(
                        "decode field type failed: " +
                                "fieldName=$name, $desc", it
                ))
            }
            fields[name] = fieldType
        }

        val classType = ClassType(
                className,
                false,
                params,
                classNamespace,
                fields
        )

        updateNamespaceEntry(parentNamespace, className, classType).toRight {
            return Either.left(Exception("update namespace entry failed: " +
                    "namespace=$parentNamespace, name=$className, $desc", it))
        }

        return Either.right(classType)
    }

    fun decodeTypeNode(
            node: Node,
            namespace: Namespace,
            classHintName: String,
            anonymous: Boolean
    ): Either<Exception, TupleNamespaceType> {
        val desc="node pos=${node.pos}, namespace=$namespace, classHintName=$classHintName"
        when (node) {
            is RefNode -> {
                val ref = decodeRef(namespace, node.typeName).toRight {
                    return Either.left(Exception("decode ref failed: " +
                            "name=${node.name}, $desc", it))
                }
                return Either.right(TupleNamespaceType(
                        namespace, ref))
            }
            is ClassDefNode -> {
                val classType = decodeClassDef(
                        classHintName,
                        anonymous,
                        node,
                        namespace
                ).toRight {
                    return Either.left(Exception("decode class def failed: " +
                            "$desc", it))
                }

                return Either.right(TupleNamespaceType(
                        namespace, classType
                ))
            }
            is ApplyNode -> {
                val applied = decodeApply(classHintName, node, namespace).toRight {
                    return Either.left(Exception("decode apply failed: " +
                            "$desc", it))
                }
                return Either.right(TupleNamespaceType(
                        applied.namespace,
                        applied.type
                ))
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
        val hintName = getClassNameFromFieldName(fieldName)!!

        val decoded = decodeTypeNode(
                fieldNode,
                classNamespace,
                hintName,
                true
        ).toRight {
            return Either.left(Exception("decode type node failed: " +
                    "fieldName=$fieldName, " +
                    "fieldNode pos=${fieldNode.pos}, " +
                    "classNamespace=$classNamespace", it))
        }

        val decodedType = decoded.type
        if (decodedType is ClassType) {
            return Either.right(RefType(
                    decoded.namespace,
                    decodedType.name
            ))
        }

        return Either.right(decoded.type)
    }

    private fun decodeApply(
            classHintName: String,
            applyNode: ApplyNode,
            parentNamespace: Namespace
    ): Either<Exception, TupleNamespaceClassType> {
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
        if (targetType !is ClassType) {
            return Either.left(Exception("invalid target type: " +
                    "type=${targetType.javaClass}, $desc"))
        }
        val targetClass = targetType
        desc = "targetClass=${targetClass.name}, " +
                "$desc"

        val targetParamNames = ArrayList(targetClass.params.keySet())
        val params = ArrayList<Type>()
        for ((paramIndex, paramNode) in applyNode.params.withIndex()) {
            val paramName = targetParamNames[paramIndex]
            val param = decodeApplyParam(
                    classHintName,
                    paramName,
                    paramNode,
                    parentNamespace
            ).toRight {
                return Either.left(it)
            }
            params.add(param)
        }

        val appliedClassName = getAppliedClassName(targetClass, params).toRight {
            return Either.left(Exception("get applied class name failed: " +
                    "$desc", it))
        }
        val definedApplied = targetNamespace.table[appliedClassName]
        if (definedApplied != null) {
            if (definedApplied !is ClassType) {
                throw Exception("defined applied class is not class type: " +
                        "${definedApplied.javaClass}, $desc")
            }
            return Either.right(TupleNamespaceClassType(targetNamespace, definedApplied))
        }

        val applied = evalApply(targetClass, params).toRight {
            return Either.left(Exception("eval apply failed: " +
                    "target=${targetClass.name}, " +
                    "params=${params}, $desc", it))
        }

        updateNamespaceEntry(targetNamespace, applied.name, applied).toRight {
            return Either.left(Exception("update namespace entry failed: " +
                    "namespace=$targetNamespace, " +
                    "name=${applied.name}, $desc", it))
        }

        return Either.right(TupleNamespaceClassType(targetNamespace, applied))
    }

    fun decodeApplyTarget(
            classHintName: String,
            node: Node,
            namespace: Namespace
    ): Either<Exception, TupleNamespaceClassType> {
        val desc = "classHintName=$classHintName, " +
                "node pos=${node.pos}, " +
                "namespace=$namespace"
        val decodeRet = decodeTypeNode(
                node,
                namespace,
                classHintName,
                true
        ).toRight {
            return Either.left(Exception("decode type node failed: " +
                    "$desc", it))
        }

        val dereferRet = dereferApplyTarget(decodeRet.type, decodeRet.namespace).toRight {
            return Either.left(Exception("derefer apply target failed: " +
                    "target=${decodeRet.type.javaClass}, sourceNamespace=${decodeRet.namespace}, " +
                    "$desc", it))
        }

        return Either.right(dereferRet)
    }

    fun dereferApplyTarget(
            sourceType: Type,
            sourceNamespace: Namespace
    ): Either<Exception, TupleNamespaceClassType> {
        var currentType = sourceType
        var currentNamespace = sourceNamespace
        loop@ while (true) {
            when (currentType) {
                is ClassType -> {
                    return Either.right(TupleNamespaceClassType(
                            currentNamespace,
                            currentType
                    ))
                }
                is RefType -> {
                    val ref = currentType
                    val decodeRet = getDecodedNamespaceEntry(
                            ref.namespace, ref.name
                    ).toRight {
                        return Either.left(Exception("get decoded namespace entry failed: " +
                                "namespace=${ref.namespace}, name=${ref.name}"))
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
    }

    fun decodeApplyParam(
            targetName: String,
            paramName: String,
            paramNode: Node,
            namespace: Namespace
    ): Either<Exception, Type> {
        val hintName = targetName + getClassNameFromFieldName(paramName)!!

        val decoded = decodeTypeNode(
                paramNode,
                namespace,
                hintName,
                true
        ).toRight {
            return Either.left(Exception("decode type node failed: " +
                    "targetName=$targetName, " +
                    "paramName=$paramName, " +
                    "paramNode pos=${paramNode.pos}, " +
                    "namespace=$namespace", it))
        }

        val decodedType = decoded.type
        if (decodedType is ClassType) {
            return Either.right(RefType(
                    decoded.namespace,
                    decodedType.name
            ))
        }

        return Either.right(decoded.type)
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
            params: List<Type>
    ): Either<Exception, TypeName> {
        val desc="target=${target.name}, params=$params"

        val paramTypeNames = ArrayList<TypeName>()
        for ((index, param) in params.withIndex()) {
            val paramTypeName = getApplyParamTypeName(param).toRight {
                return Either.left(Exception(
                        "get apply param type name failed: " +
                                "index=$index, $desc", it
                ))
            }
            paramTypeNames.add(paramTypeName)
        }

        return Either.right(TypeName(target.name.name, target.name.anonymous, paramTypeNames))
    }

    fun evalApply(
            target: ClassType,
            params: List<Type>
    ): Either<Exception, ClassType> {
        var desc="target name=${target.name}, " +
                "params=$params"

        if (target.params.size() != params.size()) {
            return Either.left(Exception(
                    "target params does not match applying params: " +
                            "target=${target.params}, applying=${params}"
            ))
        }

        val paramMap = LinkedHashMap<String, Type>()
        for ((index, targetParamName) in target.params.keySet().withIndex()) {
            paramMap[targetParamName] = params[index]
        }

        val targetParentNamespace = target.namespace.parent!!

        val appliedClassName = getAppliedClassName(target, params).toRight {
            return Either.left(Exception("get applied class name failed: $desc", it))
        }
        desc = "appliedClassName=$appliedClassName, $desc"

        val subst = NameSubstTable()
        subst.table[TupleNamespaceTypeName(targetParentNamespace, target.name)] =
                TupleNamespaceTypeName(targetParentNamespace, appliedClassName)

        val appliedClass = target.applySubsts(subst)
        //  外向きの型パラを閉じる
        appliedClass.params.clear()
        //  内部の型パラの右辺値を変更
        for ((paramName, param) in paramMap) {
            appliedClass.namespace.setEntry(
                    TypeName(paramName, false),
                    param
            )
        }

        return Either.right(appliedClass)
    }


}