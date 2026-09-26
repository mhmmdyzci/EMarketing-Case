package com.example.emarketing_case.data.api

import com.example.emarketing_case.domain.model.AppError
import com.example.emarketing_case.domain.model.AppResult
import com.google.gson.JsonIOException
import com.google.gson.JsonSyntaxException
import java.io.IOException
import java.net.SocketTimeoutException
import kotlin.coroutines.cancellation.CancellationException
import retrofit2.HttpException

internal suspend fun <T> safeApiCall(
    request: suspend () -> T,
): AppResult<T> =
    try {
        AppResult.Success(request())
    } catch (exception: CancellationException) {
        throw exception
    } catch (_: SocketTimeoutException) {
        AppResult.Failure(AppError.Timeout)
    } catch (exception: HttpException) {
        val error = when (exception.code()) {
            401 -> AppError.Unauthorized
            429, in 500..599 -> AppError.Server
            else -> AppError.Unknown
        }
        AppResult.Failure(error)
    } catch (_: JsonSyntaxException) {
        AppResult.Failure(AppError.InvalidData)
    } catch (_: JsonIOException) {
        AppResult.Failure(AppError.InvalidData)
    } catch (_: IOException) {
        AppResult.Failure(AppError.NoConnection)
    } catch (_: Exception) {
        AppResult.Failure(AppError.Unknown)
    }
