package com.subwatch.ui

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.subwatch.R
import com.subwatch.data.Subscription
import com.subwatch.util.Dates
import coil.load

class SubscriptionAdapter(
    private val onClick: (Subscription) -> Unit
) : RecyclerView.Adapter<SubscriptionAdapter.VH>() {

    private val items = mutableListOf<Subscription>()

    fun submit(list: List<Subscription>) {
        items.clear()
        items.addAll(list)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.item_subscription, parent, false)
        return VH(v, onClick)
    }

    override fun onBindViewHolder(holder: VH, position: Int) = holder.bind(items[position])

    override fun getItemCount(): Int = items.size

    class VH(itemView: View, val onClick: (Subscription) -> Unit) : RecyclerView.ViewHolder(itemView) {
        private val icon: ImageView = itemView.findViewById(R.id.icon)
        private val name: TextView = itemView.findViewById(R.id.name)
        private val dates: TextView = itemView.findViewById(R.id.dates)
        private val daysLeft: TextView = itemView.findViewById(R.id.daysLeft)

        fun bind(sub: Subscription) {
            val end = Dates.epochDayToLocalDate(sub.endDateEpochDay)
            val start = Dates.epochDayToLocalDate(sub.startDateEpochDay)
            val left = Dates.daysLeft(end)

            name.text = sub.name
            dates.text = "${Dates.format(start)} → ${Dates.format(end)}"
            daysLeft.text = if (left >= 0) "${left}d" else "expired"

            val color = when {
                left < 0 -> R.color.sw_gray
                left < 5 -> R.color.sw_red
                else -> R.color.sw_green
            }
            daysLeft.setTextColor(itemView.context.getColor(color))

            val domain = sub.domain?.trim()?.removePrefix("http://")?.removePrefix("https://")?.removePrefix("www.")
            val url = if (!domain.isNullOrBlank()) {
                // Favicon from Google (no API key): returns a site icon if available
                "https://www.google.com/s2/favicons?domain=$domain&sz=128"
            } else null

            if (url != null) {
                icon.load(url) {
                    crossfade(true)
                    placeholder(android.R.drawable.sym_def_app_icon)
                    error(android.R.drawable.sym_def_app_icon)
                }
            } else {
                icon.setImageResource(android.R.drawable.sym_def_app_icon)
            }

            itemView.setOnClickListener { onClick(sub) }
        }
    }
}
