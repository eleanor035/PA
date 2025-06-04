package framework

import com.sun.net.httpserver.HttpExchange
import com.sun.net.httpserver.HttpHandler
import model.inference.JsonConverter
import model.visitors.JsonValidationVisitor
import java.io.OutputStream
import java.net.URLDecoder
import java.nio.charset.StandardCharsets
import kotlin.reflect.KParameter

class RequestHandler(private val routes: List<Route>) : HttpHandler {
    override fun handle(exchange: HttpExchange) {
        try {
            exchange.responseHeaders.set("Access-Control-Allow-Origin", "*")
            exchange.responseHeaders.set("Access-Control-Allow-Methods", "GET")
            exchange.responseHeaders.set("Access-Control-Allow-Headers", "Content-Type")
            val requestMethod = exchange.requestMethod
            if (requestMethod != "GET") {
                sendResponse(exchange, 405, "Method Not Allowed")
                return
            }

            // Retira a "/" inicial para que a rota "search" corresponda a "/search"
            val requestPath = exchange.requestURI.path.removePrefix("/")
            val queryParams = parseQueryString(exchange.requestURI.query)

            val matchingRoutes = routes.filter { it.match(requestPath) }

            val route = routes.find { it.match(requestPath) }
            if (route == null) {
                sendResponse(exchange, 404, "Not Found")
                return
            }

            val pathVariables = route.extractPathVariables(requestPath)
            val rawArgs = route.parseArguments(pathVariables, queryParams)

            // =====================================
            // Montar o Map<KParameter, Any?> para callBy
            // =====================================
            val paramMap = mutableMapOf<KParameter, Any?>()

            // 1. Receiver (instância do controller) em parâmetro[0]
            paramMap[route.handler.parameters[0]] = route.controller

            // 2. Iterar sobre parâmetros de índice >= 1
            route.handler.parameters.forEachIndexed { index, kparam ->
                if (index == 0) return@forEachIndexed // pular receiver

                val value = rawArgs[index]

                // Lógica corrigida para tratamento de parâmetros
                if (value == null) {
                    // Parâmetros obrigatórios ausentes geram erro
                    if (!kparam.isOptional && !kparam.type.isMarkedNullable) {
                        sendResponse(exchange, 400, "Bad Request: Missing required parameter '${kparam.name}'")
                        return
                    }
                    // Parâmetros nullable recebem null explicitamente
                    if (kparam.type.isMarkedNullable) {
                        paramMap[kparam] = null
                    }
                    // Parâmetros opcionais são omitidos (callBy usará valor padrão)
                } else {
                    // Parâmetros com valor são incluídos normalmente
                    paramMap[kparam] = value
                }
            }

            // 3. Invocar o método com callBy (aplica defaults automaticamente)
            val result = route.handler.callBy(paramMap)
            val jsonElement = JsonConverter.toJsonElement(result)

            // 4. Validar o JSON de retorno
            val keyValidator = JsonValidationVisitor()
            jsonElement.accept(keyValidator)
            val keyErrors = keyValidator.getValidationErrors()
            if (keyErrors.isNotEmpty()) {
                sendResponse(exchange, 500, "JSON validation failed: ${keyErrors.joinToString()}")
                return
            }

            val json = jsonElement.serialize()
            sendResponse(exchange, 200, json, "application/json")
        } catch (e: Exception) {
            sendResponse(exchange, 500, "Internal Server Error: ${e.message}")
        }
    }

    internal fun parseQueryString(query: String?): Map<String, String> {
        if (query.isNullOrBlank()) return emptyMap()

        return query.split("&").associate { pair ->
            val parts = pair.split("=")
            val name = URLDecoder.decode(parts[0], StandardCharsets.UTF_8.name())
            val value = if (parts.size > 1) {
                URLDecoder.decode(parts[1], StandardCharsets.UTF_8.name())
            } else {
                ""
            }
            name to value
        }
    }

    private fun sendResponse(
        exchange: HttpExchange,
        status: Int,
        content: String,
        contentType: String = "text/plain"
    ) {
        exchange.responseHeaders.set("Content-Type", contentType)
        val contentBytes = content.toByteArray()
        exchange.sendResponseHeaders(status, contentBytes.size.toLong())
        exchange.responseBody.use { os: OutputStream ->
            os.write(contentBytes)
        }
    }
}