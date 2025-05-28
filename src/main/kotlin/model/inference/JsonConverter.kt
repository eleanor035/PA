package model.inference

import kotlin.reflect.KVisibility
import kotlin.reflect.full.findAnnotation
import kotlin.reflect.full.memberProperties
import model.elements.*

/**
 * Converts Kotlin objects to JSONElement instances for serialization.
 * Supports primitives, collections, enums, data classes, and custom annotations (@SerialName, @Exclude).
 * Does not support parsing; use external tools for deserialization.
 */
object JsonConverter {

    /**
     * Converts a Kotlin object to a JSONElement.
     * @param obj The object to convert (can be null).
     * @param seen A set to track objects and detect circular references.
     * @return The corresponding JSONElement.
     * @throws IllegalArgumentException if circular references or unsupported types are encountered.
     */
    fun toJsonElement(obj: Any?, seen: MutableSet<Any> = mutableSetOf()): JSONElement {
        if (obj == null) return NullValue // Corrected singleton access
        if (obj in seen) throw IllegalArgumentException("Circular reference detected for object: $obj")
        seen.add(obj)

        return try {
            when (obj) {
                is String -> JSONString(obj)
                is Number -> JSONNumber(obj)
                is Boolean -> JSONBoolean(obj)
                is List<*> -> convertList(obj, seen)
                is Map<*, *> -> convertMap(obj, seen)
                is Enum<*> -> JSONString(obj.name)
                is Array<*> -> convertList(obj.toList(), seen)
                is Set<*> -> convertList(obj.toList(), seen)
                else -> convertDataClassOrFallback(obj, seen)
            }
        } finally {
            seen.remove(obj) // Ensure removal even if an exception occurs
        }
    }

    private fun convertList(list: List<*>, seen: MutableSet<Any>): JSONArray {
        val elements = list.map { item -> toJsonElement(item, seen) }
        return JSONArray(elements).apply { elements.forEach { it.owner = this } }
    }

    private fun convertMap(map: Map<*, *>, seen: MutableSet<Any>): JSONObject {
        val entries = map.entries.map { (key, value) ->
            if (key == null) throw IllegalArgumentException("Map keys cannot be null")
            if (key !is String) throw IllegalArgumentException("Map keys must be Strings, got ${key::class.simpleName}")
            JSONProperty(key, toJsonElement(value, seen)).apply { this.value.owner = this }
        }
        return JSONObject(entries).apply { entries.forEach { it.owner = this } }
    }

    @Target(AnnotationTarget.PROPERTY)
    annotation class SerialName(val name: String)

    @Target(AnnotationTarget.PROPERTY)
    annotation class Exclude

    /**
     * Converts a data class to a JSONObject, with a fallback for non-data classes.
     * @param obj The object to convert.
     * @param seen A set to track objects and detect circular references.
     * @return A JSONObject representing the object.
     */
    private fun convertDataClassOrFallback(obj: Any, seen: MutableSet<Any>): JSONObject {
        val properties = obj::class.memberProperties
            .filter { prop ->
                prop.visibility == KVisibility.PUBLIC && prop.annotations.none { it is Exclude }
            }
            .mapNotNull { prop ->
                try {
                    val serialName = prop.findAnnotation<SerialName>()?.name ?: prop.name
                    val value = prop.getter.call(obj)
                    JSONProperty(serialName, toJsonElement(value, seen)).apply { this.value.owner = this }
                } catch (e: IllegalAccessException) {
                    throw IllegalArgumentException("Cannot access property ${prop.name} in ${obj::class.simpleName}", e)
                } catch (e: Exception) {
                    null // Skip properties that fail
                }
            }
        if (properties.isNotEmpty()) {
            return JSONObject(properties).apply { properties.forEach { it.owner = this } }
        }

        // Fallback: Treat as a map of public properties (if accessible)
        val fallbackProperties = obj::class.memberProperties
            .filter { it.visibility == KVisibility.PUBLIC }
            .mapNotNull { prop ->
                try {
                    val name = prop.name
                    val value = prop.getter.call(obj)
                    JSONProperty(name, toJsonElement(value, seen)).apply { this.value.owner = this }
                } catch (e: Exception) {
                    null // Skip inaccessible properties
                }
            }
        if (fallbackProperties.isEmpty()) {
            throw IllegalArgumentException("Unsupported type with no accessible properties: ${obj::class.simpleName}")
        }
        return JSONObject(fallbackProperties).apply { fallbackProperties.forEach { it.owner = this } }
    }
}