package com.ravi.basisdemoapp.card

import android.view.View
import java.text.FieldPosition

abstract class ViewAdapter {
    abstract  fun getItem(position: Int):Any
    abstract  fun getView(position: Int):View
    abstract fun getCount(): Int

    var dataListeners:DataListeners?=null
    var actionListeners:ActionListeners?=null
    interface DataListeners{
       fun notifyAppendData()
    }

    interface ActionListeners{
        fun right()
        fun up()
        fun left()
        fun down()
    }

    fun up() = actionListeners?.up()
    fun down() = actionListeners?.down()
    fun left() = actionListeners?.left()
    fun right() = actionListeners?.right()
}