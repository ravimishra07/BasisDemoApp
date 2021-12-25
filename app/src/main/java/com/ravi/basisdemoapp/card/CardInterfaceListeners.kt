package com.ravi.basisdemoapp.card

interface CardInterfaceListeners {
    fun onLeftSwipe(position: Int, model: Any)
    fun onUpSwipe(position: Int, model: Any)
    fun onDownSwipe(position: Int, model: Any)
    fun onRightSwipe(position: Int, model: Any)
    fun onItemShow(position: Int, model: Any)
    fun onSwipeCancel(position: Int, model: Any)
    fun onSwipeCompleted()
}