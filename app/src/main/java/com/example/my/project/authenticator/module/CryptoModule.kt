package com.example.my.project.authenticator.module

import com.example.my.project.authenticator.otp.domain.crypto.PasswordHasher
import com.example.my.project.authenticator.otp.domain.crypto.SecretEncryptor
import com.example.my.project.authenticator.otp.domain.crypto.SecretKeyRepository
import com.example.my.project.authenticator.otp.domain.crypto.TotpCodeGenerator
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import com.example.my.project.authenticator.otp.data.crypto.AesGcmSecretEncryptor
import com.example.my.project.authenticator.otp.data.crypto.AndroidKeyStoreRepository
import com.example.my.project.authenticator.otp.data.crypto.Argon2PasswordHasher
import com.example.my.project.authenticator.otp.data.crypto.TotpCodeGeneratorImpl
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