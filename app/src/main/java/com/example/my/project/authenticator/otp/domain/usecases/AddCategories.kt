package com.example.my.project.authenticator.otp.domain.usecases

import com.example.my.project.authenticator.otp.data.database.Categories
import com.example.my.project.authenticator.otp.domain.repository.TotpKeyRepository

class AddCategories(
    private val repository: TotpKeyRepository
) {

    suspend operator fun invoke(categories: Categories) {
        repository.addCategories(categories)
    }
}