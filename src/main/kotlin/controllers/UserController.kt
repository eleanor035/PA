package controllers

import framework.Mapping
import framework.Path
import framework.RestController

@RestController
class UserController {

    /**
     * GET /user/all
     * Exemplo de rota sem nenhuma variável: lista fixa de usuários
     */
    @Mapping("user/all")
    fun listAll(): List<Map<String, Any>> {
        return listOf(
            mapOf("id" to 1, "name" to "Alice"),
            mapOf("id" to 2, "name" to "Bob"),
            mapOf("id" to 3, "name" to "Charlie")
        )
    }

    /**
     * GET /user/stats/{year}
     * Exemplo de rota com variável de rota “year”
     */
    @Mapping("user/stats/{year}")
    fun userStats(@Path year: Int): Map<String, Any> {
        // Exemplo de retorno dummy
        return mapOf(
            "year" to year,
            "newUsers" to 42,
            "activeUsers" to 1000
        )
    }
}
