package framework

open class HttpException(
    val status: Int,
    message: String,
    cause: Throwable? = null
) : RuntimeException(message, cause)

class BadRequestException(message: String) : HttpException(400, message)
class NotFoundException(message: String) : HttpException(404, message)
class MethodNotAllowedException(message: String) : HttpException(405, message)
class InternalServerErrorException(message: String) : HttpException(500, message)