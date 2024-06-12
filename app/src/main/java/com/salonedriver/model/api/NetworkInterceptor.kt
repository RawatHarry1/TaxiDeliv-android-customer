package com.salonedriver.model.api

import android.os.Build
import com.salonedriver.BuildConfig
import com.salonedriver.model.dataclassses.clientConfig.ClientConfigDC
import com.salonedriver.model.dataclassses.userData.UserDataDC
import com.salonedriver.util.AppUtils
import com.salonedriver.util.SharedPreferencesManager
import com.salonedriver.util.getFCMToken
import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import okhttp3.Response
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

class NetworkInterceptor : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        var request = chain.request()

        val fcmToken = runBlocking { fcmToken() }

        request = request.newBuilder().apply {
            header("appVersion", BuildConfig.VERSION_CODE.toString())
            header("deviceName", Build.MODEL)
            header("deviceType", "0")
            header("loginType", "1")
            header("deviceToken", fcmToken)

            SharedPreferencesManager.getModel<ClientConfigDC>(SharedPreferencesManager.Keys.CLIENT_CONFIG)
                ?.let {
                    header("clientId", it.clientId.orEmpty())
                    header("locale", it.locale.orEmpty())
                    header("operatorToken", it.operatorToken.orEmpty())
                }

            SharedPreferencesManager.getModel<UserDataDC>(SharedPreferencesManager.Keys.USER_DATA)
                ?.let {
                    header("accessToken", it.accessToken.orEmpty())
                }
        }.build()

        return chain.proceed(request)
    }



    suspend fun fcmToken(): String = suspendCoroutine {
        getFCMToken { token -> it.resume(token) }
    }
}