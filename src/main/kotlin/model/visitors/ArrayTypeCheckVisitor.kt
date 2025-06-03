package model.visitors

import model.elements.*
import model.JSONVisitor

/**
 * A visitor that checks JSON arrays for consistent non-null element types.
 * Reports one error per array with mixed types.
 */
abstract class ArrayTypeCheckVisitor : JSONVisitor {
    private val errors = mutableListOf<String>()

    /**
     * Returns the list of validation errors found during traversal.
     */
    fun getValidationErrors(): List<String> = errors.toList()

    override fun visit(jsonArray: JSONArray): Boolean {
        // Perform type checking for this array
        val nonNullElements = jsonArray.elements.filter { it !is NullValue }
        if (nonNullElements.isNotEmpty()) {
            val firstType = nonNullElements.first().javaClass
            val hasMixedTypes = nonNullElements.any { it.javaClass != firstType }
            if (hasMixedTypes) {
                val secondType = nonNullElements.first { it.javaClass != firstType }.javaClass
                errors.add("Array contains mixed types: ${firstType.simpleName} and ${secondType.simpleName}")
            }
        }

        // Continue traversal to nested arrays
        return true // Allow visiting children
    }

    // Type-specific visit methods (no action needed for non-arrays)
    override fun visit(jsonString: JSONString): Boolean = true
    override fun visit(jsonBoolean: JSONBoolean): Boolean = true
    override fun visit(jsonNumber: JSONNumber): Boolean = true
    override fun visit(jsonObject: JSONObject): Boolean {
        jsonObject.entries.forEach { it.value.accept(this) }
        return true
    }
    override fun visit(jsonProperty: JSONProperty): Boolean {
        jsonProperty.value.accept(this)
        return true
    }
    override fun visit(nullValue: NullValue): Boolean = true

    // End visit methods (no action needed)
    override fun endVisit(jsonString: JSONString) {}
    override fun endVisit(jsonBoolean: JSONBoolean) {}
    override fun endVisit(jsonNumber: JSONNumber) {}
    override fun endVisit(jsonArray: JSONArray) {}
    override fun endVisit(jsonObject: JSONObject) {}
    override fun endVisit(jsonProperty: JSONProperty) {}
    override fun endVisit(nullValue: NullValue) {}
}