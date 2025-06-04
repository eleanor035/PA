package framework_test

import framework.GetJson
import framework.Mapping
import framework.RestController
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

class GetJsonTest {
    @RestController
    class ValidController {
        @Mapping("/valid")
        fun validEndpoint() = "OK"
    }

    class InvalidController {
        // Missing RestController annotation
        @Mapping("/invalid")
        fun invalidEndpoint() = "NOK"
    }

    @Test
    fun `should register valid controller endpoints`() {
        val getJson = GetJson(ValidController::class)
        assertEquals(1, getJson.routes.size)
        assertEquals("/valid", getJson.routes[0].pathPattern)
    }

    @Test
    fun `should ignore non-annotated controllers`() {
        val getJson = GetJson(InvalidController::class)
        assertTrue(getJson.routes.isEmpty())
    }

    @Test
    fun `should fail for controllers without no-arg constructor`() {
        class NoConstructorController(val param: String) {
            @Mapping("/test") fun test() = ""
        }

        assertFailsWith<IllegalArgumentException> {
            GetJson(NoConstructorController::class)
        }
    }
}