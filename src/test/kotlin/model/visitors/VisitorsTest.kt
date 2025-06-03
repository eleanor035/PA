package test

import model.JSONVisitor
import model.elements.*
import model.visitors.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.*

// Concrete implementation of ArrayTypeCheckVisitor for testing
class TestArrayTypeCheckVisitor : ArrayTypeCheckVisitor() {
    override fun endVisit(jsonString: JSONString) {}
    override fun endVisit(jsonBoolean: JSONBoolean) {}
    override fun endVisit(jsonNumber: JSONNumber) {}
    override fun endVisit(jsonArray: JSONArray) {}
    override fun endVisit(jsonObject: JSONObject) {}
    override fun endVisit(jsonProperty: JSONProperty) {}
    override fun endVisit(nullValue: NullValue) {}
}

// Concrete implementation of JsonValidationVisitor for testing
class TestJsonValidationVisitor : JsonValidationVisitor() {
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
    fun `should pass for array with consistent number types`() {
        val array = JSONArray(listOf(JSONNumber(1), JSONNumber(2.5), JSONNumber(3)))
        val visitor = TestArrayTypeCheckVisitor()
        array.accept(visitor)
        assertEquals(
            emptyList<String>(),
            visitor.getValidationErrors(),
            "Expected no errors for consistent number types"
        )
    }

    @Test
    fun `should report error for array with mixed string and number types`() {
        val array = JSONArray(listOf(JSONString("one"), JSONNumber(2), JSONString("three")))
        val visitor = TestArrayTypeCheckVisitor()
        array.accept(visitor)
        val errors = visitor.getValidationErrors()
        assertEquals(1, errors.size, "Expected one error for mixed types")
        assertEquals("Array contains mixed types: JSONString and JSONNumber", errors[0])
    }

    @Test
    fun `should pass for array with numbers and null values`() {
        val array = JSONArray(listOf(JSONNumber(1), NullValue, JSONNumber(2)))
        val visitor = TestArrayTypeCheckVisitor()
        array.accept(visitor)
        assertEquals(emptyList<String>(), visitor.getValidationErrors(), "Expected no errors when nulls are included")
    }

    @Test
    fun `should pass for empty array`() {
        val array = JSONArray(emptyList())
        val visitor = TestArrayTypeCheckVisitor()
        array.accept(visitor)
        assertEquals(emptyList<String>(), visitor.getValidationErrors(), "Expected no errors for empty array")
    }

    @Test
    fun `should report error for array with mixed object and array types`() {
        val array = JSONArray(listOf(JSONObject(emptyList()), JSONArray(emptyList())))
        val visitor = TestArrayTypeCheckVisitor()
        array.accept(visitor)
        val errors = visitor.getValidationErrors()
        assertEquals(1, errors.size, "Expected one error for mixed object and array types")
        assertEquals("Array contains mixed types: JSONObject and JSONArray", errors[0])
    }

    @Test
    fun `should pass for array with only null values`() {
        val array = JSONArray(listOf(NullValue, NullValue))
        val visitor = TestArrayTypeCheckVisitor()
        array.accept(visitor)
        assertEquals(emptyList<String>(), visitor.getValidationErrors(), "Expected no errors for null-only array")
    }

    @Test
    fun `should report multiple errors for nested arrays with mixed types`() {
        val nestedArray = JSONArray(
            listOf(
                JSONNumber(1),
                JSONArray(listOf(JSONString("a"), JSONBoolean(true)))
            )
        )
        val visitor = TestArrayTypeCheckVisitor()
        nestedArray.accept(visitor)
        val errors = visitor.getValidationErrors()
        assertEquals(2, errors.size, "Expected two errors for outer and inner array type mismatches")
        assertTrue(
            errors.contains("Array contains mixed types: JSONArray and JSONNumber") ||
                    errors.contains("Array contains mixed types: JSONNumber and JSONArray"),
            "Expected error for outer array type mismatch"
        )
        assertTrue(
            errors.contains("Array contains mixed types: JSONString and JSONBoolean") ||
                    errors.contains("Array contains mixed types: JSONBoolean and JSONString"),
            "Expected error for inner array type mismatch"
        )
    }
}

class JsonValidationVisitorTest {

    @Test
    fun `should pass for object with unique non-empty keys`() {
        val obj = JSONObject(listOf(
            JSONProperty("name", JSONString("Alice")),
            JSONProperty("age", JSONNumber(30))
        ))
        val visitor = TestJsonValidationVisitor()
        val errors = visitor.validate(obj)
        assertEquals(emptyList<String>(), errors, "Expected no errors for valid object")
    }

    @Test
    fun `should report error for object with empty key`() {
        val obj = JSONObject(listOf(
            JSONProperty("", JSONString("invalid")),
            JSONProperty("valid", JSONNumber(1))
        ))
        val visitor = TestJsonValidationVisitor()
        val errors = visitor.validate(obj)
        assertEquals(1, errors.size, "Expected one error for empty key")
        assertEquals("Object contains empty key at depth 0", errors[0])
    }

    @Test
    fun `should pass for empty object`() {
        val obj = JSONObject(emptyList())
        val visitor = TestJsonValidationVisitor()
        val errors = visitor.validate(obj)
        assertEquals(emptyList<String>(), errors, "Expected no errors for empty object")
    }

    @Test
    fun `should pass for non-object JSON elements`() {
        val elements = listOf(
            JSONString("test"),
            JSONNumber(42),
            JSONBoolean(true),
            NullValue,
            JSONArray(listOf(JSONNumber(1), JSONString("2")))
        )
        val visitor = TestJsonValidationVisitor()
        elements.forEach { element ->
            val errors = visitor.validate(element)
            assertEquals(emptyList<String>(), errors, "Expected no errors for non-object element ${element::class.simpleName}")
        }
    }

    @Test
    fun `should report multiple empty key errors in nested objects`() {
        val nestedObj1 = JSONObject(listOf(
            JSONProperty("", JSONString("first")), // Empty key
            JSONProperty("valid1", JSONString("ok"))
        ))
        val nestedObj2 = JSONObject(listOf(
            JSONProperty("", JSONNumber(2)), // Empty key
            JSONProperty("valid2", JSONString("ok"))
        ))
        val obj = JSONObject(listOf(
            JSONProperty("nested1", nestedObj1),
            JSONProperty("nested2", nestedObj2)
        ))
        val visitor = TestJsonValidationVisitor()
        val errors = visitor.validate(obj)
        assertEquals(2, errors.size, "Expected two errors for empty keys in nested objects")
        assertTrue(errors.contains("Object contains empty key at depth 1"), "Expected error for first nested object")
        assertTrue(errors.contains("Object contains empty key at depth 1"), "Expected error for second nested object")
    }
}