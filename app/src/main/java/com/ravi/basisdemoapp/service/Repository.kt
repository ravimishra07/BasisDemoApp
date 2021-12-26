package com.ravi.basisdemoapp.service

class Repository(private val api: ApiInterface) {
    fun getDataFromServer() = api.getData()
}