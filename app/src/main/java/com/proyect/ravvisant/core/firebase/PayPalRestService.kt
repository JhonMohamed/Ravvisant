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
import java.util.concurrent.TimeUnit

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
        private const val GET_ACCESS_TOKEN_ENDPOINT = "/v1/oauth2/token"

        // Request codes
        const val PAYPAL_REQUEST_CODE = 123
        
        // Timeouts
        private const val CONNECT_TIMEOUT = 30L
        private const val READ_TIMEOUT = 30L
        private const val WRITE_TIMEOUT = 30L
    }

    private val client = OkHttpClient.Builder()
        .connectTimeout(CONNECT_TIMEOUT, TimeUnit.SECONDS)
        .readTimeout(READ_TIMEOUT, TimeUnit.SECONDS)
        .writeTimeout(WRITE_TIMEOUT, TimeUnit.SECONDS)
        .addInterceptor(HttpLoggingInterceptor().apply {
            level = if (PayPalEnvironment.isSandbox()) {
                HttpLoggingInterceptor.Level.BODY
            } else {
                HttpLoggingInterceptor.Level.BASIC
            }
        })
        .addInterceptor { chain ->
            val original = chain.request()
            val requestBuilder = original.newBuilder()
                .header("Accept", "application/json")
                .header("Accept-Language", "en_US")
                .method(original.method, original.body)
            
            val request = requestBuilder.build()
            Log.d(TAG, "Making request to: ${request.url}")
            chain.proceed(request)
        }
        .build()

    /**
     * Crea una orden de PayPal
     */
    suspend fun createPayPalOrder(
        paymentRequest: PaymentRequest
    ): Result<PaymentResponse> = withContext(Dispatchers.IO) {

        try {
            Log.d(TAG, "Creating PayPal order for amount: ${paymentRequest.amount} ${paymentRequest.currency}")
            
            // Validar el monto
            if (paymentRequest.amount <= 0) {
                Log.e(TAG, "Invalid amount: ${paymentRequest.amount}")
                return@withContext Result.failure(Exception("El monto debe ser mayor a 0"))
            }

            // Obtener access token
            val accessToken = getAccessToken()
            if (accessToken.isNullOrEmpty()) {
                Log.e(TAG, "Failed to get access token")
                return@withContext Result.failure(Exception("No se pudo obtener el token de acceso"))
            }

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
                        put("invoice_id", paymentRequest.orderId)
                    })
                })
                put("application_context", JSONObject().apply {
                    put("return_url", "com.proyect.ravvisant://paypal/return")
                    put("cancel_url", "com.proyect.ravvisant://paypal/cancel")
                    put("brand_name", PayPalConfig.MERCHANT_NAME)
                    put("user_action", "PAY_NOW")
                    put("shipping_preference", "NO_SHIPPING")
                })
            }

            Log.d(TAG, "Order JSON: ${orderJson.toString(2)}")

            // Crear la request
            val request = Request.Builder()
                .url("${getBaseUrl()}$CREATE_ORDER_ENDPOINT")
                .addHeader("Content-Type", "application/json")
                .addHeader("Authorization", "Bearer $accessToken")
                .post(orderJson.toString().toRequestBody("application/json".toMediaType()))
                .build()

            // Ejecutar la request
            val response = client.newCall(request).execute()
            val responseBody = response.body?.string()

            Log.d(TAG, "PayPal response code: ${response.code}")
            Log.d(TAG, "PayPal response body: $responseBody")

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
                    Log.d(TAG, "Approval URL: $approvalUrl")
                    
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
                    Log.e(TAG, "No approval URL found in response")
                    Result.failure(Exception("No se encontró URL de aprobación en la respuesta"))
                }
            } else {
                val errorMessage = try {
                    val errorJson = JSONObject(responseBody ?: "")
                    errorJson.getJSONArray("details")?.getJSONObject(0)?.getString("message") 
                        ?: "Error al crear orden de PayPal: ${response.code}"
                } catch (e: Exception) {
                    "Error al crear orden de PayPal: ${response.code}"
                }
                
                Log.e(TAG, "Error creating PayPal order: ${response.code} - $responseBody")
                Result.failure(Exception(errorMessage))
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
            Log.d(TAG, "Capturing PayPal order: $orderId")
            
            // Obtener access token
            val accessToken = getAccessToken()
            if (accessToken.isNullOrEmpty()) {
                Log.e(TAG, "Failed to get access token for capture")
                return@withContext Result.failure(Exception("No se pudo obtener el token de acceso"))
            }

            val request = Request.Builder()
                .url("${getBaseUrl()}${CAPTURE_ORDER_ENDPOINT.replace("{order_id}", orderId)}")
                .addHeader("Content-Type", "application/json")
                .addHeader("Authorization", "Bearer $accessToken")
                .post("".toRequestBody("application/json".toMediaType()))
                .build()

            val response = client.newCall(request).execute()
            val responseBody = response.body?.string()

            Log.d(TAG, "Capture response code: ${response.code}")
            Log.d(TAG, "Capture response body: $responseBody")

            if (response.isSuccessful && responseBody != null) {
                val responseJson = JSONObject(responseBody)
                val status = responseJson.getString("status")
                
                if (status == "COMPLETED") {
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
                    Log.e(TAG, "PayPal order not completed, status: $status")
                    Result.failure(Exception("La orden no se completó. Estado: $status"))
                }
            } else {
                val errorMessage = try {
                    val errorJson = JSONObject(responseBody ?: "")
                    errorJson.getJSONArray("details")?.getJSONObject(0)?.getString("message") 
                        ?: "Error al capturar orden de PayPal: ${response.code}"
                } catch (e: Exception) {
                    "Error al capturar orden de PayPal: ${response.code}"
                }
                
                Log.e(TAG, "Error capturing PayPal order: ${response.code} - $responseBody")
                Result.failure(Exception(errorMessage))
            }

        } catch (e: Exception) {
            Log.e(TAG, "Exception capturing PayPal order", e)
            Result.failure(e)
        }
    }

    /**
     * Obtiene el access token de PayPal
     */
    private suspend fun getAccessToken(): String? = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "Getting PayPal access token")
            
            val credentials = "${PayPalConfig.CLIENT_ID}:${getSecretKey()}"
            val encodedCredentials = android.util.Base64.encodeToString(
                credentials.toByteArray(), 
                android.util.Base64.NO_WRAP
            )

            val requestBody = "grant_type=client_credentials".toRequestBody(
                "application/x-www-form-urlencoded".toMediaType()
            )

            val request = Request.Builder()
                .url("${getBaseUrl()}$GET_ACCESS_TOKEN_ENDPOINT")
                .addHeader("Authorization", "Basic $encodedCredentials")
                .addHeader("Content-Type", "application/x-www-form-urlencoded")
                .post(requestBody)
                .build()

            val response = client.newCall(request).execute()
            val responseBody = response.body?.string()

            Log.d(TAG, "Access token response code: ${response.code}")

            if (response.isSuccessful && responseBody != null) {
                val jsonResponse = JSONObject(responseBody)
                val accessToken = jsonResponse.getString("access_token")
                Log.d(TAG, "Access token obtained successfully")
                accessToken
            } else {
                Log.e(TAG, "Failed to get access token: ${response.code} - $responseBody")
                null
            }

        } catch (e: Exception) {
            Log.e(TAG, "Exception getting access token", e)
            null
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
     * Obtiene la secret key (en producción esto debería venir del backend)
     */
    private fun getSecretKey(): String {
        // ⚠️ ADVERTENCIA: En producción, NUNCA incluyas el secret key en la app móvil
        // Deberías hacer las llamadas que requieren el secret key desde tu backend
        return if (PayPalEnvironment.isSandbox()) {
            "EEelf8zkwA2pvOfnHa1a81l_SVuwmkw4QgQpJsPX8YG4dlo6LcmMOUV5oBP_YexbyWUSEEV1Xb_l-E5y"
        } else {
            // En producción, esto debería ser manejado por tu backend
            throw IllegalStateException("Secret key no disponible en producción")
        }
    }

    /**
     * Abre PayPal en el navegador
     */
    fun openPayPalInBrowser(url: String): Intent {
        Log.d(TAG, "Opening PayPal URL in browser: $url")
        return Intent(Intent.ACTION_VIEW, Uri.parse(url))
    }

    /**
     * Valida la configuración de PayPal
     */
    fun validateConfiguration(): Boolean {
        val isValid = PayPalConfig.isConfigured() &&
                PayPalConfig.CLIENT_ID.isNotEmpty() &&
                getSecretKey().isNotEmpty()
        
        Log.d(TAG, "PayPal configuration validation: $isValid")
        return isValid
    }
}