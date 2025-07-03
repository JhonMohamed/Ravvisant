package com.proyect.ravvisant.core.config

object PayPalSecrets {

    /**
     * Secret Key de PayPal (SOLO PARA REFERENCIA)
     *
     * ⚠️ ADVERTENCIA:
     * - NO uses este secret key en la aplicación móvil
     * - Solo se debe usar en el backend/servidor
     * - Si necesitas hacer llamadas que requieran el secret key,
     *   hazlas desde tu servidor, no desde la app
     */
    const val SECRET_KEY = "EEelf8zkwA2pvOfnHa1a81l_SVuwmkw4QgQpJsPX8YG4dlo6LcmMOUV5oBP_YexbyWUSEEV1Xb_l-E5y"

    /**
     * Verifica si las credenciales están configuradas
     */
    fun areCredentialsConfigured(): Boolean {
        return CLIENT_ID.isNotEmpty() &&
                CLIENT_ID != "YOUR_PAYPAL_CLIENT_ID" &&
                SECRET_KEY.isNotEmpty() &&
                SECRET_KEY != "YOUR_PAYPAL_SECRET_KEY"
    }

    /**
     * Obtiene información de las credenciales (sin mostrar el secret completo)
     */
    fun getCredentialsInfo(): String {
        val clientIdMasked = if (CLIENT_ID.length > 10) {
            "${CLIENT_ID.take(10)}...${CLIENT_ID.takeLast(10)}"
        } else {
            "No configurado"
        }

        val secretKeyMasked = if (SECRET_KEY.length > 10) {
            "${SECRET_KEY.take(10)}...${SECRET_KEY.takeLast(10)}"
        } else {
            "No configurado"
        }

        return """
            PayPal Credentials Info:
            - Client ID: $clientIdMasked
            - Secret Key: $secretKeyMasked (solo para backend)
            - Configurado: ${areCredentialsConfigured()}
        """.trimIndent()
    }

    // Referencia al Client ID desde PayPalConfig
    private val CLIENT_ID: String
        get() = PayPalConfig.CLIENT_ID
}