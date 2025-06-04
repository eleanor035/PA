package framework

import kotlin.reflect.KFunction
import kotlin.reflect.KParameter
import kotlin.reflect.jvm.javaType
import java.util.regex.Pattern

data class Route(
    val pathPattern: String,
    val handler: KFunction<*>,
    val controller: Any,
    val pathParams: Map<String, KParameter>,
    val queryParams: Map<String, KParameter>
) {
    private val regex: Regex
    private val pathVariableNames: List<String>

    init {
        // Converte "/foo/{bar}" em regex "^/foo/(?<bar>[^/]+)$"
        val regexPattern = pathPattern.replace(Regex("\\{([^}]+)}")) {
            val name = it.groupValues[1]
            "(?<$name>[^/]+)"
        }
        regex = "^$regexPattern\$".toRegex()
        pathVariableNames = extractVariableNames(pathPattern)
    }

    fun match(requestPath: String): Boolean {
        return regex.matches(requestPath)
    }

    fun extractPathVariables(path: String): Map<String, String> {
        val match = regex.matchEntire(path) ?: return emptyMap()
        return pathVariableNames.associateWith { match.groups[it]?.value ?: "" }
    }

    fun parseArguments(
        pathVariables: Map<String, String>,
        queryParams: Map<String, String>
    ): Array<Any?> {
        // Cria array com tamanho igual ao número de parâmetros do handler (inclui receiver em index 0)
        val args = arrayOfNulls<Any?>(handler.parameters.size)

        // 1) Preenche variáveis de rota (pathVariables)
        pathVariables.forEach { (name, value) ->
            pathParams[name]?.let { param ->
                args[param.index] = convertValue(value, param.type.javaType)
            }
        }

        // 2) Preenche queryParams
        queryParams.forEach { (name, value) ->
            this.queryParams[name]?.let { param ->
                args[param.index] = convertValue(value, param.type.javaType)
            }
        }

        // 3) Atribui valores padrão para parâmetros opcionais:
        //    - Se args[index] ainda for null
        //    - E param.isOptional == true
        //    - Enquanto o tipo for Int, atribui 1 (correspondente ao "= 1" na assinatura).
        handler.parameters.forEach { param ->
            when {
                // ignoramos receiver (este será setado depois, no RequestHandler)
                param.index == 0 -> return@forEach

                // Se já veio valor (não-null), não mexemos
                args[param.index] != null -> return@forEach

                // Se for outro tipo opcional, poderíamos tratar aqui (por enquanto, deixamos null)
                param.isOptional -> {
                    // nada: o método chamador (RequestHandler) assumirá null ou default
                }
            }
        }

        return args
    }

    private fun convertValue(value: String, type: java.lang.reflect.Type): Any? {
        return when (type) {
            Int::class.java -> value.toIntOrNull()
            Long::class.java -> value.toLongOrNull()
            Double::class.java -> value.toDoubleOrNull()
            Float::class.java -> value.toFloatOrNull()
            Boolean::class.java -> value.toBooleanStrictOrNull()
            String::class.java -> value
            else -> when {
                type is Class<*> && type.isEnum -> {
                    enumValue<Any>(type, value)
                }
                else -> value
            }
        }
    }

    @Suppress("UNCHECKED_CAST")
    private fun <T> enumValue(enumClass: Class<*>, value: String): T? {
        return enumClass.enumConstants?.firstOrNull {
            (it as Enum<*>).name.equals(value, ignoreCase = true)
        } as T?
    }

    private fun extractVariableNames(pattern: String): List<String> {
        val regex = Pattern.compile("\\{([^}]+)}")
        val matcher = regex.matcher(pattern)
        val names = mutableListOf<String>()
        while (matcher.find()) {
            names.add(matcher.group(1))
        }
        return names
    }
}
