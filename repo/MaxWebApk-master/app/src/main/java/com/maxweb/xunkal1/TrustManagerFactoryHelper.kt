package com.maxweb.xunkal1

import android.util.Base64
import android.util.Log
import java.io.ByteArrayInputStream
import java.security.KeyStore
import java.security.cert.CertificateFactory
import java.security.cert.X509Certificate
import javax.net.ssl.SSLContext
import javax.net.ssl.SSLSocketFactory
import javax.net.ssl.TrustManager
import javax.net.ssl.TrustManagerFactory
import javax.net.ssl.X509TrustManager

object TrustManagerFactoryHelper {

    private const val TAG = "TrustManagerFactoryHelper"
    private const val CERT_ALIAS = "russian_trusted_root_ca"

    /**
     * Создаёт X509TrustManager, который доверяет системным CA
     * плюс встроенному сертификату Russian Trusted Root CA (Минцифры).
     *
     * Если PEM-строка пустая или не валидна — логирует предупреждение
     * и возвращает только системный X509TrustManager.
     */
    fun createTrustManager(): X509TrustManager {
        // Загружаем системные CA через стандартный TrustManagerFactory
        val systemTmf = TrustManagerFactory.getInstance("X509").apply {
            init(null as KeyStore?)
        }
        val systemTrustManagers = systemTmf.getTrustManagers()
        if (systemTrustManagers.isEmpty()) {
            Log.w(TAG, "Системные trust managers не загружены")
            return object : X509TrustManager {
                override fun getAcceptedIssuers(): Array<X509Certificate> = emptyArray()
                override fun checkClientTrusted(chain: Array<X509Certificate>?, authType: String?) {}
                override fun checkServerTrusted(chain: Array<X509Certificate>?, authType: String?) {}
            }
        }

        val pem = MainActivity.RUSSIAN_TRUSTED_ROOT_CA_PEM
        if (pem.isBlank()) {
            Log.w(TAG, "RUSSIAN_TRUSTED_ROOT_CA_PEM пуст — используется только системное хранилище")
            return systemTrustManagers.first { it is X509TrustManager } as X509TrustManager
        }

        // Извлекаем Base64-строку между BEGIN/END
        val base64Block = pem
            .substringAfter("-----BEGIN CERTIFICATE-----", "")
            .substringBefore("-----END CERTIFICATE-----", "")
            .replace(Regex("\\s"), "")

        if (base64Block.isEmpty()) {
            Log.w(TAG, "PEM-сертификат не содержит Base64-данных — используется только системное хранилище")
            return systemTrustManagers.first { it is X509TrustManager } as X509TrustManager
        }

        val decoded: ByteArray = try {
            Base64.decode(base64Block.toByteArray(), Base64.NO_WRAP)
        } catch (e: IllegalArgumentException) {
            Log.e(TAG, "Ошибка декодирования Base64 PEM: ${e.message}")
            return systemTrustManagers.first { it is X509TrustManager } as X509TrustManager
        }

        val certificate = try {
            val cf = CertificateFactory.getInstance("X.509")
            cf.generateCertificate(ByteArrayInputStream(decoded))
        } catch (e: Exception) {
            Log.e(TAG, "Ошибка парсинга X.509 сертификата: ${e.message}")
            return systemTrustManagers.first { it is X509TrustManager } as X509TrustManager
        }

        // Помещаем сертификат в KeyStore и инициализируем TrustManagerFactory
        val ks = KeyStore.getInstance(KeyStore.getDefaultType()).apply {
            load(null, null)
            setCertificateEntry(CERT_ALIAS, certificate)
        }

        val customTmf = TrustManagerFactory.getInstance("X509").apply {
            init(ks)
        }

        val customTrustManagers = customTmf.getTrustManagers()
        if (customTrustManagers.isEmpty()) {
            Log.w(TAG, "Кастомные trust managers не загружены — возврат системного")
            return systemTrustManagers.first { it is X509TrustManager } as X509TrustManager
        }

        return customTrustManagers.first { it is X509TrustManager } as X509TrustManager
    }

    /**
     * Создаёт SSLSocketFactory с указанным trustManager.
     */
    fun createSslSocketFactory(trustManager: X509TrustManager): SSLSocketFactory {
        val sslContext = SSLContext.getInstance("TLS").apply {
            init(null, arrayOf<TrustManager>(trustManager), null)
        }
        return sslContext.socketFactory
    }
}