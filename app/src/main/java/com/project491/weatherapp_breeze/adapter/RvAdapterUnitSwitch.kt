package com.project491.weatherapp_breeze.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.project491.weatherapp_breeze.data.ForecastModels.ForecastData
import com.project491.weatherapp_breeze.databinding.RvItemLayoutBinding
import com.squareup.picasso.Picasso
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class RvAdapterUnitSwitch(private val forecastArray: ArrayList<ForecastData>): RecyclerView.Adapter<RvAdapterUnitSwitch.ViewHolder>() {
    class ViewHolder(val rvItemBinding: RvItemLayoutBinding): RecyclerView.ViewHolder(rvItemBinding.root) {

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(RvItemLayoutBinding.inflate(LayoutInflater.from(parent.context), parent, false))
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val currentItem = forecastArray[position]
        holder.rvItemBinding.apply {
            val imageIcon = currentItem.weather[0].icon
            val imageUrl = "https://api.openweathermap.org/img/w/$imageIcon.png"

            Picasso.get().load(imageUrl).into(imgItem)

            tvItemTemp.text = "${ currentItem.main.temp.toInt()} °F"
            tvItemStatus.text = "${currentItem.weather[0].description}"
            tvItemTime.text = displayTime(currentItem.dt_txt)
        }
    }

    private fun displayTime(dtText: String): CharSequence? {
        val input = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
        val output = DateTimeFormatter.ofPattern("MM-dd HH:mm")
        val dateTime = LocalDateTime.parse(dtText,input)
        return output.format(dateTime)

    }

    override fun getItemCount(): Int {
        return forecastArray.size
    }
}