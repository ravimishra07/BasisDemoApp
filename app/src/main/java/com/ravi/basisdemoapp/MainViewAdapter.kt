package com.ravi.basisdemoapp

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.widget.TextView
import com.ravi.basisdemoapp.card.ViewAdapter
import com.ravi.basisdemoapp.model.SubData

class MainViewAdapter(private val list:ArrayList<SubData>,context:Context):ViewAdapter() {

    var layoutInflater: LayoutInflater = LayoutInflater.from(context)

    override fun getItem(position: Int) = list[position]

    override fun getView(position: Int): View {
        val view = layoutInflater.inflate(R.layout.card_view, null)
        val tvDataText = view.findViewById<TextView>(R.id.tv_text)
        val tvCardNo = view.findViewById<TextView>(R.id.tv_card_no)
        val data = getItem(position)
        tvDataText.text = data.text
        val cardNo = position + 1
        tvCardNo.text = "Card #$cardNo of ${list.size}"
        return view
    }

    override fun getCount() = list.size
}