package com.example.my.project.authenticator.otp.domain.repository

interface RemoteConfigRepository {

    suspend fun getRemoteResponse()

    fun setDefaultIds()
}