package com.example.my.project.authenticator.otp.data.crypto

import com.example.my.project.authenticator.otp.domain.crypto.TotpCodeGenerator
import org.apache.commons.codec.digest.HmacAlgorithms
import org.apache.commons.codec.digest.HmacUtils
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec
import kotlin.experimental.and
import kotlin.math.pow
import kotlin.time.Duration

/*class TotpCodeGeneratorImpl : TotpCodeGenerator {
    override fun generate(secret: ByteArray, unixTime: Duration, shaStr: String, totpVsHop: String): Int {
        val seconds = toBigEndianBytes(unixTime.inWholeSeconds / 30)
        val hmac = HmacUtils(HmacAlgorithms.HMAC_SHA_1, secret).hmac(seconds)
        return truncate(hmac)
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
}*/


class TotpCodeGeneratorImpl : TotpCodeGenerator {
    override fun generate(secret: ByteArray, unixTime: Duration, shaStr: String, totpVsHop: String): Int {
        val counter = when (totpVsHop) {
            "TOTP" -> unixTime.inWholeSeconds / 30 // Time-based counter for TOTP
            "HOTP" -> unixTime.inWholeSeconds    // Counter-based for HOTP
            else -> throw IllegalArgumentException("Invalid value for totpVsHop: $totpVsHop")
        }

        val counterBytes = toBigEndianBytes(counter)
        val hmac = generateHmac(secret, counterBytes, shaStr) // Unified HMAC generation logic
        return truncate(hmac)
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



private const val TAG = "TotpCodeGeneratorImpl"