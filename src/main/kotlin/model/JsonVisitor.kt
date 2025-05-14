package model

import model.elements.*

/**
 * Visitor interface for traversing JSON elements.
 */

interface JSONVisitor {
    fun visit(jsonString: JSONString): Boolean
    fun endVisit(jsonString: JSONString)

    fun visit(jsonBoolean: JSONBoolean): Boolean
    fun endVisit(jsonBoolean: JSONBoolean)

    fun visit(jsonNumber: JSONNumber): Boolean
    fun endVisit(jsonNumber: JSONNumber)

    fun visit(jsonArray: JSONArray): Boolean
    fun endVisit(jsonArray: JSONArray)

    fun visit(jsonObject: JSONObject): Boolean
    fun endVisit(jsonObject: JSONObject)

    fun visit(jsonProperty: JSONProperty): Boolean
    fun endVisit(jsonProperty: JSONProperty)

    fun visit(nullValue: NullValue): Boolean
    fun endVisit(nullValue: NullValue)
}