package com.example.my.project.authenticator.utils

import android.net.Uri
import org.apache.commons.codec.binary.Base32
import java.nio.ByteBuffer
import java.util.concurrent.TimeUnit
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec

object TOTPGenerator {

    private const val TIME_STEP_SECONDS = 30L
    private const val TOTP_LENGTH = 6
    private const val ALGORITHM = "HmacSHA1"

    // Method to parse the TOTP URI
    fun parseTOTPURI(uriString: String): Boolean {
        val uri = Uri.parse(uriString)
        if (uri.scheme == "otpauth" && uri.host == "totp") {
            val label = uri.path?.substring(1) ?: ""  // Remove leading "/"
            val secret = uri.getQueryParameter("secret")
            val issuer = uri.getQueryParameter("issuer")

            println("Label: $label")
            println("Secret: $secret")
            println("Issuer: $issuer")

            // If secret is present, continue to decode and generate TOTP
            secret?.let { decodeAndGenerateTOTP(it) }
            return true
        } else {
            println("Invalid TOTP URI")
            return false
        }
    }

    // Method to decode Base32 secret
    fun decodeBase32Secret(secret: String): ByteArray {
        val base32 = Base32()
        return base32.decode(secret.toUpperCase())
    }

    // Method to generate TOTP based on the decoded secret
    fun generateTOTP(decodedSecret: ByteArray): String {
        val timeWindow = getCurrentTimeWindow()

        // Prepare the time window data
        val data = ByteBuffer.allocate(8).putLong(timeWindow).array()

        // Generate HMAC-SHA1 hash
        val mac = Mac.getInstance(ALGORITHM)
        mac.init(SecretKeySpec(decodedSecret, ALGORITHM))
        val hmacResult = mac.doFinal(data)

        // Extract OTP
        val offset = hmacResult.last().toInt() and 0xf
        val otp = ((hmacResult[offset].toInt() and 0x7f) shl 24 or
                (hmacResult[offset + 1].toInt() and 0xff) shl 16 or
                (hmacResult[offset + 2].toInt() and 0xff) shl 8 or
                (hmacResult[offset + 3].toInt() and 0xff))

        // Get the last 6 digits
        return (otp % 1_000_000).toString().padStart(TOTP_LENGTH, '0')
    }

    // Method to generate and display the TOTP
    private fun decodeAndGenerateTOTP(secret: String) {
        val decodedSecret = decodeBase32Secret(secret)
        val totp = generateTOTP(decodedSecret)
        println("Generated TOTP Code: $totp")
    }

    // Method to get the current time window based on the TOTP algorithm
    private fun getCurrentTimeWindow(): Long {
        val currentTimeInSeconds = TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis())
        return currentTimeInSeconds / TIME_STEP_SECONDS
    }

}