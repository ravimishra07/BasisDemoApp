package com.ravi.basisdemoapp.service

import com.ravi.basisdemoapp.service.ApiInterface


class Repository(private val api: ApiInterface) {
    fun getDataFromServer() = api.getData()
}