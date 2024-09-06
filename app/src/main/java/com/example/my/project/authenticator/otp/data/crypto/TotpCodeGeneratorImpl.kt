package retanar.totp_android.data.crypto

import org.apache.commons.codec.digest.HmacAlgorithms
import org.apache.commons.codec.digest.HmacUtils
import com.example.my.project.authenticator.otp.domain.crypto.TotpCodeGenerator
import kotlin.experimental.and
import kotlin.math.pow
import kotlin.time.Duration

class TotpCodeGeneratorImpl : TotpCodeGenerator {
    override fun generate(secret: ByteArray, unixTime: Duration): Int {
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
}