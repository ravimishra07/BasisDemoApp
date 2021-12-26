package com.ravi.basisdemoapp.card

import android.animation.Animator
import android.content.Context
import android.content.res.Resources
import android.util.AttributeSet
import android.util.Log
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.view.animation.AccelerateInterpolator
import android.view.animation.Animation
import android.view.animation.OvershootInterpolator
import android.view.animation.Transformation
import android.widget.FrameLayout
import androidx.cardview.widget.CardView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.contains
import com.ravi.basisdemoapp.MainViewAdapter
import com.ravi.basisdemoapp.R
import kotlin.math.roundToInt


class CardContainer(context:Context,attrs:AttributeSet?) :FrameLayout(context, attrs),
    ViewAdapter.DataListeners, 
    ViewAdapter.ActionListeners,
    View.OnTouchListener {
   var cardGestureListeners:CardGestureListeners?=null
    fun setActionListeners(cardGestureListeners: CardGestureListeners){
        this.cardGestureListeners = cardGestureListeners
    }
    var viewAdapter:ViewAdapter?=null

    var margin = 0.px
    var marginTop = 0.px
    var maxSize = 5

    private var mainView: FrameLayout?=null
    private var emptyContainer: FrameLayout? = null
    private var draggableSurfaceLayout: FrameLayout? = null

    private var rightBoundary = 0f
    private var leftBoundary = 0f
    private var downBoundary = 0f
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
    private var isFirstTimeMove = false
    private var isCardMovedDown = false

    private var viewArray:ArrayList<View> = arrayListOf()
    private var fixedArray:ArrayList<View> = arrayListOf()
    init {
        screenHeight = Resources.getSystem().displayMetrics.heightPixels
        screenWidth = Resources.getSystem().displayMetrics.widthPixels
        leftBoundary = screenWidth*(0.17f)
        rightBoundary = screenWidth*(0.83f)
        downBoundary = screenHeight*(0.75f)
        setUpSurface()

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
            //fixedv.add(childCard)
        }
        for (view in viewArray) {
            mainView?.addView(view)
        }
        mainView?.pulseOnlyUp()


        count = viewArray.size

        setCardForAnimation()
        cardGestureListeners?.onItemShow(swipeIndex, viewAdapter.getItem(swipeIndex))
        fixedArray = viewArray
    }
    private fun setCardForAnimation() {
        if (viewArray.isNotEmpty()) {
            viewArray[viewArray.size - 1].setOnTouchListener(this)
        } else {
            emptyContainer?.visibility = View.VISIBLE
            mainView?.visibility = View.GONE
            cardGestureListeners?.onSwipeCompleted()
        }
    }
    private fun reset() {
        swipeIndex = 0
        count = 0
        viewArray.clear()
        mainView?.removeAllViews()
    }

    override fun notifyAppendData() {
        val needCount = maxSize - mainView!!.childCount
        if (needCount > 0) {
            emptyContainer?.visibility = View.GONE
            mainView?.visibility = View.VISIBLE
        }
        if ((count + needCount) < viewAdapter!!.getCount()) {
            val size = (count + needCount)
            for (i in count until size) {
                val childCard = LayoutInflater.from(context).inflate(R.layout.card_position, null)
                val holder = childCard.findViewById<FrameLayout>(R.id.frame)
                val card = childCard.findViewById<CardView>(R.id.card)
                (card.layoutParams as ConstraintLayout.LayoutParams).setMargins(
                    margin * (viewArray.size + 1),
                    marginTop * (size + 1 - i),
                    margin * (viewArray.size + 1),
                    margin * (viewArray.size + 1)
                )
                card.elevation = 1.toFloat()
                holder.addView(viewAdapter?.getView(i))
                viewArray.add(0, childCard)
                mainView?.addView(childCard, 0)
                count++
            }
        }
        reOrderMarginsForNewItems()
        setCardForAnimation()
    }
    private fun reOrderMarginsForNewItems() {
        if (viewArray.isNotEmpty() && mainView != null) {
            for (i in 0 until mainView!!.childCount) {
                val card = mainView!!.getChildAt(i).findViewById<CardView>(R.id.card)
                viewWithNewMarginAnimationForNewItems(card, i)
                card.elevation = (mainView!!.childCount + 1 - i).toFloat()
            }
        }
    }
    override fun right() {
        if (viewArray.isNotEmpty()) {
            if (viewArray[viewArray.size - 1].animation?.hasEnded() == false)
                return
            dismissCard(viewArray[viewArray.size - 1], (screenWidth * 2),true)
            viewAdapter?.let {
                if (it.getCount() > swipeIndex) {
                  //  cardGestureListeners?.righ(swipeIndex, it.getItem(swipeIndex))
                    swipeIndex++
                }
            }
        }
    }

    override fun up() {

    }

    override fun left() {
        if (viewArray.isNotEmpty()) {
            if (viewArray[viewArray.size - 1].animation?.hasEnded() == false)
                return
            dismissCard(viewArray[viewArray.size - 1],- (screenWidth * 2),true)
            viewAdapter?.let {
                if (it.getCount() > swipeIndex) {
                    //  cardGestureListeners?.righ(swipeIndex, it.getItem(swipeIndex))
                    swipeIndex++
                }
            }
        }
    }

    override fun down() {

    }

    override fun onTouch(v: View?, event: MotionEvent?): Boolean {
        if (v != null)
            when (event?.action) {
                MotionEvent.ACTION_DOWN -> {
                    isFirstTimeMove = mainView?.contains(v) == true
                    if (v.parent != null) {
                        (v.parent as ViewGroup).removeView(v)
                        v.layoutParams = LayoutParams(
                            viewArray[viewArray.size - 1].width,
                            viewArray[viewArray.size - 1].height
                        )
                        (v.layoutParams as MarginLayoutParams).topMargin += mainView!!.y.toInt()
                        draggableSurfaceLayout?.addView(v)
                    }
                    oldX = event.x
                    oldY = event.y
                    v.clearAnimation()
                    return true
                }
                MotionEvent.ACTION_UP -> {
                    isFirstTimeMove = false
                    when {
                        isCardAtLeft(v) -> {
                            dismissCard(v, -(screenWidth * 2),false)
                            viewAdapter?.let {
                                if (it.getCount() > swipeIndex) {
                                    swipeIndex++
                                }
                            }
                        }
                        isCardAtRight(v) -> {
                            dismissCard(v, (screenWidth * 2),false)
                            viewAdapter?.let {
                                if (it.getCount() > swipeIndex) {
                                    swipeIndex++
                                }
                            }
                        }

                        isCardDown() -> {

//                            adapter = MainViewAdapter(modelList, this)
//                            cardContainer.setAdapter(adapter)
//
//                            cardGestureListeners?.onItemShow(swipeIndex, viewAdapter.getItem(swipeIndex))


                            resetCard(v)
                            if(swipeIndex>0){
                                swipeIndex--
                            }

                            viewAdapter?.let {
                                if (it.getCount() > swipeIndex) {
                              //      cardGestureListeners?.onSwipeCancel(swipeIndex, it.getItem(swipeIndex))
                                    cardGestureListeners?.onItemShow(swipeIndex, it.getItem(swipeIndex))

                                }
                            }
                        }
                        else -> {
                            resetCard(v)
                            viewAdapter?.let {
                                if(swipeIndex>=0){
                                    if (it.getCount() > swipeIndex) {
                                        cardGestureListeners?.onSwipeCancel(swipeIndex, it.getItem(swipeIndex))
                                    }
                                }

                            }
                        }
                    }
                    return true
                }

                MotionEvent.ACTION_MOVE -> {
                    newX = event.x
                    newY = event.y.plus(if (isFirstTimeMove) mainView!!.y else 0f)
                    dX = newX - oldX
                    dY = newY - oldY

                    setCardRotation(v, v.x)
                    if(newY<oldY){
                        v.y = v.y.plus(dY)
                        v.x = v.x.plus(dX)

                    }else{
                        isCardMovedDown = true
                    }

                    return true
                }
                else -> return super.onTouchEvent(event)
            }
        return super.onTouchEvent(event)
    }

    private fun setCardRotation(card: View, posX: Float) {
        val rotation = (cardDegreesForTransform * (posX)) / screenWidth
        val halfCardHeight = (card.height / 2)
        if (oldY < halfCardHeight) {
            card.rotation = rotation
        } else {
            card.rotation = -rotation
        }
    }

    private fun isCardAtLeft(view: View?): Boolean {
        if (view != null) {
            return view.x + view.width / 2 < leftBoundary
        }
        return false
    }

    private fun isCardAtRight(view: View?): Boolean {
        if (view != null) {
            return view.x + view.width / 2 > rightBoundary
        }
        return false
    }
    private fun isCardDown(): Boolean {

       val isDown =  isCardMovedDown
        isCardMovedDown = false
        return isDown
    }
    private fun dismissCard(card: View, xPos: Int, rotate: Boolean) {
        card.animate()
            .x(xPos.toFloat())
            .y(0F)
            .rotation(if (rotate) {if (xPos > 0) 45f else -45f} else 0f)
            .setInterpolator(AccelerateInterpolator())
            .setDuration(300)
            .setListener(object : Animator.AnimatorListener {
                override fun onAnimationStart(animation: Animator?) {

                }

                override fun onAnimationEnd(animation: Animator?) {
                    card.parent?.let { viewParent ->
                        val viewGroup = viewParent as FrameLayout
                        viewGroup.removeView(card)

                        if (viewArray.isNotEmpty()) {
                            viewArray.removeLast()
                            viewAdapter?.let {
                                if (it.getCount() > swipeIndex)
                                    cardGestureListeners?.onItemShow(
                                        swipeIndex,
                                        it.getItem(swipeIndex)
                                    )
                            }
                            setCardForAnimation()
                            reOrderMargins()
                        }
                    }
                }

                override fun onAnimationCancel(animation: Animator?) {

                }

                override fun onAnimationRepeat(animation: Animator?) {

                }
            })
    }
    private fun dismissCardVertically(card: View, yPos: Int, rotate: Boolean) {
        if (viewArray.isNotEmpty()) {
           // viewArray.removeLast()
               if(swipeIndex!=0){
                   swipeIndex--
               }
            viewAdapter?.let {
                if (it.getCount() > swipeIndex)
                    cardGestureListeners?.onItemShow(
                        swipeIndex,
                        it.getItem(swipeIndex)
                    )
            }
            setCardForAnimation()
            reOrderMargins()
        }
    }

    private fun reOrderMargins() {
        if (viewArray.isNotEmpty() && mainView != null) {
            for (i in 0 until mainView!!.childCount) {
                val card = mainView!!.getChildAt(i).findViewById<CardView>(R.id.card)
                viewWithNewMarginAnimation(card)
                card.elevation = (mainView!!.childCount + 1 - i).toFloat()
            }
            if (nextCondition()) {
                addOneMore()
            }
        }
    }
    private fun addOneMore() {
        if (count < viewAdapter?.getCount() ?: 0) {
            val childCard = LayoutInflater.from(context).inflate(R.layout.card_position, null)
            val holder = childCard.findViewById<FrameLayout>(R.id.frame)
            val card = childCard.findViewById<CardView>(R.id.card)
            (card.layoutParams as ConstraintLayout.LayoutParams).setMargins(
                margin * (viewArray.size + 1),
                marginTop,
                margin * (viewArray.size + 1),
                margin * (viewArray.size + 1)
            )
            card.elevation = 1.toFloat()
            holder.addView(viewAdapter?.getView(count))
            viewArray.add(0, childCard)
            mainView?.addView(childCard, 0)
            count++
        }
    }
    private fun nextCondition() =
        (viewAdapter != null && ((viewAdapter?.getCount() ?: 0) > count))

    private fun viewWithNewMarginAnimation(v: View) {
        val newLeftMargin = (v.layoutParams as ConstraintLayout.LayoutParams).leftMargin - margin
        val newTopMargin =
            (v.layoutParams as ConstraintLayout.LayoutParams).topMargin + (if (nextCondition()) marginTop else 0)
        val newRightMargin = (v.layoutParams as ConstraintLayout.LayoutParams).rightMargin - margin
        val newBottomMargin =
            (v.layoutParams as ConstraintLayout.LayoutParams).bottomMargin - margin

        val oldLeftMargin = (v.layoutParams as ConstraintLayout.LayoutParams).leftMargin
        val oldTopMargin = (v.layoutParams as ConstraintLayout.LayoutParams).topMargin
        val oldRightMargin = (v.layoutParams as ConstraintLayout.LayoutParams).rightMargin
        val oldBottomMargin = (v.layoutParams as ConstraintLayout.LayoutParams).bottomMargin

        val a: Animation = object : Animation() {
            override fun applyTransformation(interpolatedTime: Float, t: Transformation?) {
                val params = v.layoutParams as ConstraintLayout.LayoutParams
                params.leftMargin =
                    (oldLeftMargin + ((newLeftMargin - oldLeftMargin) * interpolatedTime)).roundToInt()
                params.topMargin =
                    (oldTopMargin + ((newTopMargin - oldTopMargin) * interpolatedTime)).roundToInt()
                params.rightMargin =
                    (oldRightMargin + ((newRightMargin - oldRightMargin) * interpolatedTime)).roundToInt()
                params.bottomMargin =
                    (oldBottomMargin + ((newBottomMargin - oldBottomMargin) * interpolatedTime)).roundToInt()
                v.layoutParams = params
            }
        }
        a.duration = 50
        v.startAnimation(a)
    }

    private fun viewWithNewMarginAnimationForNewItems(v: View, i: Int) {

        val newLeftMargin = margin * (maxSize - i)
        val newTopMargin = (i + 1) * marginTop
        val newRightMargin = margin * (maxSize - i)
        val newBottomMargin = margin * (maxSize - i)

        val oldLeftMargin = (v.layoutParams as ConstraintLayout.LayoutParams).leftMargin
        val oldTopMargin = (v.layoutParams as ConstraintLayout.LayoutParams).topMargin
        val oldRightMargin = (v.layoutParams as ConstraintLayout.LayoutParams).rightMargin
        val oldBottomMargin = (v.layoutParams as ConstraintLayout.LayoutParams).bottomMargin

        val a: Animation = object : Animation() {
            override fun applyTransformation(interpolatedTime: Float, t: Transformation?) {
                val params = v.layoutParams as ConstraintLayout.LayoutParams
                params.leftMargin =
                    (oldLeftMargin + ((newLeftMargin - oldLeftMargin) * interpolatedTime)).roundToInt()
                params.topMargin =
                    (oldTopMargin + ((newTopMargin - oldTopMargin) * interpolatedTime)).roundToInt()
                params.rightMargin =
                    (oldRightMargin + ((newRightMargin - oldRightMargin) * interpolatedTime)).roundToInt()
                params.bottomMargin =
                    (oldBottomMargin + ((newBottomMargin - oldBottomMargin) * interpolatedTime)).roundToInt()
                v.layoutParams = params
            }
        }
        a.duration = 150
        v.startAnimation(a)
    }
    private fun resetCard(card: View) {
        card.animate()
            .x(resetX)
            .y( mainView!!.y)
            .rotation(0F)
            .setInterpolator(OvershootInterpolator()).duration = 300
    }
}