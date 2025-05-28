package model.elements

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.*
//or import kotlin.test.assertSomething

class JsonNumberTest {

    @Test
    fun `serialize number`() {
        val jsonNumber = JSONNumber(42.5)
        assertEquals("42.5", jsonNumber.serialize())
    }

    @Test
    fun `serializePretty with indent`() {
        val jsonNumber = JSONNumber(123)
        assertEquals("  123", jsonNumber.serializePretty(2))
    }

    @Test
    fun `deepCopy creates new instance`() {
        val original = JSONNumber(42)
        val copy = original.deepCopy() as JSONNumber
        assertEquals(original.value, copy.value)
        assertNotSame(original, copy)
    }
}