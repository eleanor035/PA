package model.elements

import model.JSONVisitor

/**
 * Abstract base class for all JSON elements.
 */
abstract class JSONElement(owner: JSONElement? = null) {
    var owner: JSONElement? = owner
        internal set // Restrict external modification

    val depth: Int
        get() = owner?.depth?.plus(1) ?: 0

    abstract fun serialize(): String

    abstract fun serializePretty(indent: Int = 0): String

    abstract fun deepCopy(): JSONElement

    abstract fun accept(visitor: JSONVisitor): Boolean
}