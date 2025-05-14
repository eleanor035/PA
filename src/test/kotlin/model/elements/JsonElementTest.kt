package model.elements

import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

class JsonElementTest {

    @Test
    fun `depth calculation`() {
        val child = JSONString("test")
        val parent = JSONObject(listOf(JSONProperty("key", child)))
        child.owner = parent

        assertEquals(1, child.depth)
        assertEquals(0, parent.depth)
    }
}