package model.elements

import model.JSONVisitor

/**
 * Represents a JSON number value.
 * @property value The number value held by this [JSONElement].
 */
data class JSONNumber(val value: Number) : JSONElement() {
    constructor(value: Number, owner: JSONElement?) : this(value) {
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
        return JSONNumber(value, owner)
    }

    override fun toString(): String = value.toString()

    override fun accept(visitor: JSONVisitor): Boolean {
        val result = visitor.visit(this)
        visitor.endVisit(this)
        return result
    }

}