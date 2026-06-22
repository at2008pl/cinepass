package retrofit2

class Response<T> private constructor(
    private val isSuccess: Boolean,
    private val body: T?,
    private val code: Int
) {
    val isSuccessful: Boolean get() = isSuccess
    fun body(): T? = body
    fun code(): Int = code

    companion object {
        fun <T> success(body: T?): Response<T> = Response(true, body, 200)
        fun <T> error(code: Int, errorBody: Any? = null): Response<T> = Response(false, null, code)
    }
}
