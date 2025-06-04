package framework_test

import com.sun.net.httpserver.Headers
import com.sun.net.httpserver.HttpExchange
import framework.*
import kotlin.reflect.KFunction
import kotlin.reflect.KParameter
import kotlin.reflect.full.findAnnotation
import kotlin.reflect.full.functions
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.InputStream
import java.io.OutputStream
import java.net.URI
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class RequestHandlerTest {
    // Annotated test controller
    @RestController
    class TestController {
        @Mapping("hello")
        fun helloWorld(): String = "Hello, World!"

        @Mapping("search")
        fun search(
            @Param("q") query: String,
            @Param("limit") limit: Int = 10
        ): String = "Search: $query, limit: $limit"

        @Mapping("user/{id}")
        fun getUserById(@Path id: Int): String = "User $id"

        @Mapping("exception")
        fun exception(): String {
            throw RuntimeException("Test exception")
        }

        @Mapping("nullable")
        fun nullable(@Param("param") param: String?): String = param ?: "default"
    }

    private fun createMockExchange(
        method: String = "GET",
        path: String = "/hello",
        query: String? = null
    ): TestHttpExchange {
        return TestHttpExchange(method, path, query)
    }

    // Helper to get KFunction using reflection
    private fun getKFunction(controller: TestController, functionName: String): KFunction<*> {
        return controller::class.functions.first {
            it.name == functionName && it.findAnnotation<Mapping>() != null
        }
    }

    // Helper to create routes with reflection
    private fun createRoute(controller: TestController, functionName: String): Route {
        val function = getKFunction(controller, functionName)

        val pathParams = mutableMapOf<String, KParameter>()
        val queryParams = mutableMapOf<String, KParameter>()

        function.parameters.forEachIndexed { index, param ->
            if (index == 0) return@forEachIndexed // Skip receiver

            param.findAnnotation<Path>()?.let {
                val name = it.name.ifBlank { param.name!! }
                pathParams[name] = param
            }

            param.findAnnotation<Param>()?.let {
                val name = it.name.ifBlank { param.name!! }
                queryParams[name] = param
            }
        }

        val pathPattern = function.findAnnotation<Mapping>()!!.value

        return Route(
            pathPattern = pathPattern,
            handler = function,
            controller = controller,
            pathParams = pathParams,
            queryParams = queryParams
        )
    }

    @Test
    fun `handle GET request to valid route`() {
        val controller = TestController()
        val route = createRoute(controller, "helloWorld")
        val handler = RequestHandler(listOf(route))
        val exchange = createMockExchange()

        handler.handle(exchange)

        assertEquals(200, exchange.getResponseCode())
        assertEquals("\"Hello, World!\"", exchange.getResponseContent())
        assertEquals("application/json", exchange.responseHeaders.getFirst("Content-Type"))
    }

    @Test
    fun `handle request with path variable`() {
        val controller = TestController()
        val route = createRoute(controller, "getUserById")
        val handler = RequestHandler(listOf(route))
        val exchange = createMockExchange(path = "/user/123")

        handler.handle(exchange)

        assertEquals(200, exchange.getResponseCode())
        assertEquals("\"User 123\"", exchange.getResponseContent())
    }

    @Test
    fun `handle request with query parameters`() {
        val controller = TestController()
        val route = createRoute(controller, "search")
        val handler = RequestHandler(listOf(route))
        val exchange = createMockExchange(path = "/search", query = "q=test&limit=5")

        handler.handle(exchange)

        assertEquals(200, exchange.getResponseCode())
        assertEquals("\"Search: test, limit: 5\"", exchange.getResponseContent())
    }

    @Test
    fun `handle request with default query parameter`() {
        val controller = TestController()
        val route = createRoute(controller, "search")
        val handler = RequestHandler(listOf(route))
        val exchange = createMockExchange(path = "/search", query = "q=test")

        handler.handle(exchange)

        assertEquals(200, exchange.getResponseCode())
        assertEquals("\"Search: test, limit: 10\"", exchange.getResponseContent())
    }

    @Test
    fun `handle request with missing required parameter`() {
        val controller = TestController()
        val route = createRoute(controller, "search")
        val handler = RequestHandler(listOf(route))
        val exchange = createMockExchange(path = "/search", query = "limit=10")

        handler.handle(exchange)

        assertEquals(400, exchange.getResponseCode())
        assertTrue(exchange.getResponseContent().contains("Bad Request"))
    }

    @Test
    fun `handle request with nullable parameter`() {
        val controller = TestController()
        val route = createRoute(controller, "nullable")
        val handler = RequestHandler(listOf(route))
        val exchange = createMockExchange(path = "/nullable")

        handler.handle(exchange)

        assertEquals(200, exchange.getResponseCode())
        assertEquals("\"default\"", exchange.getResponseContent())
    }

    @Test
    fun `parse query string with special characters`() {
        val handler = RequestHandler(emptyList())
        val query = "q=hello%20world&price=100%24"
        val params = handler.parseQueryString(query)

        assertEquals("hello world", params["q"])
        assertEquals("100$", params["price"])
    }

    @Test
    fun `parse query string with empty values`() {
        val handler = RequestHandler(emptyList())
        val params = handler.parseQueryString("param1=&param2")

        assertEquals("", params["param1"])
        assertEquals("", params["param2"])
    }
}

class TestHttpExchange(
    private val method: String = "GET",
    private val path: String = "/",
    private val query: String? = null
) : HttpExchange() {
    private val headers = Headers()
    private val requestBodyBuffer = ByteArrayOutputStream() // para simular escrita
    private var requestBody: InputStream = ByteArrayInputStream(ByteArray(0)) // usado em getRequestBody()
    private val responseBody = ByteArrayOutputStream()
    private var responseCode = 0
    private var responseLength: Long = 0

    init {
        headers.set("Content-Type", "text/plain")
    }

    fun setRequestBody(content: String) {
        requestBody = ByteArrayInputStream(content.toByteArray(Charsets.UTF_8))
    }

    override fun getRequestMethod() = method
    override fun getRequestURI() = URI.create("http://localhost$path${query?.let { "?$it" } ?: ""}")
    override fun getResponseHeaders() = headers
    override fun getResponseBody() = responseBody
    override fun getRequestBody() = requestBody
    override fun sendResponseHeaders(rCode: Int, rLength: Long) {
        responseCode = rCode
        responseLength = rLength
    }

    // Helper methods
    override fun getResponseCode() = responseCode
    fun getResponseLength() = responseLength
    fun getResponseContent() = responseBody.toString(Charsets.UTF_8)

    override fun getRequestHeaders() = headers
    override fun getHttpContext() = throw UnsupportedOperationException()
    override fun getProtocol() = "HTTP/1.1"
    override fun getAttribute(name: String?) = null
    override fun setAttribute(name: String?, value: Any?) {}
    override fun setStreams(input: InputStream?, output: OutputStream?) {}
    override fun getPrincipal() = null
    override fun getLocalAddress() = throw UnsupportedOperationException()
    override fun getRemoteAddress() = throw UnsupportedOperationException()
    override fun close() {}
}