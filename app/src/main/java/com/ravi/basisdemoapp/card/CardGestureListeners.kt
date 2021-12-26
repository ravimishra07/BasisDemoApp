package com.ravi.basisdemoapp.card

interface CardGestureListeners {

    fun onItemShow(position: Int, model: Any)
    fun onSwipeCancel(position: Int, model: Any)
    fun onSwipeCompleted()
}