package model.elements

import model.JSONVisitor

/**
 * Represents a JSON object with a list of key-value properties.
 * @property entries The list of properties, where each property has a unique key.
 */
data class JSONObject(val entries: List<JSONProperty>) : JSONElement() {
    constructor(entries: List<JSONProperty>, owner: JSONElement?) : this(entries) {
        this.owner = owner
        // Set the owner of each JSONProperty to this JSONObject
        entries.forEach { it.owner = this }
    }

    init {
        // Validate unique and non-empty keys
        val keys = entries.map { it.key }
        require(keys.all { it.isNotEmpty() }) { "Property keys cannot be empty: $keys" }
        val duplicates = keys.groupingBy { it }.eachCount().filter { it.value > 1 }.keys
        require(duplicates.isEmpty()) { "Property keys must be unique, duplicates found: $duplicates" }
    }

    override fun serialize(): String {
        if (entries.isEmpty()) return "{}"
        val entriesStr = entries.joinToString(", ") { it.serialize() }
        return "{$entriesStr}"
    }

    override fun serializePretty(indent: Int): String {
        if (entries.isEmpty()) return "{}"
        val prefix = " ".repeat(indent.coerceAtLeast(0))
        val entriesStr = entries.joinToString(",\n") {
            "$prefix  ${it.serializePretty(indent + 2)}"
        }
        return "{\n$entriesStr\n$prefix}"
    }

    fun filter(predicate: (JSONProperty) -> Boolean): JSONObject {
        return JSONObject(entries.filter(predicate), owner)
    }

    override fun deepCopy(): JSONElement {
        return JSONObject(entries.map { it.copy(value = it.value.deepCopy()) }, owner)
    }

    fun get(key: String): JSONElement? {
        return entries.find { it.key == key }?.value
    }

    inline fun <reified T : JSONElement> getAs(key: String): T? {
        return get(key) as? T
    }

    val isEmpty: Boolean
        get() = entries.isEmpty()

    override fun accept(visitor: JSONVisitor) {
        if (visitor.visit(this)) {
            entries.forEach { it.accept(visitor) }
        }
        visitor.endVisit(this)
    }
}