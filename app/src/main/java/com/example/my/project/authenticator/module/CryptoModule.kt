package com.example.my.project.authenticator.module

import com.example.my.project.authenticator.otp.domain.crypto.PasswordHasher
import com.example.my.project.authenticator.otp.domain.crypto.SecretEncryptor
import com.example.my.project.authenticator.otp.domain.crypto.SecretKeyRepository
import com.example.my.project.authenticator.otp.domain.crypto.TotpCodeGenerator
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import retanar.totp_android.data.crypto.AesGcmSecretEncryptor
import retanar.totp_android.data.crypto.AndroidKeyStoreRepository
import retanar.totp_android.data.crypto.Argon2PasswordHasher
import retanar.totp_android.data.crypto.TotpCodeGeneratorImpl
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object CryptoModule {

    @Singleton
    @Provides
    fun provideSecretKeyRepository(): SecretKeyRepository = AndroidKeyStoreRepository()

    @Provides
    fun provideAesSecretEncryptor(keyRepository: SecretKeyRepository): SecretEncryptor {
        val key = keyRepository.getKey(AndroidKeyStoreRepository.TOTP_KEY_ALIAS)
            ?: keyRepository.generateRandomKey(AndroidKeyStoreRepository.TOTP_KEY_ALIAS)
        return AesGcmSecretEncryptor(key)
    }

    @Provides
    fun provideTotpCodeGenerator(): TotpCodeGenerator = TotpCodeGeneratorImpl()

    @Provides
    fun provideArgon2PasswordHasher(): PasswordHasher = Argon2PasswordHasher()
}