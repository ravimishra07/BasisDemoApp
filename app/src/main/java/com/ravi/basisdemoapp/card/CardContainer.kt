package com.ravi.basisdemoapp.card

import android.content.Context
import android.content.res.Resources
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.cardview.widget.CardView
import androidx.constraintlayout.widget.ConstraintLayout
import com.ravi.basisdemoapp.R

class CardContainer(context:Context,attrs:AttributeSet?) :FrameLayout(context, attrs),ViewAdapter.DataListeners, ViewAdapter.ActionListeners {
   var cardListeners:CardGestureListeners?=null
    fun setActionListeners(cardListeners: CardGestureListeners){
        this.cardListeners = cardListeners
    }
    var viewAdapter:ViewAdapter?=null

    var margin = 20.px
    var marginTop = 10.px
    var maxSize = 5

    private var mainView: FrameLayout?=null
    private var emptyContainer: FrameLayout? = null
    private var draggableSurfaceLayout: FrameLayout? = null

    private var rightBoundary = 0f
    private var leftBoundary = 0f
    private var screenWidth = 0
    private var screenHeight = 0
    private var swipeIndex = 0

    private var count = 0
    private var oldX = 0f
    private var oldY = 0f
    private var newX = 0f
    private var newY = 0f
    private var dX = 0f
    private var dY = 0f
    private var resetX = 0f
    private var resetY = 0f
    private val cardDegreesForTransform = 40.0f


    private var viewArray:ArrayList<View> = arrayListOf()
    init {
        screenHeight = Resources.getSystem().displayMetrics.heightPixels
        screenWidth = Resources.getSystem().displayMetrics.widthPixels
        leftBoundary = screenWidth*(1.66f)
        rightBoundary = screenWidth*(8.33f)
    }

    fun setEmptyView(v:View){
        emptyContainer?.visibility = VISIBLE
        mainView?.visibility = GONE

       if(v.parent!=null){
           (v as ViewGroup)?.removeView(v)
           emptyContainer?.addView(v)

       }
    }
    private fun setUpSurface(){
        val viewMain = LayoutInflater.from(context).inflate(R.layout.card_view_container,null)
        mainView = viewMain.findViewById(R.id.mainView)
        emptyContainer = viewMain.findViewById(R.id.emptyView)
        draggableSurfaceLayout = viewMain.findViewById(R.id.draggableView)
        addView(viewMain)
    }

    fun setAdapter(viewAdapter: ViewAdapter) {
        reset()

        this.viewAdapter = viewAdapter
        this.viewAdapter?.dataListeners = this
        this.viewAdapter?.actionListeners = this

        if (viewAdapter.getCount() > 0) {
            emptyContainer?.visibility = View.GONE
            mainView?.visibility = View.VISIBLE
        } else {
            return
        }

        val size =
            if (viewAdapter.getCount() > maxSize) maxSize else viewAdapter.getCount()

        for (i in size downTo 1) {
            val childCard = LayoutInflater.from(context).inflate(R.layout.card_position, null)
            val holder = childCard.findViewById<FrameLayout>(R.id.frame)
            val card = childCard.findViewById<CardView>(R.id.card)
            (card.layoutParams as ConstraintLayout.LayoutParams).setMargins(
                margin * i,
                marginTop * (size + 1 - i),
                margin * i,
                margin * i
            )
            card.elevation = (size + 1 - i).toFloat()
            holder.addView(viewAdapter.getView(i - 1))
            viewArray.add(childCard)
        }
        for (view in viewArray) {
            mainView?.addView(view)
        }
        mainView?.pulseOnlyUp()


        count = viewArray.size

            //setCardForAnimation()
        cardListeners?.onItemShow(swipeIndex, viewAdapter.getItem(swipeIndex))
    }

    private fun reset() {
        swipeIndex = 0
        count = 0
        viewArray.clear()
        mainView?.removeAllViews()
    }

    override fun notifyAppendData() {
        TODO("Not yet implemented")
    }

    override fun right() {
        TODO("Not yet implemented")
    }

    override fun up() {
        TODO("Not yet implemented")
    }

    override fun left() {
        TODO("Not yet implemented")
    }

    override fun down() {
        TODO("Not yet implemented")
    }
}