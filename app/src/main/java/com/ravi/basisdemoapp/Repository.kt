package com.ravi.basisdemoapp

import com.ravi.basisdemoapp.service.ApiInterface


class Repository(private val api: ApiInterface) {
    fun getDataFromServer() = api.getData()
}