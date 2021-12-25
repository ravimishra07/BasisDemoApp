package com.ravi.basisdemoapp.service

import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory

object RetrofitClient {
    val api: ApiInterface = Retrofit.Builder()
        .baseUrl("https://git.io/")
        .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
        .addConverterFactory(JsonCleanConverter.create())
        .build()
        .create(ApiInterface::class.java)
}