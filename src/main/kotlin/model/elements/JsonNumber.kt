package model.elements

import model.JSONVisitor
import java.math.BigDecimal

/**
 * Represents a JSON number value.
 * @property value The number value held by this [JSONElement].
 */
data class JSONNumber(val value: Number) : JSONElement() {
    constructor(value: Number, owner: JSONElement?) : this(value) {
        this.owner = owner
    }

    init {
        if (value is Double && !value.isFinite()) {
            throw IllegalArgumentException("JSON numbers must be finite")
        }
        if (value is Float && !value.isFinite()) {
            throw IllegalArgumentException("JSON numbers must be finite")
        }
    }

    override fun serialize(): String {
        return value.toString()
    }

    override fun serializePretty(indent: Int): String {
        val prefix = " ".repeat(indent.coerceAtLeast(0))
        return "$prefix${serialize()}"
    }

    override fun deepCopy(): JSONElement {
        return JSONNumber(value)
    }

    override fun toString(): String = value.toString()

    override fun accept(visitor: JSONVisitor): Boolean {
        val result = visitor.visit(this)
        visitor.endVisit(this)
        return result
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is JSONNumber) return false
        return BigDecimal(this.value.toString()) == BigDecimal(other.value.toString())
    }

    override fun hashCode(): Int {
        return BigDecimal(value.toString()).hashCode()
    }
}