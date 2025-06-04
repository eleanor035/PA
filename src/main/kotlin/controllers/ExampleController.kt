package controllers

import framework.Mapping
import framework.Path
import framework.Param
import framework.RestController

@RestController
class ExampleController {

    /**
     * GET /hello
     */
    @Mapping("hello")
    fun helloWorld(): String = "Hello, World!"

    /**
     * GET /user/{id}
     * Retorna um JSON simples com informações do usuário
     */
    @Mapping("user/{id}")
    fun getUserById(@Path id: Int): Map<String, Any> {
        return mapOf(
            "id" to id,
            "name" to "Leonor Pereira",
            "email" to "lppao@iscte-iul.pt"
        )
    }

    /**
     * GET /search?q=xyz&page=2
     * Retorna resultados de busca. “page” tem valor default = 1
     */
    @Mapping("search")
    fun search(
        @Param("q") query: String,
        @Param("page") page: Int = 1
    ): Map<String, Any> {
        // Apenas para demonstração: devolve o que veio na query
        return mapOf(
            "query" to query,
            "page" to page,
            "results" to listOf("item1", "item2", "item3")
        )
    }

    /**
     * GET /filter?active=true
     * Parâmetro Bools e Strings podem ser nullables
     */
    @Mapping("filter")
    fun filter(
        @Param("active") active: Boolean?,
        @Param("type") type: String? = null
    ): Map<String, Any> {
        return mapOf(
            "active" to (active ?: false),
            "type" to (type ?: "any")
        )
    }
}
