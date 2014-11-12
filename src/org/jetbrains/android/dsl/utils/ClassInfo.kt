/*
 * Copyright 2014 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.jetbrains.android.dsl

import org.objectweb.asm.*
import org.objectweb.asm.tree.ClassNode
import org.objectweb.asm.tree.MethodNode

class NoSignatureException(message: String) : RuntimeException(message)

class MethodNodeWithClass(var clazz: ClassNode, val method: MethodNode) {
	fun toStringCompact() = "${clazz.name}#${method.name}"
}

fun decapitalize(name: String): String {
    return name.substring(0, 1).toLowerCase() + name.substring(1)
}

fun capitalize(name: String): String {
    return name.substring(0, 1).toUpperCase() + name.substring(1)
}

fun stripClassName(name: String): String {
    return name.substring(name.lastIndexOf('.') + 1)
}

fun cleanInternalName(name: String): String {
    return name.replace('/', '.').replace('$', '.')
}

fun ClassNode.cleanName(): String {
    return stripClassName(cleanInternalName(name!!))
}

fun ClassNode.cleanNameDecap(): String {
    return decapitalize(cleanName())
}

fun ClassNode.buldTypeParams(): String {
    return if (signature != null) {
        val wtf = parseGenericMethodSignature(signature!!)
        if (wtf.typeParameters.isEmpty()) {
            return ""
        }
        val t: List<String> = wtf.typeParameters.map {
            it.upperBounds.fold("") {i, bound -> i + "out " + genericTypeToStr(bound) }
        }
        t.joinToString(prefix = "<", postfix = ">")
    } else ""
}

fun ClassNode.cleanInternalName(): String {
    return name!!.replace('/', '.').replace('$', '.') + buldTypeParams()
}

fun ClassNode.toContainerName() = "_${cleanName()}"

fun ClassNode.toContainerInternalName() = "_${cleanInternalName()}"

fun ClassNode.isInner(): Boolean {
    return name!!.contains("$")
}

fun ClassNode.isAbstract(): Boolean {
    return ((access and Opcodes.ACC_ABSTRACT) != 0)
}

fun ClassNode.isGeneric(): Boolean {
    return signature != null
}

fun ClassNode.getConstructors(): List<MethodNode> {
    return (methods: List<MethodNode>).filter { it.isConstructor() }
}

fun ClassNode.isView(classTree: ClassTree, viewBaseClass: String): Boolean {
    return classTree.isSuccessorOf(this, viewBaseClass) || this.name == viewBaseClass
}

fun ClassNode.isViewGroup(classTree: ClassTree, viewGroupBaseClass: String): Boolean {
    return classTree.isSuccessorOf(this, viewGroupBaseClass) || this.name == viewGroupBaseClass
}