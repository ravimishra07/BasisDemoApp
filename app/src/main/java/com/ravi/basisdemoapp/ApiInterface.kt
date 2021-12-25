package com.ravi.basisdemoapp

import com.ravi.basisdemoapp.model.DataModel
import retrofit2.http.GET

interface ApiInterface {
    @GET("/fjaqJ")
    fun getData(): DataModel
}