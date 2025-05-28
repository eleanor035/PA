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
        var arrayType: Class<out JSONElement>? = null
        var hasMixedTypes = false
        var secondType: Class<out JSONElement>? = null
        val nonNullElements = jsonArray.elements.filter { it !is NullValue }

        for (element in nonNullElements) {
            if (arrayType == null) {
                arrayType = element.javaClass
            } else if (element.javaClass != arrayType && !hasMixedTypes) {
                hasMixedTypes = true
                secondType = element.javaClass
            }
        }

        if (hasMixedTypes && arrayType != null && secondType != null) {
            errors.add("Array contains mixed types: ${arrayType.simpleName} and ${secondType.simpleName}")
        }

        // Traverse into each element for further validation
        jsonArray.elements.forEach { it.accept(this) }
        return true
    }

    // Type-specific visit methods (no type checking)
    override fun visit(jsonString: JSONString): Boolean = true
    override fun visit(jsonBoolean: JSONBoolean): Boolean = true
    override fun visit(jsonNumber: JSONNumber): Boolean = true
    override fun visit(jsonObject: JSONObject): Boolean = true
    override fun visit(jsonProperty: JSONProperty): Boolean = true
    override fun visit(nullValue: NullValue): Boolean = true

    // End visit methods
    override fun endVisit(jsonString: JSONString) {}
    override fun endVisit(jsonBoolean: JSONBoolean) {}
    override fun endVisit(jsonNumber: JSONNumber) {}
    override fun endVisit(jsonArray: JSONArray) {}
    override fun endVisit(jsonObject: JSONObject) {}
    override fun endVisit(jsonProperty: JSONProperty) {}
    override fun endVisit(nullValue: NullValue)  {}
}