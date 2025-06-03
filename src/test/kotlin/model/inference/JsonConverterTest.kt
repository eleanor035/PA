package model.inference

import model.elements.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.*
import kotlin.test.assertFailsWith
import java.util.PriorityQueue

class JsonConverterTest {

    // Primitives and Null
    @Test
    fun `convert null to NullValue`() {
        val result = JsonConverter.toJsonElement(null)
        assertTrue(result is NullValue)
    }

    @Test
    fun `convert string to JSONString`() {
        val result = JsonConverter.toJsonElement("Hello")
        assertTrue(result is JSONString)
        assertEquals("Hello", (result as JSONString).value)
    }

    @Test
    fun `convert string with special characters to JSONString`() {
        val result = JsonConverter.toJsonElement("Hello\n\t\"World\"")
        assertTrue(result is JSONString)
        assertEquals("Hello\n\t\"World\"", (result as JSONString).value)
    }

    @Test
    fun `convert empty string to JSONString`() {
        val result = JsonConverter.toJsonElement("")
        assertTrue(result is JSONString)
        assertEquals("", (result as JSONString).value)
    }

    @Test
    fun `convert int to JSONNumber`() {
        val result = JsonConverter.toJsonElement(42)
        assertTrue(result is JSONNumber)
        assertEquals(42, (result as JSONNumber).value)
    }

    @Test
    fun `convert double to JSONNumber`() {
        val result = JsonConverter.toJsonElement(3.14)
        assertTrue(result is JSONNumber)
        assertEquals(3.14, (result as JSONNumber).value)
    }

    @Test
    fun `convert long to JSONNumber`() {
        val result = JsonConverter.toJsonElement(1234567890123L)
        assertTrue(result is JSONNumber)
        assertEquals(1234567890123L, (result as JSONNumber).value)
    }

    @Test
    fun `convert float to JSONNumber`() {
        val result = JsonConverter.toJsonElement(3.14f)
        assertTrue(result is JSONNumber)
        assertEquals(3.14f, (result as JSONNumber).value)
    }

    @Test
    fun `convert short to JSONNumber`() {
        val result = JsonConverter.toJsonElement(123.toShort())
        assertTrue(result is JSONNumber)
        assertEquals(123.toShort(), (result as JSONNumber).value)
    }

    @Test
    fun `convert byte to JSONNumber`() {
        val result = JsonConverter.toJsonElement(42.toByte())
        assertTrue(result is JSONNumber)
        assertEquals(42.toByte(), (result as JSONNumber).value)
    }

    @Test
    fun `convert boolean to JSONBoolean`() {
        val result = JsonConverter.toJsonElement(true)
        assertTrue(result is JSONBoolean)
        assertEquals(true, (result as JSONBoolean).value)
    }

    // Collections
    @Test
    fun `convert list of numbers to JSONArray`() {
        val result = JsonConverter.toJsonElement(listOf(1, 2, 3))
        assertTrue(result is JSONArray)
        val array = result as JSONArray
        assertEquals(3, array.elements.size)
        assertEquals(JSONNumber(1), array.elements[0])
        assertEquals(JSONNumber(2), array.elements[1])
        assertEquals(JSONNumber(3), array.elements[2])
    }

    @Test
    fun `convert empty list to JSONArray`() {
        val result = JsonConverter.toJsonElement(emptyList<Any>())
        assertTrue(result is JSONArray)
        assertEquals(0, (result as JSONArray).elements.size)
    }

    @Test
    fun `convert array to JSONArray`() {
        val result = JsonConverter.toJsonElement(arrayOf("a", "b", "c"))
        assertTrue(result is JSONArray)
        val array = result as JSONArray
        assertEquals(3, array.elements.size)
        assertEquals(JSONString("a"), array.elements[0])
        assertEquals(JSONString("b"), array.elements[1])
        assertEquals(JSONString("c"), array.elements[2])
    }

    @Test
    fun `convert set to JSONArray`() {
        val result = JsonConverter.toJsonElement(setOf(1, 2, 3))
        assertTrue(result is JSONArray)
        val array = result as JSONArray
        assertEquals(3, array.elements.size)
        assertTrue(array.elements.containsAll(listOf(JSONNumber(1), JSONNumber(2), JSONNumber(3))))
    }

    @Test
    fun `convert list with null values to JSONArray`() {
        val result = JsonConverter.toJsonElement(listOf(1, null, 3))
        assertTrue(result is JSONArray)
        val array = result as JSONArray
        assertEquals(3, array.elements.size)
        assertEquals(JSONNumber(1), array.elements[0])
        assertTrue(array.elements[1] is NullValue)
        assertEquals(JSONNumber(3), array.elements[2])
    }

