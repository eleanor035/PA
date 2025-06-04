// src/main/kotlin/framework/Main.kt
import controllers.ExampleController
import controllers.UserController
import framework.GetJson

/**
 * Ponto de entrada principal. A JVM procurará esta função ao executar
 * 'java -jar ...' se o manifest do JAR apontar para "framework.MainKt".
 */
fun main() {
    // Registre aqui todos os controllers que quiser expor
    // Exemplo: ExampleController está definido abaixo apenas para demonstração.
    GetJson(
        ExampleController::class,
        UserController::class
    ).start(8080)
    println("Servidor iniciado em http://localhost:8080")
}
