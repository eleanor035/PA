package model.elements

import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotSame
import kotlin.test.assertTrue
import kotlin.test.assertNull // Now used!
import kotlin.test.assertFailsWith

class JsonObjectTest {

    @Test
    fun `serialize empty object`() {
        val jsonObject = JSONObject(emptyList())
        assertEquals("{}", jsonObject.serialize())
    }

    @Test
    fun `serialize object with properties`() {
        val jsonObject = JSONObject(listOf(JSONProperty("key", JSONString("value"))))
        assertEquals("{\"key\": \"value\"}", jsonObject.serialize())
    }

    @Test
    fun `deepCopy creates new instance with copied properties`() {
        val original = JSONObject(listOf(JSONProperty("key", JSONString("value"))))
        val copy = original.deepCopy() as JSONObject
        assertEquals(original.entries[0].value, copy.entries[0].value)
        assertNotSame(original.entries[0].value, copy.entries[0].value)
    }

    @Test
    fun `get returns correct value`() {
        val jsonObject = JSONObject(listOf(JSONProperty("key", JSONString("value"))))
        assertEquals(JSONString("value"), jsonObject.get("key"))
    }

    @Test
    fun `get returns null for non-existent key`() {
        val jsonObject = JSONObject(listOf(JSONProperty("key", JSONString("value"))))
        assertNull(jsonObject.get("invalid_key"))
    }

    @Test
    fun `getAs returns typed value`() {
        val jsonObject = JSONObject(listOf(JSONProperty("key", JSONString("value"))))
        val value = jsonObject.getAs<JSONString>("key")
        assertEquals("value", value?.value)
    }

    @Test
    fun `getAs returns null for incorrect type`() {
        val jsonObject = JSONObject(listOf(JSONProperty("key", JSONString("value"))))
        val value = jsonObject.getAs<JSONNumber>("key")
        assertNull(value)
    }

    @Test
    fun `init validates unique keys`() {
        val exception = assertFailsWith<IllegalArgumentException> {
            JSONObject(listOf(
                JSONProperty("key", JSONString("a")),
                JSONProperty("key", JSONString("b"))
            ))
        }
        assertTrue(exception.message!!.contains("Property keys must be unique"))
    }
}