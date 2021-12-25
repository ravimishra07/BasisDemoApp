package com.ravi.basisdemoapp

import androidx.lifecycle.ViewModel


class MainViewModel(private val repository: Repository) : ViewModel() {

    fun getData() = repository.getDataFromServer()

}