package model.elements

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.*

class JsonArrayTest {

    @Test
    fun `serialize empty array`() {
        val jsonArray = JSONArray(emptyList())
        assertEquals("[]", jsonArray.serialize())
    }

    @Test
    fun `serialize array with elements`() {
        val jsonArray = JSONArray(listOf(JSONString("a"), JSONNumber(1)))
        assertEquals("[\"a\", 1]", jsonArray.serialize())
    }

    @Test
    fun `deepCopy creates new instance with copied elements`() {
        val original = JSONArray(listOf(JSONString("a")))
        val copy = original.deepCopy() as JSONArray
        assertEquals(original.elements[0], copy.elements[0])
        assertNotSame(original.elements[0], copy.elements[0])
    }

    @Test
    fun `filter applies predicate`() {
        val jsonArray = JSONArray(listOf(JSONNumber(1), JSONNumber(2)))
        val filtered = jsonArray.filter { it is JSONNumber && it.value == 1 }
        assertEquals(1, filtered.elements.size)
        assertEquals(1, (filtered.elements[0] as JSONNumber).value)
    }

    @Test
    fun `contains checks element presence`() {
        val jsonArray = JSONArray(listOf(JSONString("a")))
        assertTrue(jsonArray.contains(JSONString("a")))
        assertFalse(jsonArray.contains(JSONString("b")))
    }

    @Test
    fun `serialize nested array`() {
        val inner = JSONArray(listOf(JSONNumber(1), JSONNumber(2)))
        val outer = JSONArray(listOf(inner, JSONString("test")))
        assertEquals("[[1, 2], \"test\"]", outer.serialize())
    }

    @Test
    fun `add to nested array`() {
        val inner = JSONArray(listOf(JSONNumber(1)))
        val outer = JSONArray(listOf(inner))
        val modified = outer.add(0, JSONBoolean(true))
        assertEquals("[true, [1]]", modified.serialize())
    }

class SerializationTest {
    @Test
    fun `serialize boolean`() {
        assertEquals("true", JSONBoolean(true).serialize())
        assertEquals("false", JSONBoolean(false).serializePretty())
    }

    @Test
    fun `serialize number`() {
        assertEquals("3.14", JSONNumber(3.14).serialize())
        assertEquals("  42", JSONNumber(42).serializePretty(2))
    }

    @Test
    fun `serialize string`() {
        val s = JSONString("Hello\nWorld")
        assertEquals("\"Hello\\nWorld\"", s.serialize())
    }
}
}