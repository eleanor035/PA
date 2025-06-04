// src/main/kotlin/framework/Main.kt
import framework.GetJson
import framework.Mapping
import framework.Path
import framework.RestController


/**
 * Ponto de entrada principal. A JVM procurará esta função ao executar
 * 'java -jar ...' se o manifest do JAR apontar para "framework.MainKt".
 */
fun main() {
    // Registre aqui todos os controllers que quiser expor
    // Exemplo: ExampleController está definido abaixo apenas para demonstração.
    GetJson(ExampleController::class).start(8080)
}

@RestController
class ExampleController {
    @Mapping("hello")
    fun helloWorld(): String = "Hello, World!"

    @Mapping("user/{id}")
    fun getUserById(@Path id: Int): Map<String, Any> {
        return mapOf(
            "id" to id,
            "name" to "Leonor Pereira",
            "email" to "lppao@iscte-iul.pt"
        )
    }
}
