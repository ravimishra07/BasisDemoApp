package com.ravi.basisdemoapp

import androidx.lifecycle.ViewModel
import com.ravi.basisdemoapp.service.Repository


class MainViewModel(private val repository: Repository) : ViewModel() {
    /** request repository to fetch data from server */
    fun getData() = repository.getDataFromServer()
}