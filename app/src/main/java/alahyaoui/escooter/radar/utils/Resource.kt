package alahyaoui.escooter.radar.utils

sealed class Resource<out T> {
    class Loading<out T> : Resource<T>()
    data class Success<out T>(val data: T) : Resource<T>()
    data class Error<out T>(val throwable: Throwable?) : Resource<T>()

    companion object {
        fun <T> loading(): Resource<T> = Loading()
        fun <T> success(data: T): Resource<T> = Success(data)
        fun <T> error(throwable: Throwable? = null): Resource<T> =
            Error(throwable)
    }
}