package model.elements

import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertSame
import kotlin.test.assertTrue
import kotlin.test.assertFalse

class NullValueTest {

    @Test
    fun `serialize null`() {
        val nullValue = NullValue
        assertEquals("null", nullValue.serialize())
    }

    @Test
    fun `serializePretty with indent`() {
        val nullValue = NullValue
        assertEquals("  null", nullValue.serializePretty(2))
    }

    @Test
    fun `deepCopy returns the same instance`() {
        val original = NullValue
        val copy = original.deepCopy() as NullValue
        assertSame(original, copy)
    }

    @Test
    fun `equals checks type`() {
        val nullValue = NullValue
        assertTrue(nullValue.equals(NullValue))
        assertFalse(nullValue.equals(JSONString("test")))
    }
}