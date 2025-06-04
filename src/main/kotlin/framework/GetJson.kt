package framework

import com.sun.net.httpserver.HttpServer
import kotlin.reflect.KClass
import kotlin.reflect.KParameter
import kotlin.reflect.KFunction
import kotlin.reflect.full.findAnnotation
import kotlin.reflect.full.declaredFunctions
import java.net.InetSocketAddress
import java.util.logging.Logger

class GetJson(vararg controllers: KClass<*>) {
    internal val routes = mutableListOf<Route>()
    private val logger = Logger.getLogger(GetJson::class.java.name)

    init {
        controllers.forEach { controllerClass ->
            // 1) Verificar se existe algum construtor SEM PARÂMETROS; se não existir, falhar.
            val noArgCtor = controllerClass.constructors.firstOrNull { it.parameters.isEmpty() }
                ?: throw IllegalArgumentException(
                    "Controller ${controllerClass.simpleName} must have a no-arg constructor"
                )

            // 2) Se não tiver @RestController, simplesmente ignoramos (sem lançar nada).
            if (controllerClass.findAnnotation<RestController>() == null) {
                return@forEach
            }

            // 3) Como passou na verificação de no-arg + tem @RestController, instanciamos e registramos
            val controllerInstance = try {
                noArgCtor.call()
            } catch (ex: Exception) {
                throw IllegalArgumentException(
                    "Failed to invoke no-arg constructor for ${controllerClass.simpleName}"
                )
            }

            // 4) Percorremos os métodos declarados e registramos os que têm @GetMapping
            controllerClass.declaredFunctions.forEach { function ->
                function.findAnnotation<Mapping>()?.let { mapping ->
                    val pathPattern = mapping.value
                    routes.add(createRoute(controllerInstance, function, pathPattern))
                }
            }
        }
    }

    private fun createRoute(
        controller: Any,
        function: KFunction<*>,
        pathPattern: String
    ): Route {
        val pathParams = mutableMapOf<String, KParameter>()
        val queryParams = mutableMapOf<String, KParameter>()

        function.parameters.forEachIndexed { index, param ->
            if (index == 0) return@forEachIndexed // pular o receiver

            param.findAnnotation<Path>()?.let { pathVar ->
                val name = pathVar.name.takeIf { it.isNotBlank() }
                    ?: param.name
                    ?: throw IllegalArgumentException("Path variable must have a name")
                pathParams[name] = param
            }

            param.findAnnotation<Param>()?.let { requestParam ->
                val name = requestParam.name.takeIf { it.isNotBlank() }
                    ?: param.name
                    ?: throw IllegalArgumentException("Request param must have a name")
                queryParams[name] = param
            }
        }

        return Route(pathPattern, function, controller, pathParams, queryParams)
    }

    /**
     * Inicia o servidor HTTP na porta informada, criando um contexto "/" que encaminha para RequestHandler.
     */
    fun start(port: Int) {
        val server = HttpServer.create(InetSocketAddress(port), 0)
        server.createContext("/", RequestHandler(routes))
        server.executor = null
        server.start()

        // Logging unificado usando o logger
        logger.info("Server running on port $port")
        logger.info("Available endpoints:")
        routes.forEach { route ->
            logger.info("  GET ${route.pathPattern}")
        }
    }

    fun main() {
        // Exemplo de uso com um controller de demonstração
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
}