package model.visitors

import model.elements.*
import model.JSONVisitor

abstract class JsonValidationVisitor : JSONVisitor {
    private val errors = mutableListOf<String>()

    fun validate(jsonElement: JSONElement): List<String> {
        errors.clear()
        jsonElement.accept(this)
        return errors.toList()
    }

    override fun visit(jsonString: JSONString): Boolean {
        // No specific validation for strings beyond structure
        return true
    }

    override fun visit(jsonBoolean: JSONBoolean): Boolean = true
    override fun visit(jsonNumber: JSONNumber): Boolean = true
    override fun visit(nullValue: NullValue): Boolean = true

    override fun visit(jsonObject: JSONObject): Boolean {
        val keys = jsonObject.entries.map { it.key }
        // Check for empty keys
        keys.filter { it.isEmpty() }.forEach {
            errors.add("Object contains empty key at depth ${jsonObject.depth}")
        }
        // Check for duplicate keys
        val duplicates = keys.groupBy { it }.filter { it.value.size > 1 }.keys
        duplicates.forEach {
            errors.add("Duplicate key '$it' in object at depth ${jsonObject.depth}")
        }
        return true // Continue traversing children
    }

    override fun visit(jsonArray: JSONArray): Boolean = true
    override fun visit(jsonProperty: JSONProperty): Boolean = true
}