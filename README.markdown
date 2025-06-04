# JSON Library and GetJson Framework

This repository contains two Kotlin projects for the Advanced Programming course (2024/2025):
1. **JSON Library**: A library for in-memory JSON manipulation, supporting creation, filtering, mapping, validation, and serialization of JSON structures.
2. **GetJson Framework**: A lightweight HTTP/GET endpoint framework that uses the JSON library to serialize controller outputs to JSON.

## JSON Library

### Overview
The JSON library provides classes to model JSON values (`JSONObject`, `JSONArray`, `JSONString`, `JSONNumber`, `JSONBoolean`, `NullValue`) and supports:
- In-memory creation and manipulation of JSON structures.
- Non-destructive filtering and mapping operations.
- Validation of object keys and array type uniformity using a visitor pattern.
- Reflection-based conversion of Kotlin objects to JSON.
- Serialization to standard JSON strings.

### Usage
#### Creating JSON Objects
```kotlin
import model.elements.*

val obj = JSONObject(listOf(
    JSONProperty("name", JSONString("Alice")),
    JSONProperty("age", JSONNumber(30)),
    JSONProperty("scores", JSONArray(listOf(JSONNumber(95), JSONNumber(88))))
))
println(obj.serialize()) // {"name": "Alice", "age": 30, "scores": [95, 88]}
println(obj.serializePretty()) // Pretty-printed JSON
```

#### Filtering and Mapping
```kotlin
val array = JSONArray(listOf(JSONNumber(1), JSONNumber(2), JSONNumber(3)))
val filtered = array.filter { it is JSONNumber && (it as JSONNumber).value.toInt() > 1 }
println(filtered.serialize()) // [2, 3]

val mapped = array.map { JSONNumber((it as JSONNumber).value.toInt() * 2) }
println(mapped.serialize()) // [2, 4, 6]
```

#### Validation
```kotlin
import model.visitors.*

val invalidObj = JSONObject(listOf(
    JSONProperty("", JSONString("empty")), // Invalid: empty key
    JSONProperty("key", JSONString("value")),
    JSONProperty("key", JSONNumber(1)) // Invalid: duplicate key
))
val keyValidator = JsonValidationVisitor()
invalidObj.accept(keyValidator)
println(keyValidator.getValidationErrors()) // ["Object contains empty key at depth 0", "Duplicate key 'key' in object at depth 0"]

val mixedArray = JSONArray(listOf(JSONString("a"), JSONNumber(1)))
val arrayValidator = ArrayTypeCheckVisitorImpl()
mixedArray.accept(arrayValidator)
println(arrayValidator.getValidationErrors()) // ["Array contains mixed types: JSONString and JSONNumber"]
```

#### Reflection-Based Conversion
```kotlin
import model.inference.JsonConverter

data class Person(val name: String, val age: Int)
val person = Person("Alice", 30)
val json = JsonConverter.toJsonElement(person)
println(json.serialize()) // {"name": "Alice", "age": 30}

val list = listOf(1, 2, 3)
println(JsonConverter.toJsonElement(list).serialize()) // [1, 2, 3]
```

## GetJson Framework

### Overview
GetJson is a lightweight Kotlin framework for creating HTTP/GET endpoints, similar to Spring Boot. It uses annotations (`@RestController`, `@Mapping`, `@Path`, `@Param`) to define endpoints and leverages the JSON library to serialize responses.

### Usage
#### Defining a Controller
```kotlin
import framework.*

@RestController
class ExampleController {
    @Mapping("hello")
    fun helloWorld(): String = "Hello, World!"

    @Mapping("user/{id}")
    fun getUserById(@Path id: Int): Map<String, Any> {
        return mapOf("id" to id, "name" to "Leonor Pereira", "email" to "lppao@iscte-iul.pt")
    }

    @Mapping("search")
    fun search(@Param("q") query: String, @Param("page") page: Int = 1): Map<String, Any> {
        return mapOf("query" to query, "page" to page, "results" to listOf("item1", "item2", "item3"))
    }
}
```

#### Starting the Server
```kotlin
import framework.GetJson

fun main() {
    GetJson(ExampleController::class).start(8080)
    println("Server running at http://localhost:8080")
}
```

#### Available Endpoints
- `GET /hello` → `"Hello, World!"`
- `GET /user/1` → `{"id": 1, "name": "Leonor Pereira", "email": "lppao@iscte-iul.pt"}`
- `GET /search?q=xyz&page=2` → `{"query": "xyz", "page": 2, "results": ["item1", "item2", "item3"]}`

### Setup
1. **Clone the Repository**:
   ```bash
   git clone <repository-url>
   cd <repository-directory>
   ```

2. **Build the Project**:
   ```bash
   ./gradlew build
   ```

3. **Run the Server**:
   ```bash
   ./gradlew run
   ```

4. **Test Endpoints**:
   Use a tool like `curl` or a browser:
   ```bash
   curl http://localhost:8080/hello
   curl http://localhost:8080/user/1
   curl http://localhost:8080/search?q=test&page=2
   ```

### Dependencies
- Kotlin 1.9.22
- Java 21
- JUnit 5.10.0 (for testing)
- OkHttp 4.12.0 (for testing)
- Mockito 5.12.0 (for testing)

### Project Structure
- `model.elements`: JSON model classes (`JSONObject`, `JSONArray`, etc.).
- `model.inference`: JSON conversion logic (`JsonConverter`).
- `model.visitors`: Validation visitors (`JsonValidationVisitor`, `ArrayTypeCheckVisitor`).
- `framework`: GetJson core classes (`GetJson`, `RequestHandler`, `Route`, etc.).
- `controllers`: Example controllers (`ExampleController`, `UserController`).

### UML Diagram
See `src/main/kotlin/diagram.png` for the class diagram (generated with PlantUML).

### JAR Release
The compiled JAR is available in the GitHub Releases section.