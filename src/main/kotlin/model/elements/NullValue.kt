package model.elements

import model.JSONVisitor

/**
 * Represents a JSON null value.
 */
data object NullValue : JSONElement(null) {

    override fun serialize(): String = "null"

    override fun serializePretty(indent: Int): String {
        val prefix = " ".repeat(indent.coerceAtLeast(0))
        return "$prefix${serialize()}"
    }

    override fun deepCopy(): JSONElement = NullValue

    override fun accept(visitor: JSONVisitor): Boolean {
        val result = visitor.visit(this)
        visitor.endVisit(this)
        return result
    }

}