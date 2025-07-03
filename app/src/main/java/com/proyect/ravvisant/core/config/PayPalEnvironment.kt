package com.proyect.ravvisant.core.config

object PayPalEnvironment {

    // Constantes para entornos
    const val ENVIRONMENT_SANDBOX = "SANDBOX"
    const val ENVIRONMENT_PRODUCTION = "PRODUCTION"

    /**
     * Entorno actual de PayPal
     *
     * - ENVIRONMENT_SANDBOX: Para desarrollo y pruebas
     * - ENVIRONMENT_PRODUCTION: Para producción
     */
    val CURRENT_ENVIRONMENT = ENVIRONMENT_SANDBOX

    /**
     * Verifica si estamos en modo sandbox
     */
    fun isSandbox(): Boolean {
        return CURRENT_ENVIRONMENT == ENVIRONMENT_SANDBOX
    }

    /**
     * Verifica si estamos en modo producción
     */
    fun isProduction(): Boolean {
        return CURRENT_ENVIRONMENT == ENVIRONMENT_PRODUCTION
    }

    /**
     * Obtiene el nombre del entorno actual
     */
    fun getEnvironmentName(): String {
        return when (CURRENT_ENVIRONMENT) {
            ENVIRONMENT_SANDBOX -> "Sandbox (Desarrollo)"
            ENVIRONMENT_PRODUCTION -> "Production (Producción)"
            else -> "Desconocido"
        }
    }

    /**
     * Obtiene información del entorno
     */
    fun getEnvironmentInfo(): String {
        return """
            PayPal Environment:
            - Entorno: ${getEnvironmentName()}
            - Es Sandbox: ${isSandbox()}
            - Es Producción: ${isProduction()}
            - Client ID configurado: ${PayPalConfig.isConfigured()}
            - Credenciales configuradas: ${PayPalSecrets.areCredentialsConfigured()}
        """.trimIndent()
    }

    /**
     * Obtiene las cuentas de prueba para sandbox
     */
    fun getSandboxAccounts(): Map<String, String> {
        return mapOf(
            "Comprador" to "sb-buyer@business.example.com",
            "Vendedor" to "sb-seller@business.example.com",
            "Contraseña" to "12345678"
        )
    }

    /**
     * Obtiene información de las cuentas de prueba
     */
    fun getSandboxInfo(): String {
        if (!isSandbox()) {
            return "No aplicable - No estás en modo Sandbox"
        }

        val accounts = getSandboxAccounts()
        return """
            Cuentas de Prueba Sandbox:
            - Comprador: ${accounts["Comprador"]}
            - Vendedor: ${accounts["Vendedor"]}
            - Contraseña: ${accounts["Contraseña"]}
            
            Instrucciones:
            1. Usa estas cuentas para probar pagos
            2. Los pagos no son reales
            3. Puedes simular diferentes escenarios
            4. Cambia a producción solo cuando estés listo
        """.trimIndent()
    }
}