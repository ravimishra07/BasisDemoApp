package com.ravi.basisdemoapp.card

import android.content.Context
import android.content.res.Resources
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import com.ravi.basisdemoapp.R

class CardContainer(context:Context,attrs:AttributeSet?) :FrameLayout(context, attrs){
   var cardListeners:CardInterfaceListeners?=null
    fun setActionListeners(cardListeners: CardInterfaceListeners){
        this.cardListeners = cardListeners
    }
    var viewAdapter:ViewAdapter?=null

    val margin = 20.px
    val marginTop = 10.px
    val maxSta = 5

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
}