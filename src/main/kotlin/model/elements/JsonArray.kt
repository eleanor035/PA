package model.elements

import model.JSONVisitor

/**
 * Represents a JSON array with a list of JSON elements.
 * @property elements The list of JSON elements in the array.
 */
data class JSONArray(val elements: List<JSONElement>) : JSONElement() {
    constructor(elements: List<JSONElement>, owner: JSONElement?) : this(elements) {
        this.owner = owner
        // Ensure the elements list is immutable by copying if necessary
        require(elements != null) { "Elements list cannot be null" }
        elements.forEach { it.owner = this }
    }

    override fun serialize(): String {
        if (elements.isEmpty()) return "[]"
        val elementsStr = elements.joinToString(", ") { it.serialize() }
        return "[$elementsStr]"
    }

    override fun serializePretty(indent: Int): String {
        if (elements.isEmpty()) return "[]"
        val prefix = " ".repeat(indent.coerceAtLeast(0))
        val elementsStr = if (elements.size == 1) {
            elements.joinToString(" ") { it.serializePretty(indent) }
        } else {
            elements.joinToString(",\n") { "$prefix  ${it.serializePretty(indent + 2)}" }
        }
        return "[\n$elementsStr\n$prefix]"
    }

    fun filter(predicate: (JSONElement) -> Boolean): JSONArray {
        return JSONArray(elements.filter(predicate), owner)
    }

    fun map(transform: (JSONElement) -> JSONElement): JSONArray {
        return JSONArray(elements.map(transform), owner)
    }

    fun merge(other: JSONArray): JSONArray {
        return JSONArray(elements + other.elements, owner)
    }

    fun add(index: Int, element: JSONElement): JSONArray {
        if (index < 0 || index > elements.size) throw IndexOutOfBoundsException("Index: $index, Size: ${elements.size}")
        return JSONArray(elements.subList(0, index) + element + elements.subList(index, elements.size), owner)
    }

    fun removeAt(index: Int): JSONArray {
        if (index < 0 || index >= elements.size) throw IndexOutOfBoundsException("Index: $index, Size: ${elements.size}")
        return JSONArray(elements.subList(0, index) + elements.subList(index + 1, elements.size), owner)
    }

    override fun deepCopy(): JSONElement {
        return JSONArray(elements.map { it.deepCopy() }, owner)
    }

    fun get(index: Int): JSONElement? {
        return elements.getOrNull(index)
    }

    inline fun <reified T : JSONElement> getAs(index: Int): T? {
        return elements.getOrNull(index) as? T
    }

    fun contains(element: JSONElement): Boolean {
        return elements.contains(element)
    }

    val size: Int
        get() = elements.size

    val isEmpty: Boolean
        get() = elements.isEmpty()

    override fun accept(visitor: JSONVisitor) {
        if (visitor.visit(this)) {
            elements.forEach { it.accept(visitor) }
        }
        visitor.endVisit(this)
    }
}