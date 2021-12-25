package com.ravi.basisdemoapp

import androidx.lifecycle.ViewModel
import com.ravi.basisdemoapp.service.Repository


class MainViewModel(private val repository: Repository) : ViewModel() {

    fun getData() = repository.getDataFromServer()

}