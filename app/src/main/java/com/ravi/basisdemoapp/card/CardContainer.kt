package com.ravi.basisdemoapp.card

import android.animation.Animator
import android.content.Context
import android.content.res.Resources
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.animation.AccelerateInterpolator
import android.view.animation.OvershootInterpolator
import android.widget.FrameLayout
import androidx.cardview.widget.CardView
import androidx.core.view.contains
import com.ravi.basisdemoapp.R
import android.view.animation.TranslateAnimation




class CardContainer(context: Context, attrs: AttributeSet?) : FrameLayout(context, attrs),
    ViewAdapter.DataListeners,
    ViewAdapter.ActionListeners,
    View.OnTouchListener {
    var cardGestureListeners: CardGestureListeners? = null
    fun setActionListeners(cardGestureListeners: CardGestureListeners) {
        this.cardGestureListeners = cardGestureListeners
    }

    var viewAdapter: ViewAdapter? = null

    var margin = 0.px
    var marginTop = 0.px

    private var mainView: FrameLayout? = null

    // private var emptyContainer: FrameLayout? = null
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
    private var centerY = 0f
    private val cardDegreesForTransform = 40.0f
    private var isFirstTimeMove = false
    private var isCardMovedDown = false
    private var lastViewIndex = 0
    private var downSwipeCount = 0


    private var viewArray: ArrayList<View> = arrayListOf()
    private var lastSwipedView: View? = null
    private var fixedArray: ArrayList<View> = arrayListOf()
    var maxValue = 0;

    init {
        screenHeight = Resources.getSystem().displayMetrics.heightPixels
        screenWidth = Resources.getSystem().displayMetrics.widthPixels
        leftBoundary = screenWidth * (0.17f)
        rightBoundary = screenWidth * (0.83f)
        downBoundary = screenHeight * (0.75f)
        setUpSurface()

    }

    private fun setUpSurface() {
        val viewMain = LayoutInflater.from(context).inflate(R.layout.card_view_container, null)
        mainView = viewMain.findViewById(R.id.mainView)
        draggableSurfaceLayout = viewMain.findViewById(R.id.draggableView)
        addView(viewMain)
    }

    fun setAdapter(viewAdapter: ViewAdapter) {

        reset()
        this.viewAdapter = viewAdapter
        this.viewAdapter?.dataListeners = this
        this.viewAdapter?.actionListeners = this

        if (viewAdapter.getCount() > 0) {
            mainView?.visibility = View.VISIBLE
        } else {
            return
        }
        centerY =mainView?.y ?: 0f
        maxValue = viewAdapter.getCount()
        formCards(1)
    }

    private fun formCards(lastIndex: Int, isPrev: Boolean? = false) {
        // mainView?.removeAllViews()
        // swipeIndex = lastIndex
        if (lastIndex > 0) {
            viewAdapter?.let {
                val size = it.getCount() ?: 0

                for (i in size downTo lastIndex) {
                    val childCard =
                        LayoutInflater.from(context).inflate(R.layout.card_position, null)
                    val holder = childCard.findViewById<FrameLayout>(R.id.frame)
                    val card = childCard.findViewById<CardView>(R.id.card)

                    card.elevation = (size + 1 - i).toFloat()

                    holder.addView(it.getView(i - 1))
                    viewArray.add(childCard)
                }
                for (view in viewArray) {

                    mainView?.addView(view)
                }
               val lastView =  viewArray.last()
                if(isPrev==true){
                    presentCardAnim(lastView, 200)
                }

                //mainView?.pulseOnlyUp()
                count = viewArray.size
                setCardForAnimation()
                cardGestureListeners?.onItemShow(swipeIndex, it.getItem(swipeIndex))
                fixedArray = viewArray
            }
        }

    }

    private fun setCardForAnimation() {
        if (viewArray.isNotEmpty()) {
            viewArray[viewArray.size - 1].setOnTouchListener(this)
        } else {
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
        appendData()
        setCardForAnimation()
    }

    private fun appendData() {
        for (i in 1 until viewArray.size) {
            val childCard = LayoutInflater.from(context).inflate(R.layout.card_position, null)
            val holder = childCard.findViewById<FrameLayout>(R.id.frame)
            val card = childCard.findViewById<CardView>(R.id.card)
            card.elevation = 1.toFloat()
            holder.addView(viewAdapter?.getView(i))
            viewArray.add(0, childCard)
            mainView?.addView(childCard, 0)
            count++
        }
    }

    override fun right() {
        if (viewArray.isNotEmpty()) {
            if (viewArray[viewArray.size - 1].animation?.hasEnded() == false)
                return
            dismissCard(viewArray[viewArray.size - 1], (screenWidth * 2), true)
            viewAdapter?.let {
                if (it.getCount() > swipeIndex) {
                    //  cardGestureListeners?.righ(swipeIndex, it.getItem(swipeIndex))
                    swipeIndex++
                    lastViewIndex++
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
            dismissCard(viewArray[viewArray.size - 1], -(screenWidth * 2), true)
            viewAdapter?.let {
                if (it.getCount() > swipeIndex) {
                    //  cardGestureListeners?.righ(swipeIndex, it.getItem(swipeIndex))
                    swipeIndex++
                    lastViewIndex++
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
                    oldX = event.x
                    oldY = event.y
                    return true
                }
                MotionEvent.ACTION_UP -> {
                    isFirstTimeMove = false
                    when {
                        isCardAtLeft(v) -> {
                            downSwipeCount = 0
                            dismissCard(v, -(screenWidth * 2), false)
                            viewAdapter?.let {
                                if (it.getCount() > swipeIndex) {
                                    swipeIndex++
                                    lastViewIndex++
                                }
                            }
                        }
                        isCardAtRight(v) -> {
                            downSwipeCount = 0
                            dismissCard(v, (screenWidth * 2), false)
                            viewAdapter?.let {
                                if (it.getCount() > swipeIndex) {
                                    swipeIndex++
                                    lastViewIndex++
                                }
                            }
                        }
                        isCardDown(v) -> {
//                            lastSwipedView?.let {  lastView ->
//                                viewArray.add(lastView)
//                            }
                            downSwipeCount++
                            // if(swipeIndex>0) {


                            if (downSwipeCount > 1) {
                                lastViewIndex--
                            } else {
                                lastViewIndex = swipeIndex
                            }

                            if (lastViewIndex > 0) {
                                reset()
                                formCards(lastViewIndex, true)
                                setCardForAnimation()
                            } else {
                                resetCardPosition(v)
                            }

                        }

                        else -> {
                            downSwipeCount = 0
                            resetCardPosition(v)
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
                    if (newY < oldY) {
                        v.y = v.y.plus(dY)
                        v.x = v.x.plus(dX)
                    } else {
                        if ((newY - oldY) > 25) {
                            isCardMovedDown = true
                        } else {
                            v.y = v.y.plus(dY)
                            v.x = v.x.plus(dX)
                        }

                    }

                    return true
                }
                else -> return super.onTouchEvent(event)
            }
        return super.onTouchEvent(event)
    }

    private fun resetCardPosition(view: View?) {
        view?.let {
            resetCard(it)
        }

        viewAdapter?.let {
            if (swipeIndex >= 0) {
                if (it.getCount() > swipeIndex) {
                    cardGestureListeners?.onSwipeCancel(
                        swipeIndex,
                        it.getItem(swipeIndex)
                    )
                }
            }

        }
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

    private fun isCardDown(view: View?): Boolean {

//        if (view != null) {
//            return view.y + view.height / 2 > downBoundary
//        }
        val isDown = isCardMovedDown
        isCardMovedDown = false
        return isDown
    }

    private fun dismissCard(card: View, xPos: Int, rotate: Boolean) {
        card.animate()
            .x(xPos.toFloat())
            .y(0F)
            .rotation(
                if (rotate) {
                    if (xPos > 0) 45f else -45f
                } else 0f
            )
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
                            lastSwipedView = viewArray.last()
                            viewArray.removeLast()
                            viewAdapter?.let {
                                if (it.getCount() > swipeIndex)
                                    cardGestureListeners?.onItemShow(
                                        swipeIndex,
                                        it.getItem(swipeIndex)
                                    )
                            }
                            setCardForAnimation()
                            //  reOrderMargins()
                        }
                    }
                }

                override fun onAnimationCancel(animation: Animator?) {

                }

                override fun onAnimationRepeat(animation: Animator?) {

                }
            })
    }
    private fun presentCardAnim(card: View, yPos: Int) {
        card.visibility = VISIBLE
        val animate = TranslateAnimation(
            0F,  // fromXDelta
            0F,  // toXDelta
            -screenHeight.toFloat()/2,  // fromYDelta
            centerY
        ) // toYDelta
        animate.duration = 300
        animate.fillAfter = true
        card.startAnimation(animate)
    }


    private fun resetCard(card: View) {
        card.animate()
            .x(resetX)
            .y(mainView!!.y)
            .rotation(0F)
            .setInterpolator(OvershootInterpolator()).duration = 300
    }
}