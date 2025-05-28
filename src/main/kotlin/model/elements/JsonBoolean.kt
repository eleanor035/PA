package model.elements

import model.JSONVisitor

/**
 * Represents a JSON boolean value.
 * @property value The boolean value of this JSON boolean.
 */
data class JSONBoolean(val value: Boolean) : JSONElement() {
    constructor(value: Boolean, owner: JSONElement?) : this(value) {
        this.owner = owner
    }

    override fun serialize(): String {
        return value.toString()
    }

    override fun serializePretty(indent: Int): String {
        val prefix = " ".repeat(indent.coerceAtLeast(0))
        return "$prefix${serialize()}"
    }

    override fun deepCopy(): JSONElement {
        return JSONBoolean(value, owner)
    }

    fun transform(transform: (Boolean) -> Boolean): JSONBoolean {
        return JSONBoolean(transform(value), owner)
    }

    val isTrue: Boolean
        get() = value

    val isFalse: Boolean
        get() = !value

    fun toBoolean(): Boolean {
        return value
    }

    override fun accept(visitor: JSONVisitor): Boolean {
        val result = visitor.visit(this)
        visitor.endVisit(this)
        return result
    }

}