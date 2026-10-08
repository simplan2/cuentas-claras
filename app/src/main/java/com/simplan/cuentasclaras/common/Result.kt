package com.simplan.cuentasclaras.common

sealed interface Result<out T> {
    data class Success<out T>(val value: T) : Result<T>
    data class Error(val exception: Throwable, val message: String? = null) : Result<Nothing>

    companion object {
        fun <T> success(value: T): Result<T> = Success(value)
        fun <T> error(exception: Throwable, message: String? = null): Result<T> = Error(exception, message)
        fun <T> error(message: String): Result<T> = Error(Exception(message), message)
    }

    fun isSuccess(): Boolean = this is Success<*>
    fun isError(): Boolean = this is Error

    fun getOrNull(): T? = when (this) {
        is Success -> value
        is Error -> null
    }

    fun getOrElse(default: () -> @UnsafeVariance T): T = when (this) {
        is Success -> value
        is Error -> default()
    }

    fun getOrThrow(): T = when (this) {
        is Success -> value
        is Error -> throw exception
    }

    fun <R> map(transform: (T) -> R): Result<R> = when (this) {
        is Success -> Success(transform(value))
        is Error -> this
    }

    fun <R> flatMap(transform: (T) -> Result<R>): Result<R> = when (this) {
        is Success -> transform(value)
        is Error -> this
    }

    fun onSuccess(action: (T) -> Unit): Result<T> = when (this) {
        is Success -> { action(value); this }
        is Error -> this
    }

    fun onError(action: (Throwable) -> Unit): Result<T> = when (this) {
        is Success -> this
        is Error -> { action(exception); this }
    }

    val errorMessage: String?
        get() = when (this) {
            is Error -> message ?: exception.message
            is Success -> null
        }
}