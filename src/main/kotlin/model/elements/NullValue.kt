package model.elements

import model.JSONVisitor

/**
 * Represents a JSON null value.
 */
class NullValue(owner: JSONElement? = null) : JSONElement(owner) {

    override fun serialize(): String = "null"

    override fun serializePretty(indent: Int): String {
        val prefix = " ".repeat(indent.coerceAtLeast(0))
        return "$prefix${serialize()}"
    }

    override fun deepCopy(): JSONElement = NullValue(owner)

    override fun accept(visitor: JSONVisitor) {
        if (visitor.visit(this)) {
            visitor.endVisit(this)
        }
    }

    override fun equals(other: Any?): Boolean = other is NullValue

    override fun hashCode(): Int = javaClass.hashCode()
}