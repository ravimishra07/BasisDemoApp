package com.ravi.basisdemoapp

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import androidx.lifecycle.ViewModelProvider
import com.ravi.basisdemoapp.card.CardContainer
import com.ravi.basisdemoapp.card.CardGestureListeners
import com.ravi.basisdemoapp.card.px
import com.ravi.basisdemoapp.model.SubData
import com.ravi.basisdemoapp.service.Repository
import com.ravi.basisdemoapp.service.RetrofitClient
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.plusAssign
import io.reactivex.rxkotlin.subscribeBy
import io.reactivex.schedulers.Schedulers

class MainActivity : AppCompatActivity(), CardGestureListeners {
    private lateinit var viewModel: MainViewModel
    private val compositeDisposable = CompositeDisposable()
  // private lateinit var recyclerView:RecyclerView

    lateinit var cardContainer: CardContainer
    lateinit var adapter: MainViewAdapter
    private var modelList = emptyList<SubData>()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
       // recyclerView = findViewById(R.id.recyclerView)
        cardContainer = findViewById(R.id.cardContainer)
        cardContainer.setActionListeners(this)
        /*Customization*/
        cardContainer.maxSize = 3
        cardContainer.marginTop = 13.px
        cardContainer.margin = 20.px
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

    private fun initRecyclerView(subDataList: List<SubData>) {
        modelList = subDataList
        adapter = MainViewAdapter(modelList, this)
        cardContainer.setAdapter(adapter)



//       recyclerView.apply {
//            setHasFixedSize(true)
//            layoutManager = LinearLayoutManager(this@MainActivity)
//            adapter = RecyclerViewAdapter(userDataList)
//        }
    }
    override fun onStop() {
        super.onStop()
        compositeDisposable.dispose()
    }
    private fun generateEmptyView(): View {
        return LayoutInflater.from(this).inflate(R.layout.end_view, null)
    }

    override fun onLeftSwipe(position: Int, model: Any) {
    }

    override fun onUpSwipe(position: Int, model: Any) {
        
    }

    override fun onDownSwipe(position: Int, model: Any) {
        
    }

    override fun onRightSwipe(position: Int, model: Any) {
        
    }

    override fun onItemShow(position: Int, model: Any) {
        
    }

    override fun onSwipeCancel(position: Int, model: Any) {
        
    }

    override fun onSwipeCompleted() {
        
    }

}
