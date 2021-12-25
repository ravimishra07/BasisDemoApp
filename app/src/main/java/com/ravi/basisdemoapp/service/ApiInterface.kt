package com.ravi.basisdemoapp.service

import com.ravi.basisdemoapp.model.DataModel
import io.reactivex.Single
import retrofit2.http.GET

interface ApiInterface {
    @GET("fjaqJ")
    fun getData(): Single<DataModel>
}