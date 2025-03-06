package com.example.my.project.authenticator.otp.data.crypto

import com.example.my.project.authenticator.otp.domain.crypto.HotpCodeGenerator
import org.apache.commons.codec.binary.Base32
import java.nio.ByteBuffer
import java.security.InvalidKeyException
import java.security.NoSuchAlgorithmException
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec
import kotlin.experimental.and
import kotlin.math.pow

class HotpCodeGeneratorImpl : HotpCodeGenerator {
    /*override fun generate(secret: ByteArray, unixTime: Duration, shaStr: String, totpVsHop: String): Int {
        val counter = when (totpVsHop) {
            "TOTP" -> unixTime.inWholeSeconds / 30 // Time-based counter for TOTP
            "HOTP" -> unixTime.inWholeSeconds    // Counter-based for HOTP
            else -> throw IllegalArgumentException("Invalid value for totpVsHop: $totpVsHop")
        }

        val counterBytes = toBigEndianBytes(counter)
        val hmac = generateHmac(secret, counterBytes, shaStr) // Unified HMAC generation logic
        return truncate(hmac)
    }*/

    override fun generate(secretKey: String, counter: Long, shaStr: String): Int {
        return try {
            // Decode the Base32-encoded secret key (RFC 4226 Standard)
            val base32 = Base32()
            val keyBytes = base32.decode(secretKey.uppercase())  // HOTP uses uppercase Base32

            // Convert counter to an 8-byte array (big-endian)
            val buffer = ByteBuffer.allocate(8)
            buffer.putLong(counter)
            val counterBytes = buffer.array()

            val algorithm = when (shaStr) {
                "SHA1" -> "HmacSHA1"
                "SHA256" -> "HmacSHA256"
                else -> throw IllegalArgumentException("Invalid value for shaStr: $shaStr")
            }

            // Create HMAC hash
            val mac = Mac.getInstance(algorithm)
            mac.init(SecretKeySpec(keyBytes, algorithm))
            val hash = mac.doFinal(counterBytes)

            // Dynamic truncation (RFC 4226)
            val offset = hash[hash.size - 1].toInt() and 0x0F
            val truncatedHash = (hash[offset].toInt() and 0x7F shl 24) or
                    (hash[offset + 1].toInt() and 0xFF shl 16) or
                    (hash[offset + 2].toInt() and 0xFF shl 8) or
                    (hash[offset + 3].toInt() and 0xFF)

            val codeLength = 6
            // Generate HOTP by taking modulo with 10^codeLength
            val hotp = truncatedHash % Math.pow(10.0, codeLength.toDouble()).toInt()

            // Format the code with leading zeros if necessary
            hotp/*.toString().padStart(codeLength, '0')*/
        } catch (e: NoSuchAlgorithmException) {e.printStackTrace()
            -1  // Return -1 in case of an error
        } catch (e: InvalidKeyException) {e.printStackTrace()
            -1  // Return -1 in case of an error
        }
    }

    private fun generateHmac(secret: ByteArray, counterBytes: ByteArray, shaStr: String): ByteArray {
        val algorithm = when (shaStr) {
            "SHA1" -> "HmacSHA1"
            "SHA256" -> "HmacSHA256"
            else -> throw IllegalArgumentException("Invalid value for shaStr: $shaStr")
        }
        val keySpec = SecretKeySpec(secret, algorithm)
        val mac = Mac.getInstance(algorithm)
        mac.init(keySpec)
        return mac.doFinal(counterBytes)
    }

    private fun toBigEndianBytes(long: Long): ByteArray {
        return ByteArray(8) { i -> (long ushr (8 * (7 - i))).toByte() }
    }

    private fun truncate(hmac: ByteArray, digits: Int = 6): Int {
        val offset = (hmac.last() and 0xf).toInt()
        val code = (hmac[offset].toUInt() and 0x7fu shl 24) or
                (hmac[offset + 1].toUInt() and 0xffu shl 16) or
                (hmac[offset + 2].toUInt() and 0xffu shl 8) or
                (hmac[offset + 3].toUInt() and 0xffu)
        return code.toInt() % (10f.pow(digits).toInt())
    }
}