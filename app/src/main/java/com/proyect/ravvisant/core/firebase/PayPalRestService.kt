package com.proyect.ravvisant.core.firebase

import android.content.Intent
import android.net.Uri
import android.util.Log
import com.proyect.ravvisant.core.config.PayPalConfig
import com.proyect.ravvisant.core.config.PayPalEnvironment
import com.proyect.ravvisant.domain.model.PaymentRequest
import com.proyect.ravvisant.domain.model.PaymentResponse
import com.proyect.ravvisant.domain.model.PaymentStatus
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.logging.HttpLoggingInterceptor
import org.json.JSONArray
import org.json.JSONObject
import java.io.IOException

/**
 * Servicio de PayPal usando la API REST
 * Esta implementación es más moderna y compatible con versiones recientes de Android
 */
class PayPalRestService {

    companion object {
        private const val TAG = "PayPalRestService"

        // URLs de PayPal
        private const val SANDBOX_BASE_URL = "https://api-m.sandbox.paypal.com"
        private const val PRODUCTION_BASE_URL = "https://api-m.paypal.com"

        // Endpoints
        private const val CREATE_ORDER_ENDPOINT = "/v2/checkout/orders"
        private const val CAPTURE_ORDER_ENDPOINT = "/v2/checkout/orders/{order_id}/capture"

        // Request codes
        const val PAYPAL_REQUEST_CODE = 123
    }

    private val client = OkHttpClient.Builder()
        .addInterceptor(HttpLoggingInterceptor().apply {
            level = if (PayPalEnvironment.isSandbox()) {
                HttpLoggingInterceptor.Level.BODY
            } else {
                HttpLoggingInterceptor.Level.BASIC
            }
        })
        .build()

    /**
     * Crea una orden de PayPal
     */
    suspend fun createPayPalOrder(
        paymentRequest: PaymentRequest
    ): Result<PaymentResponse> = withContext(Dispatchers.IO) {

        try {
            // Crear el JSON para la orden
            val orderJson = JSONObject().apply {
                put("intent", "CAPTURE")
                put("purchase_units", JSONArray().apply {
                    put(JSONObject().apply {
                        put("amount", JSONObject().apply {
                            put("currency_code", PayPalConfig.DEFAULT_CURRENCY)
                            put("value", String.format("%.2f", paymentRequest.amount))
                        })
                        put("description", paymentRequest.description)
                        put("custom_id", paymentRequest.orderId)
                    })
                })
                put("application_context", JSONObject().apply {
                    put("return_url", "com.proyect.ravvisant://paypal/return")
                    put("cancel_url", "com.proyect.ravvisant://paypal/cancel")
                    put("brand_name", PayPalConfig.MERCHANT_NAME)
                    put("user_action", "PAY_NOW")
                })
            }

            // Crear la request
            val request = Request.Builder()
                .url("${getBaseUrl()}$CREATE_ORDER_ENDPOINT")
                .addHeader("Content-Type", "application/json")
                .addHeader("Authorization", "Bearer ${getAccessToken()}")
                .post(orderJson.toString().toRequestBody("application/json".toMediaType()))
                .build()

            // Ejecutar la request
            val response = client.newCall(request).execute()
            val responseBody = response.body?.string()

            if (response.isSuccessful && responseBody != null) {
                val responseJson = JSONObject(responseBody)
                val orderId = responseJson.getString("id")
                val links = responseJson.getJSONArray("links")

                // Buscar el link de aprobación
                var approvalUrl = ""
                for (i in 0 until links.length()) {
                    val link = links.getJSONObject(i)
                    if (link.getString("rel") == "approve") {
                        approvalUrl = link.getString("href")
                        break
                    }
                }

                if (approvalUrl.isNotEmpty()) {
                    Log.d(TAG, "PayPal order created successfully: $orderId")
                    Result.success(
                        PaymentResponse(
                            success = true,
                            transactionId = orderId,
                            qrCodeUrl = null,
                            paymentUrl = approvalUrl,
                            message = "Orden de PayPal creada exitosamente",
                            status = PaymentStatus.PENDING
                        )
                    )
                } else {
                    Result.failure(Exception("No se encontró URL de aprobación"))
                }
            } else {
                Log.e(TAG, "Error creating PayPal order: ${response.code} - $responseBody")
                Result.failure(Exception("Error al crear orden de PayPal: ${response.code}"))
            }

        } catch (e: Exception) {
            Log.e(TAG, "Exception creating PayPal order", e)
            Result.failure(e)
        }
    }

