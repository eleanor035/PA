package model.elements

import model.JSONVisitor

/**
 * JSON object property.
 * @property key The name of the property.
 * @property value The value of the property. Can be any [JSONElement].
 */
data class JSONProperty(val key: String, val value: JSONElement) : JSONElement() {
    constructor(key: String, value: JSONElement, owner: JSONElement?) : this(key, value) {
        this.owner = owner
        value.owner = this
    }

    init {
        value.owner = this
    }

    override fun serialize(): String {
        val escapedKey = escapeJsonString(key)
        return "\"$escapedKey\": ${value.serialize()}"
    }

    override fun serializePretty(indent: Int): String {
        val prefix = " ".repeat(indent.coerceAtLeast(0))
        val escapedKey = escapeJsonString(key)
        return "$prefix\"$escapedKey\": ${value.serializePretty(indent)}"
    }

    override fun deepCopy(): JSONElement {
        return JSONProperty(key, value.deepCopy(), owner)
    }

    override fun toString(): String = "\t".repeat(depth) + "\"$key\": $value"

    override fun accept(visitor: JSONVisitor) {
        if (visitor.visit(this)) {
            value.accept(visitor)
        }
        visitor.endVisit(this)
    }

    private fun escapeJsonString(s: String): String {
        return s.replace("\\", "\\\\")
            .replace("\"", "\\\"")
            .replace("\n", "\\n")
            .replace("\r", "\\r")
            .replace("\t", "\\t")
            .replace("\b", "\\b")
            .replace("\u000c", "\\f")
    }
}