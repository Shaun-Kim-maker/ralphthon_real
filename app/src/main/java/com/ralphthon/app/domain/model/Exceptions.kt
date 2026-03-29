package com.ralphthon.app.domain.model

sealed class DomainException(message: String, cause: Throwable? = null) : Exception(message, cause) {
    class NetworkException(message: String = "네트워크 오류가 발생했습니다", cause: Throwable? = null) : DomainException(message, cause)
    class ServerException(val code: Int, message: String = "서버 오류가 발생했습니다", cause: Throwable? = null) : DomainException(message, cause)
    class TimeoutException(message: String = "서버 응답 시간이 초과되었습니다", cause: Throwable? = null) : DomainException(message, cause)
    class NotFoundException(message: String = "데이터를 찾을 수 없습니다", cause: Throwable? = null) : DomainException(message, cause)
    class UnauthorizedException(message: String = "인증에 실패했습니다", cause: Throwable? = null) : DomainException(message, cause)
    class UnknownException(message: String = "알 수 없는 오류가 발생했습니다", cause: Throwable? = null) : DomainException(message, cause)
}