    /**
     * Captura una orden de PayPal
     */
    suspend fun capturePayPalOrder(orderId: String): Result<PaymentResponse> = withContext(Dispatchers.IO) {

        try {
            val request = Request.Builder()
                .url("${getBaseUrl()}${CAPTURE_ORDER_ENDPOINT.replace("{order_id}", orderId)}")
                .addHeader("Content-Type", "application/json")
                .addHeader("Authorization", "Bearer ${getAccessToken()}")
                .post("".toRequestBody("application/json".toMediaType()))
                .build()

            val response = client.newCall(request).execute()
            val responseBody = response.body?.string()

            if (response.isSuccessful && responseBody != null) {
                val responseJson = JSONObject(responseBody)
                val captureId = responseJson.getJSONArray("purchase_units")
                    .getJSONObject(0)
                    .getJSONArray("payments")
                    .getJSONObject(0)
                    .getJSONArray("captures")
                    .getJSONObject(0)
                    .getString("id")

                Log.d(TAG, "PayPal order captured successfully: $captureId")
                Result.success(
                    PaymentResponse(
                        success = true,
                        transactionId = captureId,
                        qrCodeUrl = null,
                        paymentUrl = null,
                        message = "Pago capturado exitosamente",
                        status = PaymentStatus.COMPLETED
                    )
                )
            } else {
                Log.e(TAG, "Error capturing PayPal order: ${response.code} - $responseBody")
                Result.failure(Exception("Error al capturar orden de PayPal: ${response.code}"))
            }

        } catch (e: Exception) {
            Log.e(TAG, "Exception capturing PayPal order", e)
            Result.failure(e)
        }
    }

    /**
     * Obtiene el token de acceso de PayPal
     */
    private suspend fun getAccessToken(): String = withContext(Dispatchers.IO) {
        try {
            val credentials = "${PayPalConfig.CLIENT_ID}:${getSecretKey()}"
            val encodedCredentials = android.util.Base64.encodeToString(
                credentials.toByteArray(),
                android.util.Base64.NO_WRAP
            )

            val request = Request.Builder()
                .url("${getBaseUrl()}/v1/oauth2/token")
                .addHeader("Authorization", "Basic $encodedCredentials")
                .addHeader("Content-Type", "application/x-www-form-urlencoded")
                .post("grant_type=client_credentials".toRequestBody("application/x-www-form-urlencoded".toMediaType()))
                .build()

            val response = client.newCall(request).execute()
            val responseBody = response.body?.string()

            if (response.isSuccessful && responseBody != null) {
                val responseJson = JSONObject(responseBody)
                responseJson.getString("access_token")
            } else {
                throw Exception("Error obteniendo token de acceso: ${response.code}")
            }

        } catch (e: Exception) {
            Log.e(TAG, "Error getting access token", e)
            throw e
        }
    }

    /**
     * Obtiene la URL base según el entorno
     */
    private fun getBaseUrl(): String {
        return if (PayPalEnvironment.isSandbox()) {
            SANDBOX_BASE_URL
        } else {
            PRODUCTION_BASE_URL
        }
    }

    /**
     * Obtiene el Secret Key (en producción esto debería venir de un lugar seguro)
     */
    private fun getSecretKey(): String {
        // En producción, esto debería venir de variables de entorno o un lugar seguro
        return "EEelf8zkwA2pvOfnHa1a81l_SVuwmkw4QgQpJsPX8YG4dlo6LcmMOUV5oBP_YexbyWUSEEV1Xb_l-E5y"
    }

    /**
     * Abre PayPal en el navegador
     */
    fun openPayPalInBrowser(url: String): Intent {
        return Intent(Intent.ACTION_VIEW, Uri.parse(url))
    }

    /**
     * Valida la configuración de PayPal
     */
    fun validateConfiguration(): Boolean {
        return PayPalConfig.isConfigured() &&
                PayPalConfig.CLIENT_ID.isNotEmpty() &&
                getSecretKey().isNotEmpty()
    }
}