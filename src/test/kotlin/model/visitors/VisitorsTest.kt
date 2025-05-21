package model.visitors

import model.elements.*
import model.visitors.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.*
import kotlin.test.assertEquals

class ConcreteArrayTypeCheckVisitor : ArrayTypeCheckVisitor() {
    override fun endVisit(jsonString: JSONString) {}
    override fun endVisit(jsonBoolean: JSONBoolean) {}
    override fun endVisit(jsonNumber: JSONNumber) {}
    override fun endVisit(jsonArray: JSONArray) {}
    override fun endVisit(jsonObject: JSONObject) {}
    override fun endVisit(jsonProperty: JSONProperty) {}
    override fun endVisit(nullValue: NullValue) {}
}

class ConcreteJsonValidationVisitor : JsonValidationVisitor() {
    override fun endVisit(jsonString: JSONString) {}
    override fun endVisit(jsonBoolean: JSONBoolean) {}
    override fun endVisit(jsonNumber: JSONNumber) {}
    override fun endVisit(jsonArray: JSONArray) {}
    override fun endVisit(jsonObject: JSONObject) {}
    override fun endVisit(jsonProperty: JSONProperty) {}
    override fun endVisit(nullValue: NullValue) {}
}

class ArrayTypeCheckVisitorTest {

    @Test
    fun `array with same type elements passes validation`() {
        val array = JSONArray(listOf(JSONNumber(1), JSONNumber(2), JSONNumber(3)))
        val visitor = ConcreteArrayTypeCheckVisitor()
        array.accept(visitor)
        assertEquals(emptyList(), visitor.getValidationErrors())
    }

    @Test
    fun `array with mixed types reports error`() {
        val array = JSONArray(listOf(JSONNumber(1), JSONString("two"), JSONNumber(3)))
        val visitor = ConcreteArrayTypeCheckVisitor()
        array.accept(visitor)
        val errors = visitor.getValidationErrors()
        assertEquals(1, errors.size)
        assertEquals("Array contains mixed types: JSONNumber and JSONString", errors[0])
    }

    @Test
    fun `array with null values and same type passes validation`() {
        val array = JSONArray(listOf(JSONNumber(1), NullValue, JSONNumber(3)))
        val visitor = ConcreteArrayTypeCheckVisitor()
        array.accept(visitor)
        assertEquals(emptyList(), visitor.getValidationErrors())
    }

    @Test
    fun `empty array passes validation`() {
        val array = JSONArray(emptyList())
        val visitor = ConcreteArrayTypeCheckVisitor()
        array.accept(visitor)
        assertEquals(emptyList(), visitor.getValidationErrors())
    }

    @Test
    fun `nested array with mixed types reports error`() {
        val nestedArray = JSONArray(listOf(JSONNumber(1), JSONArray(listOf(JSONString("a"), JSONNumber(2)))))
        val visitor = ConcreteArrayTypeCheckVisitor()
        nestedArray.accept(visitor)
        val errors = visitor.getValidationErrors()
        assertEquals(1, errors.size)
        assertEquals("Array contains mixed types: JSONNumber and JSONArray", errors[0])
    }

    @Test
    fun `array with only null values passes validation`() {
        val array = JSONArray(listOf(NullValue, NullValue))
        val visitor = ConcreteArrayTypeCheckVisitor()
        array.accept(visitor)
        assertEquals(emptyList(), visitor.getValidationErrors())
    }
}

class JsonValidationVisitorTest {

    @Test
    fun `object with unique non-empty keys passes validation`() {
        val obj = JSONObject(listOf(
            JSONProperty("name", JSONString("Alice")),
            JSONProperty("age", JSONNumber(30))
        ))
        val visitor = ConcreteJsonValidationVisitor()
        val errors = visitor.validate(obj)
        assertEquals(emptyList(), errors)
    }

    @Test
    fun `object with empty key reports error`() {
        val obj = JSONObject(listOf(
            JSONProperty("", JSONString("value")),
            JSONProperty("valid", JSONNumber(1))
        ))
        val visitor = ConcreteJsonValidationVisitor()
        val errors = visitor.validate(obj)
        assertEquals(1, errors.size)
        assertEquals("Object contains empty key at depth 0", errors[0])
    }

    @Test
    fun `object with duplicate keys reports error`() {
        val obj = JSONObject(listOf(
            JSONProperty("key", JSONString("value1")),
            JSONProperty("key", JSONNumber(1))
        ))
        val visitor = ConcreteJsonValidationVisitor()
        val errors = visitor.validate(obj)
        assertEquals(1, errors.size)
        assertEquals("Duplicate key 'key' in object at depth 0", errors[0])
    }

    @Test
    fun `empty object passes validation`() {
        val obj = JSONObject(emptyList())
        val visitor = ConcreteJsonValidationVisitor()
        val errors = visitor.validate(obj)
        assertEquals(emptyList(), errors)
    }

    @Test
    fun `nested object with duplicate keys reports error`() {
        val nestedObj = JSONObject(listOf(
            JSONProperty("name", JSONString("test")),
            JSONProperty("data", JSONObject(listOf(
                JSONProperty("id", JSONNumber(1)),
                JSONProperty("id", JSONString("duplicate"))
            )))
        ))
        val visitor = ConcreteJsonValidationVisitor()
        val errors = visitor.validate(nestedObj)
        assertEquals(1, errors.size)
        assertEquals("Duplicate key 'id' in object at depth 1", errors[0])
    }

    @Test
    fun `non-object elements pass validation`() {
        val elements = listOf(
            JSONString("test"),
            JSONNumber(42),
            JSONBoolean(true),
            NullValue,
            JSONArray(listOf(JSONNumber(1), JSONString("2")))
        )
        val visitor = ConcreteJsonValidationVisitor()
        elements.forEach { element ->
            val errors = visitor.validate(element)
            assertEquals(emptyList(), errors)
        }
    }
}