package com.ravi.basisdemoapp


class Repository(private val api: ApiInterface) {
    fun getDataFromServer() = api.getData()
}