    @Test
    fun `convert list with mixed types to JSONArray`() {
        val result = JsonConverter.toJsonElement(listOf<Any>(1, "text", true))
        assertTrue(result is JSONArray)
        val array = result as JSONArray
        assertEquals(3, array.elements.size)
        assertEquals(JSONNumber(1), array.elements[0])
        assertEquals(JSONString("text"), array.elements[1])
        assertEquals(JSONBoolean(true), array.elements[2])
    }

    @Test
    fun `convert large list to JSONArray`() {
        val largeList = List(1000) { it }
        val result = JsonConverter.toJsonElement(largeList)
        assertTrue(result is JSONArray)
        val array = result as JSONArray
        assertEquals(1000, array.elements.size)
        assertEquals(JSONNumber(999), array.elements[999])
    }

    @Test
    fun `convert list of only nulls to JSONArray`() {
        val result = JsonConverter.toJsonElement(listOf(null, null, null))
        assertTrue(result is JSONArray)
        val array = result as JSONArray
        assertEquals(3, array.elements.size)
        assertTrue(array.elements.all { it is NullValue })
    }

    // Maps
    @Test
    fun `convert map to JSONObject`() {
        val result = JsonConverter.toJsonElement(mapOf("key1" to 1, "key2" to "value"))
        assertTrue(result is JSONObject)
        val obj = result as JSONObject
        assertEquals(2, obj.entries.size)
        assertEquals(JSONProperty("key1", JSONNumber(1)), obj.entries[0])
        assertEquals(JSONProperty("key2", JSONString("value")), obj.entries[1])
    }

    @Test
    fun `throw exception for map with non-string keys`() {
        assertFailsWith<IllegalArgumentException>("Map keys must be Strings") {
            JsonConverter.toJsonElement(mapOf(1 to "invalid"))
        }
    }

    @Test
    fun `convert map with complex values to JSONObject`() {
        val result = JsonConverter.toJsonElement(mapOf(
            "list" to listOf(1, 2),
            "person" to Person("Alice", 30)
        ))
        assertTrue(result is JSONObject)
        val obj = result as JSONObject
        assertEquals(2, obj.entries.size)
        val listProp = obj.entries.find { it.key == "list" }?.value as JSONArray
        assertEquals(2, listProp.elements.size)
        assertEquals(JSONNumber(1), listProp.elements[0])
        val personProp = obj.entries.find { it.key == "person" }?.value as JSONObject
        assertEquals(JSONString("Alice"), personProp.get("name"))
    }

    @Test
    fun `convert map with whitespace keys to JSONObject`() {
        val result = JsonConverter.toJsonElement(mapOf("  key  " to 1, "key\n2" to "value"))
        assertTrue(result is JSONObject)
        val obj = result as JSONObject
        assertEquals(2, obj.entries.size)
        assertEquals(JSONProperty("  key  ", JSONNumber(1)), obj.entries[0])
        assertEquals(JSONProperty("key\n2", JSONString("value")), obj.entries[1])
    }

    // Enums
    enum class Color { RED, GREEN, BLUE }
    enum class SpecialEnum { `Enum@Name`, EMPTY }

    @Test
    fun `convert enum to JSONString`() {
        val result = JsonConverter.toJsonElement(Color.GREEN)
        assertTrue(result is JSONString)
        assertEquals("GREEN", (result as JSONString).value)
    }

    @Test
    fun `convert enum with special characters to JSONString`() {
        val result = JsonConverter.toJsonElement(SpecialEnum.`Enum@Name`)
        assertTrue(result is JSONString)
        assertEquals("Enum@Name", (result as JSONString).value)
    }

    // Data Classes
    data class Person(val name: String, val age: Int)

    @Test
    fun `convert data class to JSONObject`() {
        val result = JsonConverter.toJsonElement(Person("Alice", 30))
        assertTrue(result is JSONObject)
        val obj = result as JSONObject
        assertEquals(2, obj.entries.size)
        assertEquals(JSONString("Alice"), obj.get("name"))
        assertEquals(JSONNumber(30), obj.get("age"))
    }

    data class NullablePerson(val name: String?, val age: Int?)

    @Test
    fun `convert data class with nullable properties to JSONObject`() {
        val result = JsonConverter.toJsonElement(NullablePerson(null, null))
        assertTrue(result is JSONObject)
        val obj = result as JSONObject
        assertEquals(2, obj.entries.size)
        assertTrue(obj.get("name") is NullValue)
        assertTrue(obj.get("age") is NullValue)
    }

    data class Department(val name: String, val employees: List<Person>)

