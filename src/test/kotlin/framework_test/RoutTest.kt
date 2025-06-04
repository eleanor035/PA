package framework_test

import framework.*
import kotlin.reflect.KFunction
import kotlin.reflect.KParameter
import kotlin.reflect.full.findAnnotation
import kotlin.reflect.full.functions
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import kotlin.test.fail

enum class Color { RED, GREEN, BLUE }

class RouteTest {
    @RestController
    class TestController {
        @Mapping("user/{id}")
        fun getUser(@Path id: String) = "User $id"

        @Mapping("product/{category}/{id}")
        fun getProduct(
            @Path category: String,
            @Path id: Int
        ) = "Product $category-$id"

        @Mapping("search")
        fun search(
            @Param("q") query: String,
            @Param("page") page: Int = 1
        ) = "Results for $query, page $page"
    }

    // Helper to get KFunction using reflection
    private fun getKFunction(controller: TestController, functionName: String): KFunction<*> {
        return controller::class.functions.first {
            it.name == functionName && it.findAnnotation<Mapping>() != null
        }
    }

    @Test
    fun `match simple path`() {
        val controller = TestController()
        val function = getKFunction(controller, "getUser")
        val route = Route(
            pathPattern = "/user/{id}",
            handler = function,
            controller = controller,
            pathParams = mapOf("id" to function.parameters[1]),
            queryParams = emptyMap()
        )

        assertTrue(route.match("/user/123"))
        assertFalse(route.match("/user"))
        assertFalse(route.match("/user/123/profile"))
    }

    @Test
    fun `match multi-segment path`() {
        val controller = TestController()
        val function = getKFunction(controller, "getProduct")
        val route = Route(
            pathPattern = "/product/{category}/{id}",
            handler = function,
            controller = controller,
            pathParams = mapOf(
                "category" to function.parameters[1],
                "id" to function.parameters[2]
            ),
            queryParams = emptyMap()
        )

        assertTrue(route.match("/product/books/42"))
        assertFalse(route.match("/product/books"))
        assertFalse(route.match("/product/books/42/extra"))
    }

    @Test
    fun `extract path variables`() {
        val controller = TestController()
        val function = getKFunction(controller, "getProduct")
        val route = Route(
            pathPattern = "/product/{category}/{id}",
            handler = function,
            controller = controller,
            pathParams = mapOf(
                "category" to function.parameters[1],
                "id" to function.parameters[2]
            ),
            queryParams = emptyMap()
        )

        val variables = route.extractPathVariables("/product/electronics/99")
        assertEquals(mapOf("category" to "electronics", "id" to "99"), variables)
    }

    @Test
    fun `parse integer path variable`() {
        val controller = TestController()
        val function = getKFunction(controller, "getProduct")
        val route = Route(
            pathPattern = "/product/{category}/{id}",
            handler = function,
            controller = controller,
            pathParams = mapOf(
                "category" to function.parameters[1],
                "id" to function.parameters[2]
            ),
            queryParams = emptyMap()
        )

        val args = route.parseArguments(
            pathVariables = mapOf("category" to "books", "id" to "42"),
            queryParams = emptyMap()
        )

        assertEquals("books", args[1])
        assertEquals(42, args[2])
    }

    @Test
    fun `parse query parameters`() {
        val controller = TestController()
        val function = getKFunction(controller, "search")
        val route = Route(
            pathPattern = "/search",
            handler = function,
            controller = controller,
            pathParams = emptyMap(),
            queryParams = mapOf(
                "q" to function.parameters[1],
                "page" to function.parameters[2]
            )
        )

        val args = route.parseArguments(
            pathVariables = emptyMap(),
            queryParams = mapOf("q" to "kotlin", "page" to "2")
        )

        assertEquals("kotlin", args[1])
        assertEquals(2, args[2])
    }

    @Test
    fun `use default value for missing query param`() {
        val controller = TestController()
        val function = getKFunction(controller, "search")

        // Simula uma rota completa com handler
        val route = Route(
            pathPattern = "/search",
            handler = function,
            controller = controller,
            pathParams = emptyMap(),
            queryParams = mapOf(
                "q" to function.parameters[1],
                "page" to function.parameters[2] // Adiciona mapeamento para page
            )
        )

        // Simula o processamento de uma requisição
        val pathVariables = emptyMap<String, String>()
        val queryParams = mapOf("q" to "framework") // Page ausente

        // Extrai e converte argumentos
        val rawArgs = route.parseArguments(pathVariables, queryParams)

        // Simula o RequestHandler
        val paramMap = mutableMapOf<KParameter, Any?>()
        paramMap[function.parameters[0]] = controller // receiver

        function.parameters.forEachIndexed { index, param ->
            if (index == 0) return@forEachIndexed

            when (val value = rawArgs[index]) {
                null -> {
                    if (!param.isOptional && !param.type.isMarkedNullable) {
                        // Usando fail corretamente importado
                        fail("Missing required parameter")
                    }
                    if (param.type.isMarkedNullable) {
                        paramMap[param] = null
                    }
                }
                else -> paramMap[param] = value
            }
        }

        // Invoca o método
        val result = function.callBy(paramMap) as String

        // Verifica o resultado final
        assertEquals("Results for framework, page 1", result)
    }


    @Test
    fun `convert enum parameters`() {
        // 1) Declaramos o enum
        // 2) Criamos um controller dedicado só para esse teste, contendo getColor(...)
        class ColorController {
            @Mapping("color/{color}")
            fun getColor(@Path color: Color) = color.name
        }

        val controller = ColorController()
        val function = controller::class.functions.first {
            it.name == "getColor" && it.findAnnotation<Mapping>() != null
        }

        // 4) Construímos a rota apontando para “color/{color}”
        val route = Route(
            pathPattern = "color/{color}",          // sem “/” na frente, pois nosso RequestHandler faz removePrefix("/")
            handler = function,
            controller = controller,
            pathParams = mapOf("color" to function.parameters[1]),
            queryParams = emptyMap()
        )

        // 5) Chamamos parseArguments com “color=GREEN”
        val args = route.parseArguments(
            pathVariables = mapOf("color" to "GREEN"),
            queryParams = emptyMap()
        )

        // 6) Por fim, verificamos que args[1] é do tipo Color e vale Color.GREEN
        val enumArg = args[1]
        assertTrue(enumArg is Color, "Expected argument to be of type Color")
        assertEquals(Color.GREEN, enumArg)
    }
}