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
        val namespace = Namespace(null, "root")

        decodeNamesFromTypes(file.types, namespace).toRight {
            return Either.left(Exception("decode names failed", it))
        }

        for (name in ArrayList(namespace.table.keySet())) {
            val type = namespace.table[name]!!
            if (type is NodeType) {
                val decodedType = decodeType(
                        name, "", type.node, namespace
                ).toRight {
                    return Either.left(Exception("decode type failed: name=$name", it))
                }
                namespace.setEntry(name, decodedType)
            }
        }

        return Either.right(namespace)
    }

    fun decodeNamesFromTypes(
            types: Map<String, Node>,
            namespace: Namespace
    ): Either<Exception, Namespace> {
        for ((name, typeNode) in types) {
            val type = NodeType(typeNode)
            namespace.addEntry(name, type).toRight {
                return Either.left(it)
            }
        }
        return Either.right(namespace)
    }

    fun decodeType(
            specifiedName: String?,
            hintName: String,
            typeNode: Node,
            namespace: Namespace
    ): Either<Exception, Type> {
        when (typeNode) {
            is ClassDefNode -> {
                return decodeClassDef(
                        specifiedName,
                        hintName,
                        typeNode,
                        namespace
                )
            }
            is RefNode -> {
                return Either.right(decodeRef(typeNode, namespace))
            }
            is ApplyNode -> {
                return decodeApply(
                        specifiedName,
                        hintName,
                        typeNode,
                        namespace
                )
            }
            else -> {
                throw Error("unimplemented: ${typeNode.javaClass}")
            }
        }
    }

    fun decodeClassDef (
            specifiedName: String?,
            hintName: String,
            classDefNode: ClassDefNode,
            parentNamespace: Namespace
    ): Either<Exception, Type> {
        val selfName = classDefNode.name
        if (specifiedName != null && selfName != null &&
                specifiedName != selfName
        ) {
            return Either.left(Exception(
                    "class name conflicted: " +
                            "specifiedName=$specifiedName vs selfName=$selfName"
            ))
        }
        val className = specifiedName ?: selfName ?: hintName

        val params = LinkedHashMap<String, Type>()
        for (name in classDefNode.let) {
            val paramType = PolyType()
            params[name] = paramType
        }

        val namespace = Namespace(parentNamespace, className)

        val fields = HashMap<String, Type>()
        for ((name, field) in classDefNode.fields) {
            val innerClassHintName = getClassNameFromFieldName(name)!!
            val fieldType = decodeType(
                    null,
                    innerClassHintName,
                    field,
                    namespace
            ).toRight {
                return Either.left(Exception(
                        "decode field type failed: name=$name", it
                ))
            }
            fields[name] = fieldType
        }

        return Either.right(ClassType(
                className,
                params,
                namespace,
                fields
        ))
    }

    private fun decodeRef(
            refNode: RefNode,
            namespace: Namespace
    ): RefType {
        val ref = RefType(
                namespace,
                refNode.name
        )
        return ref
    }

    private fun decodeApply(
            specifiedName: String?,
            hintName: String,
            applyNode: ApplyNode,
            parentNamespace: Namespace
    ): Either<Exception, Type> {
        val selfName = applyNode.name
        if (specifiedName != null && selfName != null &&
                specifiedName != selfName
        ) {
            return Either.left(Exception(
                    "class name conflicted: " +
                            "specifiedName=$specifiedName vs selfName=$selfName"
            ))
        }
        val className = specifiedName ?: selfName ?: hintName

        val params = LinkedHashMap<String, Type>()
        for (name in applyNode.let) {
            val paramType = PolyType()
            params[name] = paramType
        }

        val target = decodeDerefered(applyNode.target, parentNamespace).toRight {
            return Either.left(Exception("apply target decode failed: className=$className", it))
        }

        when (target) {
            is ClassType -> {
                val applyParams = LinkedHashMap<String, Type>()
                for ((name, node) in applyNode.params) {
                    val param = decodeType(
                            null, "", node, parentNamespace
                    ).toRight {
                        return Either.left(it)
                    }
                    applyParams[name] = param
                }

                val applied = evalApply(className, target, applyParams, parentNamespace).toRight {
                    return Either.left(Exception("eval apply failed: " +
                            "className=$className, " +
                            "target=$target, params=$applyParams", it))
                }

                return Either.right(applied)
            }
            else -> {
                throw Error("not implemented target type : ${target.javaClass}")
            }
        }
    }

    fun decodeDerefered(
            node: Node,
            namespace: Namespace
    ): Either<Exception, Type> {
        val type = decodeType(
                null, "", node, namespace
        ).toRight {
            return Either.left(it)
        }

        val derefered = dereferRef(type, namespace).toRight {
            return Either.left(it)
        }

        return Either.right(derefered)
    }

    fun dereferRef(
            ref: Type,
            namespace: Namespace
    ): Either<Exception, Type> {
        var currentNamespace = namespace
        var currentRef = ref
        while (currentRef is RefType) {
            val refName = currentRef.name
            val resolveRet = namespace.resolve(refName)
            if (resolveRet == null) {
                return Either.left(Exception("not found: name=${refName}, namespace=$namespace"))
            }
            currentNamespace = resolveRet.namespace
            currentRef = resolveRet.type

            if (currentRef is NodeType) {
                currentRef = decodeType(refName, "", currentRef.node, currentNamespace).toRight {
                    return Either.left(Exception("decode failed", it))
                }
                currentNamespace.setEntry(refName, currentRef)
            }
        }
        return Either.right(currentRef)
    }

    fun evalApply(
            name: String,
            target: Type,
            params: Map<String, Type>,
            namespace: Namespace
    ): Either<Exception, Type> {
        when (target) {
            is ClassType -> {
                return evalApplyToClass(name, target, params, namespace)
            }
            else -> { return Either.left(Exception("invalid target type: ${target.javaClass}"))}
        }
    }

    fun evalApplyToClass(
            className: String,
            target: ClassType,
            params: Map<String, Type>,
            namespace: Namespace
    ): Either<Exception, Type> {

        for (targetParam in target.params.keySet()) {
            if (targetParam !in params.keySet()) {
                return Either.left(Exception(
                        "target param not specified: " +
                        "target param=$targetParam, " +
                        "apply params=${params.keySet()}"))
            }
        }

        val classNamespace = Namespace(namespace, className)

        val fields = LinkedHashMap<String, Type>()
        for ((name, srcField) in target.fields) {
            var destField = srcField
            when (srcField) {
                is RefType -> {
                    val replaced = params[srcField.name]
                    if (replaced != null) {
                        destField = replaced
                    }
                }
                is ClassType -> {
                    if (srcField.params.size() > 0) {
                        return Either.left(Exception(
                                "type params is invalid here: " +
                                        "field name=$name, field class=${srcField.name}"))
                    }

                    destField = evalApplyToClass(
                            srcField.name, srcField, params, classNamespace).toRight {
                        return Either.left(Exception(
                                "apply field class failed: " +
                                        "field name=$name, field class=${srcField.name}", it
                        ))
                    }
                }
            }
            fields[name] = destField
        }

        return Either.right(
                ClassType(
                        className,
                        emptyMap(),
                        classNamespace,
                        fields
                )
        )
    }

}