    @Test
    fun `convert nested data class with list`() {
        val input = Department("Engineering", listOf(Person("Alice", 30), Person("Bob", 25)))
        val result = JsonConverter.toJsonElement(input)
        assertTrue(result is JSONObject)
        val obj = result as JSONObject
        assertEquals(2, obj.entries.size)
        assertEquals(JSONString("Engineering"), obj.get("name"))
        val employees = obj.get("employees") as JSONArray
        assertEquals(2, employees.elements.size)
        val alice = employees.elements[0] as JSONObject
        assertEquals(JSONString("Alice"), alice.get("name"))
        assertEquals(JSONNumber(30), alice.get("age"))
        val bob = employees.elements[1] as JSONObject
        assertEquals(JSONString("Bob"), bob.get("name"))
        assertEquals(JSONNumber(25), bob.get("age"))
    }

    data class Company(val name: String, val departments: List<Department>)

    @Test
    fun `convert deeply nested data class to JSONObject`() {
        val input = Company("TechCorp", listOf(
            Department("Engineering", listOf(Person("Alice", 30))),
            Department("HR", listOf(Person("Bob", 25)))
        ))
        val result = JsonConverter.toJsonElement(input)
        assertTrue(result is JSONObject)
        val obj = result as JSONObject
        assertEquals(2, obj.entries.size)
        assertEquals(JSONString("TechCorp"), obj.get("name"))
        val depts = obj.get("departments") as JSONArray
        assertEquals(2, depts.elements.size)
        val eng = depts.elements[0] as JSONObject
        assertEquals(JSONString("Engineering"), eng.get("name"))
        val hr = depts.elements[1] as JSONObject
        assertEquals(JSONString("HR"), hr.get("name"))
    }

    data class MinimalDataClass(val dummy: Int = 0)

    @Test
    fun `convert minimal data class to JSONObject`() {
        val result = JsonConverter.toJsonElement(MinimalDataClass())
        assertTrue(result is JSONObject)
        val obj = result as JSONObject
        assertEquals(1, obj.entries.size)
        assertEquals(JSONProperty("dummy", JSONNumber(0)), obj.entries[0])
    }

    // Annotations
    data class User(@JsonConverter.SerialName("full_name") val name: String)

    @Test
    fun `respect SerialName annotation`() {
        val result = JsonConverter.toJsonElement(User("Bob"))
        assertTrue(result is JSONObject)
        val obj = result as JSONObject
        assertEquals(1, obj.entries.size)
        assertEquals(JSONProperty("full_name", JSONString("Bob")), obj.entries[0])
    }

    data class Account(val id: Int, @JsonConverter.Exclude val password: String)

    @Test
    fun `respect Exclude annotation`() {
        val result = JsonConverter.toJsonElement(Account(123, "secret"))
        assertTrue(result is JSONObject)
        val obj = result as JSONObject
        assertEquals(1, obj.entries.size)
        assertEquals(JSONProperty("id", JSONNumber(123)), obj.entries[0])
    }

    data class EdgeCaseUser(@JsonConverter.SerialName("") val name: String)

    @Test
    fun `handle empty SerialName annotation`() {
        val result = JsonConverter.toJsonElement(EdgeCaseUser("Bob"))
        assertTrue(result is JSONObject)
        val obj = result as JSONObject
        assertEquals(1, obj.entries.size)
        assertEquals(JSONProperty("name", JSONString("Bob")), obj.entries[0]) // Fallback to property name
    }

    // Error Conditions
    class A(var b: B? = null)
    class B(var a: A? = null)

    @Test
    fun `detect circular reference`() {
        val a = A()
        val b = B(a)
        a.b = b
        assertFailsWith<IllegalArgumentException>("Circular reference detected") {
            JsonConverter.toJsonElement(a)
        }
    }

    class C(var d: D? = null)
    class D(var e: E? = null)
    class E(var c: C? = null)

    @Test
    fun `detect deep circular reference`() {
        val c = C()
        val d = D()
        val e = E(c)
        c.d = d
        d.e = e
        assertFailsWith<IllegalArgumentException>("Circular reference detected") {
            JsonConverter.toJsonElement(c)
        }
    }

    class Empty

    @Test
    fun `throw exception for unsupported type`() {
        assertFailsWith<IllegalArgumentException>("Unsupported type with no accessible properties") {
            JsonConverter.toJsonElement(Empty())
        }
    }

    class NonDataClass(val id: Int)

    @Test
    fun `throw exception for non-data class with properties`() {
        assertFailsWith<IllegalArgumentException> {
            JsonConverter.toJsonElement(NonDataClass(42))
        }
    }

    @Test
    fun `throw exception for unsupported collection type`() {
        val queue = PriorityQueue(listOf(1, 2, 3))
        assertFailsWith<IllegalArgumentException> {
            JsonConverter.toJsonElement(queue)
        }
    }
}