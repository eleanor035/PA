package model.inference

import model.elements.*
import kotlin.reflect.full.findAnnotation
import kotlin.reflect.full.primaryConstructor
import kotlin.reflect.full.memberProperties
import kotlin.reflect.jvm.javaGetter
import java.lang.reflect.Method
import kotlin.reflect.KParameter

object JsonConverter {
    @Retention(AnnotationRetention.RUNTIME)
    annotation class SerialName(val value: String)

    @Retention(AnnotationRetention.RUNTIME)
    annotation class Exclude

    private val processedObjects = mutableSetOf<Any>()

    fun toJsonElement(value: Any?): JSONElement {
        if (value == null) return NullValue
        if (value in processedObjects) throw IllegalArgumentException("Circular reference detected")
        processedObjects.add(value)

        try {
            return when (value) {
                is String -> JSONString(value)
                is Int, is Double, is Long, is Float, is Short, is Byte -> JSONNumber(value as Number)
                is Boolean -> JSONBoolean(value)
                is List<*> -> JSONArray(value.map { toJsonElement(it) })
                is Array<*> -> JSONArray(value.map { toJsonElement(it) })
                is Set<*> -> JSONArray(value.map { toJsonElement(it) })
                is Map<*, *> -> {
                    if (value.keys.any { it !is String }) {
                        throw IllegalArgumentException("Map keys must be Strings")
                    }
                    JSONObject(value.entries.map { (k, v) ->
                        JSONProperty(k as String, toJsonElement(v))
                    })
                }
                is Enum<*> -> JSONString(value.name)
                is Collection<*> -> throw IllegalArgumentException("Unsupported collection type: ${value::class.simpleName}")
                else -> {
                    if (value::class.isData) {
                        convertDataClass(value)
                    } else {
                        throw IllegalArgumentException("Unsupported type: ${value::class.simpleName}")
                    }
                }
            }
        } finally {
            processedObjects.remove(value)
        }
    }

    private fun convertDataClass(value: Any): JSONObject {
        val kClass = value::class
        if (!kClass.isData) throw IllegalArgumentException("Only data classes are supported")

        val constructor = kClass.primaryConstructor
            ?: throw IllegalArgumentException("Data class must have a primary constructor")

        val entries = constructor.parameters.mapNotNull { param ->
            val prop = kClass.memberProperties.find { it.name == param.name }
                ?: throw IllegalArgumentException("Property ${param.name} not found")

            // Check annotations on the constructor parameter
            val serialNameAnn = param.findAnnotation<SerialName>()
            val excludeAnn = param.findAnnotation<Exclude>()
            if (excludeAnn != null) return@mapNotNull null

            // Get serial name, fallback to parameter name if empty or blank
            val serialName = serialNameAnn?.value?.takeIf { it.isNotBlank() } ?: param.name
            ?: throw IllegalArgumentException("Parameter name missing")

            // Get property value using getter
            val getter = prop.javaGetter
                ?: throw IllegalArgumentException("Getter for ${param.name} not found")
            val propValue = getter.invoke(value)
            JSONProperty(serialName, toJsonElement(propValue))
        }

        return JSONObject(entries)
    }
}