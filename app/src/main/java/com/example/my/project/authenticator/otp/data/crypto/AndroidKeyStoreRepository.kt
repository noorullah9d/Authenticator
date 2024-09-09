package com.example.my.project.authenticator.otp.data.crypto

import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import com.example.my.project.authenticator.otp.domain.crypto.SecretKeyRepository
import java.security.KeyStore
import java.security.KeyStore.SecretKeyEntry
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey

class AndroidKeyStoreRepository : SecretKeyRepository {
    private val keyStore = KeyStore.getInstance("AndroidKeyStore").apply { load(null) }

    override fun generateRandomKey(alias: String): SecretKey {
        val keyGenerator = KeyGenerator.getInstance("AES", "AndroidKeyStore")
        keyGenerator.init(
            KeyGenParameterSpec.Builder(
                alias,
                KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
            )
                .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                .setKeySize(256)
                // false to use custom IV
                .setRandomizedEncryptionRequired(false)
                .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                .build()
        )
        return keyGenerator.generateKey()
    }

    override fun getKey(alias: String): SecretKey? {
        if (!keyStore.containsAlias(alias)) return null
        val entry = keyStore.getEntry(alias, null) as SecretKeyEntry
        return entry.secretKey
    }

    override fun deleteKey(alias: String) {
        keyStore.deleteEntry(alias)
    }

    override fun getAliases(): List<String> {
        return keyStore.aliases().toList()
    }

    companion object {
        const val TOTP_KEY_ALIAS = "totp_key"
    }
}