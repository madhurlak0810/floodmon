package com.example.floodmon

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class MyAdapter(private val fList : ArrayList<Locations>) : RecyclerView.Adapter<MyAdapter.MyViewHolder>() {


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {

        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.item,
            parent,false)
        return MyViewHolder(itemView)

    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {

        val currentitem = fList[position]

        holder.locName.text=currentitem.LocName
        holder.temperature.text=currentitem.Temperature
        holder.humidity.text = currentitem.Humidity
        holder.distance.text = currentitem.Distance
        holder.water.text=currentitem.WaterFlow
        holder.alert.text=currentitem.AlertStatus

    }

    override fun getItemCount(): Int {

        return fList.size
    }


    class MyViewHolder(itemView : View) : RecyclerView.ViewHolder(itemView){

        val locName:TextView=itemView.findViewById(R.id.locName)
        val temperature : TextView = itemView.findViewById(R.id.temp)
        val humidity : TextView = itemView.findViewById(R.id.humidity)
        val distance : TextView = itemView.findViewById(R.id.distance)
        val alert: TextView= itemView.findViewById(R.id.alert)
        val water:TextView=itemView.findViewById(R.id.waterFlow)

    }

}
