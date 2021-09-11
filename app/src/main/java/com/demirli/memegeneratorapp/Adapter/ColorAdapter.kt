package com.demirli.memegeneratorapp.Adapter

import android.content.Context
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.demirli.memegeneratorapp.R

class ColorAdapter(internal var context: Context,
                   internal var listener: ColorAdapterClickListener): RecyclerView.Adapter<ColorAdapter.ColorViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ColorViewHolder {
        val itemView = LayoutInflater.from(context).inflate(R.layout.color_item, parent, false)
        return ColorViewHolder(itemView)
    }

    override fun getItemCount(): Int = colorList.size

    override fun onBindViewHolder(holder: ColorViewHolder, position: Int) {
        holder.color_selection.setCardBackgroundColor(colorList[position])
    }


    internal var colorList: List<Int>

    init {
        this.colorList = genColorList()
    }

    interface ColorAdapterClickListener {
        fun onColorItemSelected(color: Int)
    }

    private fun genColorList(): List<Int> {

        val colorList =  ArrayList<Int>()

        colorList.add(Color.parseColor("#131722"))
        colorList.add(Color.parseColor("#ff545e"))
        colorList.add(Color.parseColor("#57bb82"))
        colorList.add(Color.parseColor("#dbeeff"))
        colorList.add(Color.parseColor("#ba5796"))

        return colorList
    }

    inner class ColorViewHolder(itemView: View): RecyclerView.ViewHolder(itemView) {
        internal var color_selection: CardView
        init {
            color_selection = itemView.findViewById(R.id.color_selection) as CardView
            itemView.setOnClickListener{
                listener.onColorItemSelected(colorList[adapterPosition])
            }
        }
    }
}