package model.elements

import model.JSONVisitor

/**
 * Represents a JSON string value.
 * @property value The string value held by this [JSONElement].
 */
data class JSONString(val value: String) : JSONElement() {
    constructor(value: String, owner: JSONElement?) : this(value) {
        this.owner = owner
    }

    override fun serialize(): String {
        return "\"" + escapeJsonString(value) + "\""
    }

    override fun serializePretty(indent: Int): String {
        val prefix = " ".repeat(indent.coerceAtLeast(0))
        return "$prefix${serialize()}"
    }

    override fun deepCopy(): JSONElement {
        return JSONString(value, owner)
    }

    override fun toString(): String = value

    override fun accept(visitor: JSONVisitor): Boolean {
        val result = visitor.visit(this)
        visitor.endVisit(this)
        return result
    }

    private fun escapeJsonString(s: String): String {
        val escapeMap = mapOf(
            "\\" to "\\\\",
            "\"" to "\\\"",
            "\n" to "\\n",
            "\r" to "\\r",
            "\t" to "\\t",
            "\b" to "\\b",
            "\u000c" to "\\f"
        )
        return escapeMap.entries.fold(s) { acc, (char, replacement) ->
            acc.replace(char, replacement)
        }
    }
}