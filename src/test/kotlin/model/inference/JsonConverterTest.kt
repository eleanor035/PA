package model.inference

import model.elements.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.*
import kotlin.test.assertFailsWith

class JsonConverterTest {

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
    fun `convert boolean to JSONBoolean`() {
        val result = JsonConverter.toJsonElement(true)
        assertTrue(result is JSONBoolean)
        assertEquals(true, (result as JSONBoolean).value)
    }

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

    enum class Color { RED, GREEN, BLUE }

    @Test
    fun `convert enum to JSONString`() {
        val result = JsonConverter.toJsonElement(Color.GREEN)
        assertTrue(result is JSONString)
        assertEquals("GREEN", (result as JSONString).value)
    }

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

    class Empty

    @Test
    fun `throw exception for unsupported type`() {
        assertFailsWith<IllegalArgumentException>("Unsupported type with no accessible properties") {
            JsonConverter.toJsonElement(Empty())
        }
    }

    data class Department(val name: String, val employees: List<Person>)

    @Test
    fun `convert nested data class with list`() {
        val input = Department("Engineering", listOf(Person("Alice", 30), Person("Bob", 25)))
        val result = JsonConverter.toJsonElement(input)
        assertTrue(result is JSONObject)
        val obj = result as JSONObject

        // Verify the object has exactly 2 properties
        assertEquals(2, obj.entries.size)

        // Check the "name" property using get()
        assertEquals(JSONString("Engineering"), obj.get("name"))

        // Check the "employees" property using get() and cast to JSONArray
        val employees = obj.get("employees") as JSONArray
        assertEquals(2, employees.elements.size)

        // Verify the first employee (Alice)
        val alice = employees.elements[0] as JSONObject
        assertEquals(JSONString("Alice"), alice.get("name"))
        assertEquals(JSONNumber(30), alice.get("age"))

        // Verify the second employee (Bob)
        val bob = employees.elements[1] as JSONObject
        assertEquals(JSONString("Bob"), bob.get("name"))
        assertEquals(JSONNumber(25), bob.get("age"))
    }
}