package retanar.totp_android.data.crypto

import com.example.my.project.authenticator.otp.domain.crypto.SecretEncryptor
import javax.crypto.Cipher
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec

class AesGcmSecretEncryptor(
    private val secretKey: SecretKey,
) : SecretEncryptor {
    private val cipher: Cipher = Cipher.getInstance("AES/GCM/NoPadding")
    override val ivSize: Int
        get() = 12

    override fun encrypt(plainSecret: ByteArray, iv: ByteArray?): ByteArray {
        init(Cipher.ENCRYPT_MODE, iv!!)
        return cipher.doFinal(plainSecret)
    }

    override fun decrypt(encryptedSecret: ByteArray, iv: ByteArray?): ByteArray {
        init(Cipher.DECRYPT_MODE, iv!!)
        return cipher.doFinal(encryptedSecret)
    }

    private fun init(mode: Int, iv: ByteArray) {
        cipher.init(mode, secretKey, GCMParameterSpec(128, iv))
    }
}