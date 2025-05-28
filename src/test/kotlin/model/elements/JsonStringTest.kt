package model.elements

import model.JSONVisitor

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.*

class JsonStringTest {

    @Test
    fun `serialize simple string`() {
        val jsonString = JSONString("hello")
        assertEquals("\"hello\"", jsonString.serialize())
    }

    @Test
    fun `serialize with special characters`() {
        val jsonString = JSONString("hello\nworld\t\"quote\"")
        assertEquals("\"hello\\nworld\\t\\\"quote\\\"\"", jsonString.serialize())
    }

    @Test
    fun `serializePretty with indent`() {
        val jsonString = JSONString("hello")
        assertEquals("  \"hello\"", jsonString.serializePretty(2))
    }

    @Test
    fun `deepCopy creates new instance with same value and owner`() {
        val original = JSONString("test")
        val owner = JSONObject(listOf(JSONProperty("key", original)))
        original.owner = owner
        val copy = original.deepCopy() as JSONString

        assertEquals(original.value, copy.value)
        assertEquals(original.owner, copy.owner)
        assertNotSame(original, copy)
    }

    @Test
    fun `accept visitor pattern`() {
        val jsonString = JSONString("test")
        var visited = false
        val visitor = object : JSONVisitor {
            override fun visit(jsonString: JSONString): Boolean {
                visited = true
                return true
            }
            override fun endVisit(jsonString: JSONString) {}
            override fun visit(jsonBoolean: JSONBoolean): Boolean = true
            override fun endVisit(jsonBoolean: JSONBoolean) {}
            override fun visit(jsonNumber: JSONNumber): Boolean = true
            override fun endVisit(jsonNumber: JSONNumber) {}
            override fun visit(jsonArray: JSONArray): Boolean = true
            override fun endVisit(jsonArray: JSONArray) {}
            override fun visit(jsonObject: JSONObject): Boolean = true
            override fun endVisit(jsonObject: JSONObject) {}
            override fun visit(jsonProperty: JSONProperty): Boolean = true
            override fun endVisit(jsonProperty: JSONProperty) {}
            override fun visit(jsonNull: NullValue): Boolean = true
            override fun endVisit(jsonNull: NullValue) {}
        }
        jsonString.accept(visitor)
        assertTrue(visited)
    }
}