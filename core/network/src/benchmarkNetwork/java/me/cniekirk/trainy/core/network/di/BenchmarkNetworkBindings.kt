package me.cniekirk.trainy.core.network.di

import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.BindingContainer
import dev.zacsweers.metro.Provides
import dev.zacsweers.metro.SingleIn
import okhttp3.Interceptor
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Protocol
import okhttp3.Response
import okhttp3.ResponseBody.Companion.toResponseBody

@BindingContainer
object BenchmarkNetworkBindings {

    @Provides
    @SingleIn(AppScope::class)
    fun provideOkHttpClient(): OkHttpClient = OkHttpClient.Builder()
        .addInterceptor(MockRttProxyInterceptor)
        .build()
}

private object MockRttProxyInterceptor : Interceptor {
    private val contentType = "application/json".toMediaType()

    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        val path = request.url.encodedPath
        val body = when {
            request.method == "POST" && path == "/v1/client-tokens" -> clientTokenJson
            request.method == "GET" && path == "/v1/stops" -> stopsJson
            request.method == "GET" && path.startsWith("/v1/boards/") -> cachedDataJson
            request.method == "GET" && path.startsWith("/v1/services/") -> cachedDataJson
            request.method == "GET" && path == "/v1/meta" -> metaJson
            request.method == "GET" && path == "/health/live" -> healthJson
            request.method == "GET" && path == "/health/ready" -> healthJson
            else -> notFoundJson
        }
        val statusCode = if (body == notFoundJson) 404 else 200

        return Response.Builder()
            .request(request)
            .protocol(Protocol.HTTP_1_1)
            .code(statusCode)
            .message(if (statusCode == 200) "OK" else "Not Found")
            .body(body.toResponseBody(contentType))
            .build()
    }
}

private const val clientTokenJson = """
    {
      "token": "benchmark-token",
      "tokenType": "Bearer",
      "expiresAt": "2099-01-01T00:00:00Z"
    }
"""

private const val stopsJson = """
    {
      "data": [
        {
          "namespace": "benchmark",
          "description": "Benchmark Central",
          "shortCode": "BNC",
          "longCode": "BENCHMARK",
          "uniqueIdentity": "benchmark-central"
        }
      ],
      "meta": {
        "cacheStatus": "HIT",
        "count": 1
      }
    }
"""

private const val cachedDataJson = """
    {
      "data": {},
      "meta": {
        "cacheStatus": "HIT"
      }
    }
"""

private const val metaJson = """
    {
      "data": {
        "apiVersion": "benchmark",
        "rttApiVersion": "benchmark",
        "rttApiInfo": {}
      },
      "meta": {
        "cacheStatus": "HIT"
      }
    }
"""

private const val healthJson = """
    {
      "status": "ok"
    }
"""

private const val notFoundJson = """
    {
      "error": {
        "message": "No benchmark mock is registered for this request."
      }
    }
"""
