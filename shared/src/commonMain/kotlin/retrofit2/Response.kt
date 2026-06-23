package retrofit2

class Response<T> private constructor(
    private val isSuccess: Boolean,
    private val body: T?,
    private val code: Int,
) {
    val isSuccessful: Boolean get() = isSuccess
    fun body(): T? = body
    fun code(): Int = code
    fun errorBody(): T? = if (!isSuccess) body else null

    companion object {
        fun <T> success(body: T?, code: Int = 200): Response<T> = Response(true, body, code)
        fun <T> error(code: Int, errorBody: T? = null): Response<T> = Response(false, errorBody, code)
    }
}
