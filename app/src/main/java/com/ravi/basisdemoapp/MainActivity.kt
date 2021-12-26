package com.ravi.basisdemoapp

import android.os.Bundle
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

    private lateinit var cardContainer: CardContainer
    private var adapter: MainViewAdapter? = null
    private lateinit var progressBar: ProgressBar
    private lateinit var ivPrev: ImageView
    private lateinit var ivNext: ImageView
    private lateinit var ivReStart: ImageView
    private lateinit var llEmptyStack: LinearLayout
    lateinit var progressIndicator: LinearProgressIndicator
    private var modelList = emptyList<SubData>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        initViews()
        setListeners()
        configureViewModel()
    }

    override fun onStart() {
        super.onStart()

        compositeDisposable += viewModel.getData()
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribeBy(
                onSuccess = { userDataList ->
                    progressBar.visibility = GONE
                    initCardContainerView(userDataList.data)
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

    private fun initViews() {
        cardContainer = findViewById(R.id.cardContainer)
        progressBar = findViewById(R.id.progress_bar)
        ivPrev = findViewById(R.id.iv_prev)
        ivReStart = findViewById(R.id.iv_restart)
        ivNext = findViewById(R.id.iv_next)
        progressIndicator = findViewById(R.id.progress_view)
        llEmptyStack = findViewById(R.id.ll_empty_stack)
    }

    private fun setListeners() {
        ivPrev.setOnClickListener(this)
        ivNext.setOnClickListener(this)
        ivReStart.setOnClickListener(this)
        cardContainer.setActionListeners(this)
    }

    private fun configureViewModel() {
        progressBar.visibility = VISIBLE
        val repo = Repository(RetrofitClient.api)
        val vmFactory = MainViewModelFactory(repo)
        /** initialized viewModel with repository as dependency */
        viewModel = ViewModelProvider(this, vmFactory)[MainViewModel::class.java]
    }

    private fun initCardContainerView(subDataList: List<SubData>) {
        /** initialized cardContainer with data from server */
        modelList = subDataList
        adapter = MainViewAdapter(modelList, this)
        adapter?.let {
            cardContainer.setAdapter(it)
        }
    }

    override fun onStop() {
        super.onStop()
        compositeDisposable.dispose()
    }

    /**
     * @param position: Position of card at front
     * @param model: data associated with the card
     * Used to updated progress view when a view is swiped and new one appears
     */
    override fun onItemShow(position: Int, model: Any) {
        if (modelList.size == position + 1) {
            llEmptyStack.visibility = VISIBLE
        } else {
            llEmptyStack.visibility = GONE
        }
        val modelData = model as SubData
        progressIndicator.progress = modelData.id
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
                adapter = MainViewAdapter(modelList, this)
                adapter?.let {
                    cardContainer.setAdapter(it)
                    progressIndicator.progress = 0
                }
            }
        }
    }
}
