package com.example.my.project.authenticator.otp.domain.usecases

import com.example.my.project.authenticator.otp.domain.repository.TotpKeyRepository

class DeleteCategories(
    private val repository: TotpKeyRepository
) {

    suspend operator fun invoke(categories: Int) {
        repository.deleteCategories(categories)
    }
}