package com.project491.weatherapp_breeze.utils

import com.project491.weatherapp_breeze.data.ApiInterface
import okhttp3.ConnectionSpec
import okhttp3.OkHttpClient
import okhttp3.TlsVersion
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

/*object RetrofitInstance {
    val apiCurrent: ApiInterface by lazy {
        Retrofit.Builder()
            .baseUrl(Util.Base)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ApiInterface::class.java)
    }

    val apiForecast: ApiInterface by lazy {
        Retrofit.Builder()
            .baseUrl(Util.Base1)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ApiInterface::class.java)
    }
}*/

object RetrofitInstance {
    private val okHttpClient = OkHttpClient.Builder()
        .connectionSpecs(listOf(
            ConnectionSpec.Builder(ConnectionSpec.MODERN_TLS)
                .tlsVersions(TlsVersion.TLS_1_2, TlsVersion.TLS_1_3)
                .allEnabledCipherSuites()  // Optionally enable all ciphers if the server has specific requirements
                .build()))
        .addNetworkInterceptor(HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BODY))
        .build()

    val apiCurrent: ApiInterface by lazy {
        Retrofit.Builder()
            .baseUrl(Util.Base)  // Ensure Util.Base is your correct URL
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ApiInterface::class.java)
    }

    val apiForecast: ApiInterface by lazy {
        Retrofit.Builder()
            .baseUrl(Util.Base1)  // Ensure Util.Base1 is your correct URL
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ApiInterface::class.java)
    }
}
