package com.venus_customer.model.api

import android.os.Build
import android.service.autofill.UserData
import com.venus_customer.model.dataClass.base.ClientConfig
import com.venus_customer.model.dataClass.userData.UserDataDC
import com.venus_customer.util.SharedPreferencesManager
import com.venus_customer.util.getFCMToken
import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import okhttp3.Response
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

class NetworkInterceptor : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        val fcmToken = runBlocking { fcmToken() }

        val newRequest = request.newBuilder().apply {
            addHeader("appVersion", "550")
            addHeader("deviceName", Build.MODEL)
            addHeader("deviceToken", fcmToken)
            addHeader("deviceType", "0")
            addHeader("loginType", "0")

            SharedPreferencesManager.getModel<UserDataDC>(SharedPreferencesManager.Keys.USER_DATA)?.let {
                addHeader("accessToken", it.accessToken.orEmpty())
            }

            SharedPreferencesManager.getModel<ClientConfig>(SharedPreferencesManager.Keys.CLIENT_CONFIG)?.let {
                addHeader("operatorToken", it.operatorToken.orEmpty())
                addHeader("locale", it.locale.orEmpty())
                addHeader("clientId", it.clientId.orEmpty())
            }

        }.build()

        return chain.proceed(newRequest)

    }



    suspend fun fcmToken(): String = suspendCoroutine {
        getFCMToken { token -> it.resume(token) }
    }

}