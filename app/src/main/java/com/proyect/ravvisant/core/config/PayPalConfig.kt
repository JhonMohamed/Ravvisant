package com.proyect.ravvisant.core.config

object PayPalConfig {

    // ===== CONFIGURACIÓN DE CREDENCIALES =====

    /**
     * Client ID de PayPal
     * Obtén tu Client ID desde: https://developer.paypal.com/dashboard/
     */
    const val CLIENT_ID = "AaRafdfLxn2MV4_Hy9ocz4Ldi2HM_QEtyl1dMj_4Ggb89ZI1Ol4735G5QcXuBGJarsbucCYkV9swwukV"

    /**
     * Entorno de PayPal
     * - ENVIRONMENT_SANDBOX: Para pruebas y desarrollo
     * - ENVIRONMENT_PRODUCTION: Para producción
     */
    val ENVIRONMENT = PayPalEnvironment.CURRENT_ENVIRONMENT

    // ===== CONFIGURACIÓN DE LA APLICACIÓN =====

    /**
     * Nombre de la empresa que aparecerá en PayPal
     */
    const val MERCHANT_NAME = "Ravvisant"

    /**
     * URL de la política de privacidad
     */
    const val PRIVACY_POLICY_URL = "https://www.ravvisant.com/privacy"

    /**
     * URL de los términos de servicio
     */
    const val TERMS_OF_SERVICE_URL = "https://www.ravvisant.com/terms"

    /**
     * Moneda por defecto para los pagos
     * PayPal soporta múltiples monedas, pero USD es la más común
     */
    const val DEFAULT_CURRENCY = "USD"

    /**
     * Tipo de pago por defecto (REST API)
     * - CAPTURE: Pago inmediato
     * - AUTHORIZE: Autorización para cobro posterior
     */
    const val DEFAULT_PAYMENT_INTENT = "CAPTURE"

    // ===== VALIDACIONES =====

    /**
     * Verifica si PayPal está configurado correctamente
     */
    fun isConfigured(): Boolean {
        return CLIENT_ID != "YOUR_PAYPAL_CLIENT_ID" &&
                CLIENT_ID.isNotEmpty() &&
                CLIENT_ID.length > 10
    }

    /**
     * Obtiene información de configuración para debugging
     */
    fun getConfigInfo(): String {
        return """
            PayPal Configuration (REST API):
            - Client ID: ${if (isConfigured()) "✓ Configurado" else "✗ No configurado"}
            - Environment: ${if (PayPalEnvironment.isSandbox()) "Sandbox (Desarrollo)" else "Production"}
            - Merchant: $MERCHANT_NAME
            - Currency: $DEFAULT_CURRENCY
            - Payment Intent: $DEFAULT_PAYMENT_INTENT
        """.trimIndent()
    }
}