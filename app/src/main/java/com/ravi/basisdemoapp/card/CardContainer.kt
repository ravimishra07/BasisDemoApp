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
import android.view.animation.TranslateAnimation
import android.widget.FrameLayout
import androidx.cardview.widget.CardView
import androidx.core.view.contains
import com.ravi.basisdemoapp.R

class CardContainer(context: Context, attrs: AttributeSet?) : FrameLayout(context, attrs),
    ViewAdapter.ActionListeners,
    View.OnTouchListener {

    var cardGestureListeners: CardGestureListeners? = null
    fun setActionListeners(cardGestureListeners: CardGestureListeners) {
        this.cardGestureListeners = cardGestureListeners
    }

    var viewAdapter: ViewAdapter? = null
    private var mainView: FrameLayout? = null
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
    private var centerY = 0f
    private val cardDegreesForTransform = 40.0f
    private var isFirstTimeMove = false
    private var isCardMovedDown = false
    private var lastViewIndex = 0
    private var downSwipeCount = 0

    private var viewArray: ArrayList<View> = arrayListOf()
    private var lastSwipedView: View? = null
    private var fixedArray: ArrayList<View> = arrayListOf()
    private var maxValue = 0;

    init {
        screenHeight = Resources.getSystem().displayMetrics.heightPixels
        screenWidth = Resources.getSystem().displayMetrics.widthPixels

        /** created boundaries to determine swipe events*/
        leftBoundary = screenWidth * (0.25f)
        rightBoundary = screenWidth * (0.75f)
        downBoundary = screenHeight * (0.75f)
        setUpSurface()

    }

    /** inflated card view */
    private fun setUpSurface() {
        val viewMain = LayoutInflater.from(context).inflate(R.layout.card_view_container, null)
        mainView = viewMain.findViewById(R.id.mainView)
        draggableSurfaceLayout = viewMain.findViewById(R.id.draggableView)
        addView(viewMain)
    }

    fun setAdapter(viewAdapter: ViewAdapter) {
        reset()
        this.viewAdapter = viewAdapter
        this.viewAdapter?.actionListeners = this

        if (viewAdapter.getCount() > 0) {
            mainView?.visibility = View.VISIBLE
        } else {
            return
        }
        centerY = mainView?.y ?: 0f
        viewAdapter.getCount().also { maxValue = it }
        formCards(1)
    }

    /**
     * @param lastIndex: top most index which the user will see
     * @param isPrev: to tell if user has swiped down and prev card will be shown
     */
    private fun formCards(lastIndex: Int, isPrev: Boolean? = false) {

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
                    /**form cards and appedn in array to use as data source*/
                    viewArray.add(childCard)
                }

                // appending views in our main frame layout
                for (view in viewArray) {
                    mainView?.addView(view)
                }
                val lastView = viewArray.last()

                // here we check is we should show top card with animation or not
                if (isPrev == true) {
                    downSwipeCardPresentAnimation(lastView)
                }
                count = viewArray.size
                setCardForAnimation()
                fixedArray = viewArray
            }
        }

    }

    /** sets touch listener on the top most view */
    private fun setCardForAnimation() {
        if (viewArray.isNotEmpty()) {
            viewArray[viewArray.size - 1].setOnTouchListener(this)
        } else {
            mainView?.visibility = View.GONE
        }
    }

    private fun reset() {
        swipeIndex = 0
        count = 0
        viewArray.clear()
        mainView?.removeAllViews()
    }


    /** moves the view to the right and dismiss after it has moved beyond the right boundary*/
    override fun right() {
        if (viewArray.isNotEmpty()) {
            if (viewArray[viewArray.size - 1].animation?.hasEnded() == false)
                return
            dismissCard(viewArray[viewArray.size - 1], (screenWidth * 2), true)
            viewAdapter?.let {
                if (it.getCount() > swipeIndex) { // updating counts
                    swipeIndex++
                    lastViewIndex++
                }
            }
        }
    }

    /** moves the view to the left and dismiss after it has moved beyond the left boundary*/
    override fun left() {
        if (viewArray.isNotEmpty()) {
            if (viewArray[viewArray.size - 1].animation?.hasEnded() == false)
                return
            dismissCard(viewArray[viewArray.size - 1], -(screenWidth * 2), true)
            viewAdapter?.let {
                if (it.getCount() > swipeIndex) {// updating counts
                    swipeIndex++
                    lastViewIndex++
                }
            }
        }
    }


    override fun onTouch(v: View?, event: MotionEvent?): Boolean {
        if (v != null)
            when (event?.action) {
                /**
                 *  MotionEvent.ACTION_DOWN: when user first tap the screen we register it and save in our variable(oldX and oldY)
                 *              we save isFirstTimeMove as true
                 */
                MotionEvent.ACTION_DOWN -> {
                    isFirstTimeMove = mainView?.contains(v) == true
                    oldX = event.x
                    oldY = event.y
                    return true
                }
                /**
                 * @MotionEvent.ACTION_UP: when user removes touch, we determine if the user has swiped right, left or down, we determine the operation
                 */
                MotionEvent.ACTION_UP -> {
                    isFirstTimeMove = false
                    when {
                        //if swiped left, dismiss the view post animation
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
                        //if swiped right, dismiss the view post animation
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
                        //if swiped down, reset the adapter data and populate subData with only desired range
                        isCardDown() -> {
                            downSwipeCount++
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

                        // reposition dragged view
                        else -> {
                            downSwipeCount = 0
                            resetCardPosition(v)
                        }
                    }
                    return true
                }
                /**
                 *  MotionEvent.ACTION_MOVE: we continuously update our newX and newY as the user drags the screen
                 *                  When user drags down slight movement of 25 px was allowed(to make view look flexible)
                 */
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
    }

    /** rotating the card while being dragged */
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
        val isDown = isCardMovedDown
        isCardMovedDown = false
        return isDown
    }

    /**
     * @param card: view which is to dismissed post animation
     * @param xPos: horizontal translation (-ve for left and +ve right)
     */
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
                        }
                    }
                }

                override fun onAnimationCancel(animation: Animator?) {}
                override fun onAnimationRepeat(animation: Animator?) {}
            })
    }

    /** animate card to transform from top to current position with animation*/
    private fun downSwipeCardPresentAnimation(card: View) {
        card.visibility = VISIBLE
        val animate = TranslateAnimation(
            0F,  // fromXDelta
            0F,  // toXDelta
            -screenHeight.toFloat() / 2,
            centerY
        )
        animate.duration = 300
        animate.fillAfter = true
        card.startAnimation(animate)
        viewAdapter?.let {
            cardGestureListeners?.onItemShow(
                viewArray.size,
                it.getItem(lastViewIndex - 1)
            )
        }
    }

    private fun resetCard(card: View) {
        card.animate()
            .x(resetX)
            .y(mainView!!.y)
            .rotation(0F)
            .setInterpolator(OvershootInterpolator()).duration = 300
    }
}