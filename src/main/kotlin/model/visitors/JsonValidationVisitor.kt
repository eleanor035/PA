package model.visitors

import model.elements.*
import model.JSONVisitor

/**
 * A visitor that validates JSON objects for unique and non-empty keys.
 * Collects errors with depth context during traversal.
 */
open class JsonValidationVisitor : JSONVisitor {
    private val errors = mutableListOf<String>()

    /**
     * Returns the list of validation errors found during traversal.
     */
    fun getValidationErrors(): List<String> = errors.toList()

    override fun visit(jsonString: JSONString): Boolean = true
    override fun visit(jsonBoolean: JSONBoolean): Boolean = true
    override fun visit(jsonNumber: JSONNumber): Boolean = true
    override fun visit(nullValue: NullValue): Boolean = true

    override fun visit(jsonObject: JSONObject): Boolean {
        val keys = jsonObject.entries.map { it.key }
        val depth = calculateDepth(jsonObject)

        // Check for empty or blank keys
        if (keys.any { it.isBlank() }) {
            errors.add("Object contains empty key at depth $depth")
        }

        // Check for duplicate keys
        val duplicates = keys.groupingBy { it }.eachCount().filter { it.value > 1 }.keys
        duplicates.forEach {
            errors.add("Duplicate key '$it' in object at depth $depth")
        }

        // Traverse into properties' values
        jsonObject.entries.forEach { it.value.accept(this) }
        return true
    }

    override fun visit(jsonArray: JSONArray): Boolean {
        // Traverse into array elements
        jsonArray.elements.forEach { it.accept(this) }
        return true
    }

    override fun visit(jsonProperty: JSONProperty): Boolean {
        // Traverse into the property's value
        jsonProperty.value.accept(this)
        return true
    }

    override fun endVisit(jsonString: JSONString) {}
    override fun endVisit(jsonBoolean: JSONBoolean) {}
    override fun endVisit(jsonNumber: JSONNumber) {}
    override fun endVisit(nullValue: NullValue) {}
    override fun endVisit(jsonObject: JSONObject) {}
    override fun endVisit(jsonArray: JSONArray) {}
    override fun endVisit(jsonProperty: JSONProperty) {}

    /**
     * Calculates the depth of a JSON element in the hierarchy.
     * @param element The JSON element to calculate depth for.
     * @return The depth (0 for root, 1 for first-level children, etc.).
     */
    private fun calculateDepth(element: JSONElement): Int {
        var depth = 0
        var current: JSONElement? = element
        while (current?.owner != null) {
            current = current.owner
            depth++
        }
        return depth
    }

    fun validate(element: JSONElement): List<String> {
        element.accept(this)
        return getValidationErrors()
    }
}