package com.example.my.project.authenticator.otp.domain.entities

data class EncryptedTotpKey(
    val id: Int,
    val email: String,
    val shaStr: String,
    val issuer: String,
    val totpVsHop: String,
    val name: String,
    val category: String,
    val filePath: String,
    val secretKey: String,
    val secret: ByteArray,
    val iv: ByteArray,
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as EncryptedTotpKey

        if (id != other.id) return false
        if (name != other.name) return false
        if (!secret.contentEquals(other.secret)) return false
        if (!iv.contentEquals(other.iv)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = id
        result = 31 * result + name.hashCode()
        result = 31 * result + secret.contentHashCode()
        result = 31 * result + iv.contentHashCode()
        return result
    }
}
