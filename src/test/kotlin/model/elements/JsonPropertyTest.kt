package model.elements

import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotSame

class JsonPropertyTest {

    @Test
    fun `serialize property`() {
        val prop = JSONProperty("key", JSONString("value"))
        assertEquals("\"key\": \"value\"", prop.serialize())
    }

    @Test
    fun `deepCopy creates new instance with copied value`() {
        val original = JSONProperty("key", JSONString("value"))
        val copy = original.deepCopy() as JSONProperty
        assertEquals(original.value, copy.value)
        assertNotSame(original.value, copy.value)
    }
}