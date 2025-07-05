package com.proyect.ravvisant.core.utils

import android.content.Context
import android.util.Log
import com.proyect.ravvisant.core.config.PayPalConfig
import com.proyect.ravvisant.core.config.PayPalSecrets
import com.proyect.ravvisant.core.config.PayPalEnvironment
import java.math.BigDecimal

object PayPalUtils {

    private const val TAG = "PayPalUtils"

    /**
     * Valida si un monto es válido para PayPal
     */
    fun isValidAmount(amount: Double): Boolean {
        return amount > 0 && amount <= 10000 // Límite de $10,000 USD
    }

    /**
     * Formatea un monto para PayPal (máximo 2 decimales)
     */
    fun formatAmount(amount: Double): BigDecimal {
        return BigDecimal.valueOf(amount).setScale(2, java.math.RoundingMode.HALF_UP)
    }

    /**
     * Crea un pago de PayPal con validaciones (versión REST)
     */
    fun createPayPalPaymentRequest(
        amount: Double,
        description: String,
        currency: String = PayPalConfig.DEFAULT_CURRENCY
    ): com.proyect.ravvisant.domain.model.PaymentRequest? {

        if (!isValidAmount(amount)) {
            Log.e(TAG, "Invalid amount: $amount")
            return null
        }

        if (description.isBlank()) {
            Log.e(TAG, "Description cannot be empty")
            return null
        }

        try {
            return com.proyect.ravvisant.domain.model.PaymentRequest(
                amount = amount,
                currency = currency,
                description = description,
                orderId = generateOrderId(),
                customerName = "Cliente Ravvisant",
                customerPhone = "+51 999 999 999",
                paymentMethod = com.proyect.ravvisant.domain.model.PaymentMethod.PAYPAL
            )
        } catch (e: Exception) {
            Log.e(TAG, "Error creating PayPal payment request", e)
            return null
        }
    }

    private fun generateOrderId(): String {
        return "PAYPAL_${System.currentTimeMillis()}_${(0..999).random()}"
    }

    /**
     * Verifica si PayPal está disponible en el dispositivo
     */
    fun isPayPalAvailable(context: Context): Boolean {
        // Con la implementación REST, PayPal siempre está disponible a través del navegador
        return true
    }

    /**
     * Obtiene información de configuración para debugging
     */
    fun getDebugInfo(): String {
        return """
            PayPal Debug Info:
            ${PayPalConfig.getConfigInfo()}
            ${PayPalSecrets.getCredentialsInfo()}
            ${PayPalEnvironment.getEnvironmentInfo()}
            ${PayPalEnvironment.getSandboxInfo()}
            - Amount validation: ${isValidAmount(10.0)}
            - Amount validation: ${isValidAmount(-1.0)}
            - Amount validation: ${isValidAmount(15000.0)}
        """.trimIndent()
    }

    /**
     * Convierte moneda local a USD (ejemplo básico)
     * En producción, deberías usar un servicio de conversión de monedas
     */
    fun convertToUSD(localAmount: Double, localCurrency: String): Double {
        return when (localCurrency.uppercase()) {
            "PEN" -> localAmount * 0.27 // Tasa de cambio aproximada PEN a USD
            "EUR" -> localAmount * 1.08 // Tasa de cambio aproximada EUR a USD
            "GBP" -> localAmount * 1.26 // Tasa de cambio aproximada GBP a USD
            else -> localAmount // Asumir que ya está en USD
        }
    }

    /**
     * Valida la configuración completa de PayPal
     */
    fun validateConfiguration(): ValidationResult {
        val errors = mutableListOf<String>()
        val warnings = mutableListOf<String>()

        // Verificar Client ID
        if (!PayPalConfig.isConfigured()) {
            errors.add("PayPal Client ID no está configurado")
        }

        // Verificar Secret Key (solo para información)
        if (!PayPalSecrets.areCredentialsConfigured()) {
            warnings.add("PayPal Secret Key no está configurado (solo necesario para backend)")
        }

        // Verificar entorno
        if (PayPalEnvironment.isSandbox()) {
            warnings.add("PayPal está configurado en modo Sandbox (desarrollo)")
        }

        // Verificar URLs
        if (PayPalConfig.PRIVACY_POLICY_URL == "https://www.ravvisant.com/privacy") {
            warnings.add("URL de política de privacidad es la predeterminada")
        }

        if (PayPalConfig.TERMS_OF_SERVICE_URL == "https://www.ravvisant.com/terms") {
            warnings.add("URL de términos de servicio es la predeterminada")
        }

        return ValidationResult(
            isValid = errors.isEmpty(),
            errors = errors,
            warnings = warnings
        )
    }

    /**
     * Resultado de validación de configuración
     */
    data class ValidationResult(
        val isValid: Boolean,
        val errors: List<String>,
        val warnings: List<String>
    ) {
        fun hasErrors(): Boolean = errors.isNotEmpty()
        fun hasWarnings(): Boolean = warnings.isNotEmpty()

        fun getSummary(): String {
            return buildString {
                appendLine("PayPal Configuration Validation:")
                if (isValid) {
                    appendLine("Configuration is valid")
                } else {
                    appendLine("Configuration has errors:")
                    errors.forEach { appendLine("  - $it") }
                }
                if (warnings.isNotEmpty()) {
                    appendLine("Warnings:")
                    warnings.forEach { appendLine("  - $it") }
                }
            }
        }
    }
}