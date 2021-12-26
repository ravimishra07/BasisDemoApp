package com.ravi.basisdemoapp.card

import android.view.View
import java.text.FieldPosition
/** abstract class for out main view adapter, with all possible functions we need to override */
abstract class ViewAdapter {
    abstract  fun getItem(position: Int):Any
    abstract  fun getView(position: Int):View
    abstract fun getCount(): Int

    var actionListeners:ActionListeners?=null

    interface ActionListeners{
        fun right()
        fun left()
    }
    fun left() = actionListeners?.left()
    fun right() = actionListeners?.right()
}