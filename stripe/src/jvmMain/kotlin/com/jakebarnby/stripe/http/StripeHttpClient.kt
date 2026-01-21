package com.jakebarnby.stripe.http

import com.jakebarnby.stripe.model.StripeException
import io.ktor.client.*
import io.ktor.client.engine.okhttp.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonPrimitive

/**
 * HTTP client for making direct Stripe API calls with publishable keys.
 *
 * This is used for client-side operations (tokens, payment methods, sources)
 * because stripe-java SDK doesn't support publishable keys - it's server-side only.
 */
internal class StripeHttpClient(
    private val publishableKey: String
) {
    private val httpClient = HttpClient(OkHttp)

    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
    }

    private val baseUrl = "https://api.stripe.com/v1"

    /**
     * POST request to Stripe API with form data
     */
    suspend fun post(
        endpoint: String,
        params: Map<String, Any>,
        idempotencyKey: String? = null
    ): JsonObject {
        return try {
            val response: HttpResponse = httpClient.post("$baseUrl/$endpoint") {
                header("Authorization", "Bearer $publishableKey")
                idempotencyKey?.let { header("Idempotency-Key", it) }
                contentType(ContentType.Application.FormUrlEncoded)

                // Convert nested maps to form-encoded format
                val formParams = flattenParams(params)
                setBody(formParams.formUrlEncode())
            }

            val bodyText = response.bodyAsText()

            if (response.status.isSuccess()) {
                try {
                    json.parseToJsonElement(bodyText) as JsonObject
                } catch (e: Exception) {
                    throw StripeException(
                        "Failed to parse Stripe API response: ${e.message}. Body: ${bodyText.take(500)}",
                        cause = e
                    )
                }
            } else {
                val error = try {
                    json.parseToJsonElement(bodyText) as? JsonObject
                } catch (e: Exception) {
                    null
                }

                val errorMessage = error
                    ?.get("error")?.let { it as? JsonObject }
                    ?.get("message")?.jsonPrimitive?.content
                    ?: "HTTP ${response.status.value}: ${response.status.description}. Response: ${bodyText.take(500)}"

                throw StripeException(errorMessage)
            }
        } catch (e: StripeException) {
            throw e
        } catch (e: Exception) {
            throw StripeException("Network error during API call to $endpoint: ${e.message}", cause = e)
        }
    }

    /**
     * Flatten nested parameter maps into form-encoded format
     * e.g., map["card"]["number"] becomes "card[number]"
     */
    private fun flattenParams(
        params: Map<String, Any>,
        prefix: String = ""
    ): Map<String, String> {
        val result = mutableMapOf<String, String>()

        params.forEach { (key, value) ->
            val fullKey = if (prefix.isEmpty()) key else "$prefix[$key]"

            when (value) {
                is Map<*, *> -> {
                    @Suppress("UNCHECKED_CAST")
                    result.putAll(flattenParams(value as Map<String, Any>, fullKey))
                }
                else -> {
                    result[fullKey] = value.toString()
                }
            }
        }

        return result
    }

    private fun Map<String, String>.formUrlEncode(): String {
        return entries.joinToString("&") { (key, value) ->
            "${key.encodeURLParameter()}=${value.encodeURLParameter()}"
        }
    }

    fun close() {
        httpClient.close()
    }
}
