package model.elements

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.*

class JsonBooleanTest {

    @Test
    fun `serialize boolean`() {
        val jsonBool = JSONBoolean(true)
        assertEquals("true", jsonBool.serialize())
    }

    @Test
    fun `serializePretty with indent`() {
        val jsonBool = JSONBoolean(false)
        assertEquals("  false", jsonBool.serializePretty(2))
    }

    @Test
    fun `deepCopy creates new instance`() {
        val original = JSONBoolean(true)
        val copy = original.deepCopy() as JSONBoolean
        assertEquals(original.value, copy.value)
        assertNotSame(original, copy)
    }

    @Test
    fun `transform applies function`() {
        val jsonBool = JSONBoolean(false)
        val transformed = jsonBool.transform { !it }
        assertEquals(true, transformed.value)
    }
}