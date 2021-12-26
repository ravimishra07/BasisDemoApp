package com.ravi.basisdemoapp

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.progressindicator.LinearProgressIndicator
import com.ravi.basisdemoapp.card.CardContainer
import com.ravi.basisdemoapp.card.CardGestureListeners
import com.ravi.basisdemoapp.model.SubData
import com.ravi.basisdemoapp.service.Repository
import com.ravi.basisdemoapp.service.RetrofitClient
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.plusAssign
import io.reactivex.rxkotlin.subscribeBy
import io.reactivex.schedulers.Schedulers

class MainActivity : AppCompatActivity(), CardGestureListeners, View.OnClickListener {
    private lateinit var viewModel: MainViewModel
    private val compositeDisposable = CompositeDisposable()
    // private lateinit var recyclerView:RecyclerView

    lateinit var cardContainer: CardContainer
    var adapter: MainViewAdapter? = null
    lateinit var progressBar: ProgressBar
    lateinit var ivPrev: ImageView
    lateinit var ivNext: ImageView
    lateinit var ivReStart: ImageView
    lateinit var llEmptyStack: LinearLayout

    lateinit var progressIndicator: LinearProgressIndicator

    private var modelList = emptyList<SubData>()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        // recyclerView = findViewById(R.id.recyclerView)
        cardContainer = findViewById(R.id.cardContainer)
        progressBar = findViewById(R.id.progress_bar)

        ivPrev = findViewById(R.id.iv_prev)
        ivReStart = findViewById(R.id.iv_restart)
        ivNext = findViewById(R.id.iv_next)
        progressIndicator = findViewById(R.id.progress_view)
        llEmptyStack = findViewById(R.id.ll_empty_stack)

        ivPrev.setOnClickListener(this)
        ivNext.setOnClickListener(this)
        ivReStart.setOnClickListener(this)

        cardContainer.setActionListeners(this)

        progressBar.visibility = VISIBLE

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
                    progressBar.visibility = GONE
                    initRecyclerView(userDataList.data)
                    progressIndicator.max = userDataList.data.size
                    progressIndicator.progress = 0
                    progressIndicator.visibility = VISIBLE

                },
                onError = { e ->
                    progressBar.visibility = GONE
                    Toast.makeText(this, "Something went wrong", Toast.LENGTH_LONG).show()
                }
            )
    }

    private fun initRecyclerView(subDataList: List<SubData>) {
        modelList = subDataList
        adapter = MainViewAdapter(modelList, this)
        adapter?.let {
            cardContainer.setAdapter(it)
        }


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

    override fun onItemShow(position: Int, model: Any) {
        Log.v("onItemShow", "$position pos")
        if (modelList.size == position + 1) {
            llEmptyStack.visibility = VISIBLE
        } else {
            llEmptyStack.visibility = GONE
        }
val modelData = model as SubData
      //  if(position>0){
            progressIndicator.progress = modelData.id//position + 1
//            if (progressIndicator.progress < position){
//                progressIndicator.progress = position + 1
//            }else{
//                progressIndicator.progress = position - 1
//            }

//        if (progressIndicator.progress <= position && position > 0) {
//            progressIndicator.progress = position + 1
//        }


    }

    override fun onSwipeCancel(position: Int, model: Any) {
//        cardContainer?.removeAllViews()
//
//        //modelList = subDataList
//        adapter = MainViewAdapter(modelList, this)
//        adapter?.let {
//            cardContainer.setAdapter(it)
//        }
//        cardContainer.cardContainer
    }

    override fun onSwipeCompleted() {

    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.iv_prev -> {

                progressIndicator.progress = (progressIndicator.progress + 1)
                adapter?.left()
            }

            R.id.iv_next -> {
                progressIndicator.progress = (progressIndicator.progress + 1)
                adapter?.right()
            }

            R.id.iv_restart -> {
                // it.pulse()
                // modelList.shuffle()

                adapter = MainViewAdapter(modelList, this)
                adapter?.let {
                    cardContainer.setAdapter(it)
                    progressIndicator.progress = 0
                }

            }
        }
    }

}
