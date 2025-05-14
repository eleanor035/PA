package model.visitors

import model.elements.*
import model.JSONVisitor

abstract class ArrayTypeCheckVisitor : JSONVisitor {
    private val errors = mutableListOf<String>()
    private var currentArrayType: Class<out JSONElement>? = null

    fun getValidationErrors(): List<String> = errors.toList()

    override fun visit(jsonArray: JSONArray): Boolean {
        currentArrayType = null
        jsonArray.elements.forEach {
            it.accept(this) // Triggers type-specific visit methods below
            if (currentArrayType == null && it !is NullValue) {
                currentArrayType = it.javaClass
            }
        }
        return false // Manual traversal (no auto-visit)
    }

    // Type-specific visit methods
    override fun visit(jsonString: JSONString): Boolean = checkType(jsonString)
    override fun visit(jsonBoolean: JSONBoolean): Boolean = checkType(jsonBoolean)
    override fun visit(jsonNumber: JSONNumber): Boolean = checkType(jsonNumber)
    override fun visit(jsonObject: JSONObject): Boolean = checkType(jsonObject)
    override fun visit(jsonProperty: JSONProperty): Boolean = checkType(jsonProperty)
    override fun visit(nullValue: NullValue): Boolean = true // Ignore nulls

    private fun checkType(element: JSONElement): Boolean {
        if (currentArrayType != null && element.javaClass != currentArrayType) {
            errors.add("Array contains mixed types: ${currentArrayType?.simpleName} and ${element.javaClass.simpleName}")
        }
        return true // Continue traversal
    }
}