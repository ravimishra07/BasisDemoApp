package com.ravi.basisdemoapp

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.ravi.basisdemoapp.model.SubData
import com.ravi.basisdemoapp.service.Repository
import com.ravi.basisdemoapp.service.RetrofitClient
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.plusAssign
import io.reactivex.rxkotlin.subscribeBy
import io.reactivex.schedulers.Schedulers

class MainActivity : AppCompatActivity() {
    private lateinit var viewModel: MainViewModel
    private val compositeDisposable = CompositeDisposable()
   private lateinit var recyclerView:RecyclerView


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        recyclerView = findViewById(R.id.recyclerView)

        val repo = Repository(RetrofitClient.api)
        val vmFactory = MainViewModelFactory(repo)
        viewModel = ViewModelProvider(this, vmFactory)[MainViewModel::class.java]
    }

    override fun onStart() {
        super.onStart()

        compositeDisposable += viewModel.getData()
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribeBy(
                onSuccess = { userDataList ->
                    initRecyclerView(userDataList.data)

                },
                onError = { e ->  }
            )
    }

    private fun initRecyclerView(userDataList: List<SubData>) {
       recyclerView.apply {
            setHasFixedSize(true)
            layoutManager = LinearLayoutManager(this@MainActivity)
            adapter = RecyclerViewAdapter(userDataList)
        }
    }
    override fun onStop() {
        super.onStop()
        compositeDisposable.dispose()
    }

}