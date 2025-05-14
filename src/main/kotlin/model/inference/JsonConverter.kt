package model.inference

import kotlin.reflect.KVisibility
import kotlin.reflect.full.isData
import kotlin.reflect.full.memberProperties
import model.elements.*

object JsonConverter {

    fun toJsonElement(obj: Any?, seen: MutableSet<Any> = mutableSetOf()): JSONElement {
        if (obj == null) return NullValue()
        if (seen.contains(obj)) throw IllegalArgumentException("Circular reference detected")
        seen.add(obj)

        return when (obj) {
            is String -> JSONString(obj)
            is Number -> JSONNumber(obj)
            is Boolean -> JSONBoolean(obj)
            is List<*> -> convertList(obj, seen)
            is Map<*, *> -> convertMap(obj, seen)
            is Enum<*> -> JSONString(obj.name)
            is Array<*> -> convertList(obj.toList(), seen)
            is Set<*> -> convertList(obj.toList(), seen)
            else -> {
                if (obj::class.isData) {
                    convertDataClass(obj, seen)
                } else {
                    throw IllegalArgumentException("Unsupported type: ${obj::class.simpleName}")
                }
            }
        }.also { seen.remove(obj) }
    }

    private fun convertList(list: List<*>, seen: MutableSet<Any>): JSONArray {
        val elements = list.map { item -> toJsonElement(item, seen) }
        return JSONArray(elements)
    }

    private fun convertMap(map: Map<*, *>, seen: MutableSet<Any>): JSONObject {
        val entries = map.entries.map { (key, value) ->
            if (key !is String) throw IllegalArgumentException("Map keys must be Strings")
            JSONProperty(key, toJsonElement(value, seen))
        }
        return JSONObject(entries)
    }

    private fun convertDataClass(obj: Any, seen: MutableSet<Any>): JSONObject {
        val properties = obj::class.memberProperties
            .filter { it.visibility == KVisibility.PUBLIC }
            .mapNotNull { prop ->
                try {
                    val value = prop.call(obj)
                    JSONProperty(prop.name, toJsonElement(value, seen))
                } catch (e: Exception) {
                    null
                }
            }
        return JSONObject(properties)
    }
}