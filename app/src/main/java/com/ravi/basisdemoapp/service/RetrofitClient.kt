package com.ravi.basisdemoapp.service

import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory

object RetrofitClient {
    private const val BASE_URL = "https://git.io/"

    val api: ApiInterface = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
            /** As the response has a forward slash which needs to be removed before actual parsing
             * i've added a custom gson converter **/
        .addConverterFactory(JsonCleanConverter.create())
        .build()
        .create(ApiInterface::class.java)
}