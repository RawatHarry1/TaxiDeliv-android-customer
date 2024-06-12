package com.salonedriver.di

import android.content.Context
import android.net.ConnectivityManager
import com.google.gson.GsonBuilder
import com.salonedriver.SaloneDriver
import com.salonedriver.model.api.ApiInterface
import com.salonedriver.model.api.NetworkInterceptor
import com.salonedriver.BuildConfig
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.IOException
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
class ApiModule {
    val apiClient: ApiInterface
        @Singleton
        @Provides
        get() {
            val interceptor = ConnectivityInterceptor(SaloneDriver.appContext)
            val okHttpClient = OkHttpClient.Builder()
                .addInterceptor(interceptor)
                .connectTimeout(2, TimeUnit.MINUTES)
                .readTimeout(2, TimeUnit.MINUTES)
                .writeTimeout(2, TimeUnit.MINUTES)
                .addNetworkInterceptor(HttpLoggingInterceptor().setLevel(if (BuildConfig.DEBUG) HttpLoggingInterceptor.Level.BODY else HttpLoggingInterceptor.Level.NONE))
                .addInterceptor(NetworkInterceptor())
                .build()
            val retrofit = Retrofit.Builder()
                .addConverterFactory(GsonConverterFactory.create())
                .baseUrl(BuildConfig.BASE_URL)
                .client(okHttpClient)
                .build()

            return retrofit.create(ApiInterface::class.java)
        }

    /**
     * Method to create [OkHttpClient] builder by adding required headers in the [Request]
     *
     * @return OkHttpClient object
     */


    class ConnectivityInterceptor(private val context: Context) : Interceptor {

        @Throws(IOException::class)
        override fun intercept(chain: Interceptor.Chain): Response {
            if (!isConnected()) {
                throw IOException("No internet connection")
            }

            return chain.proceed(chain.request())
        }

        private fun isConnected(): Boolean {
            val connectivityManager =
                context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
            val networkInfo = connectivityManager.activeNetworkInfo
            return networkInfo != null && networkInfo.isConnected
        }
    }

}