package com.app.design_motion.ui.movie

import android.os.Build
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import coil.api.load
import com.app.design_motion.R
import com.app.design_motion.common.TRANSITION_CARD
import com.app.design_motion.data.model.Subject
import com.google.common.base.Strings
import kotlinx.android.synthetic.main.item_card.view.*

class MovieAdapter(
    private val delegate: ViewHolder.Delegate
) : ListAdapter<Subject, MovieAdapter.ViewHolder>(diffCallback) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val inflater =
            LayoutInflater.from(parent.context).inflate(R.layout.item_card, parent, false)

        return ViewHolder(inflater, delegate)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = getItem(position)
        holder.apply {
            bind(item)
            itemView.tag = item
        }
    }

    class ViewHolder(view: View, private val delegate: Delegate) : RecyclerView.ViewHolder(view),
        View.OnClickListener {

        private lateinit var subject: Subject

        init {
            itemView.setOnClickListener(this)
        }

        fun bind(data: Subject) {
            this.subject = data
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                itemView.transitionName = TRANSITION_CARD + adapterPosition
            }
            itemView.item_title.text = data.title
            itemView.img_card.load(data.images.large){
                crossfade(true)
                allowHardware(false)
            }

            val pubCountry = StringBuffer()
            for (i in data.pubdates.indices) {
                data.pubdates[i].indexOf("(")
                pubCountry.append(
                    data.pubdates[i].substring(
                        data.pubdates[i].indexOf("(") + 1,
                        data.pubdates[i].indexOf(")")
                    ) + " "
                )
            }

            val genres = StringBuffer()
            for (i in data.genres.indices) {
                genres.append(data.genres[i] + " ")
            }

            val casts = StringBuffer()
            for (i in data.casts.indices) {
                casts.append(data.casts[i].name + " ")
            }

            itemView.item_description.text = (
                    data.year + " / " + pubCountry.toString() + " / " + genres.toString() + (if (data.directors.isEmpty()) " " else " / " +
                            data.directors.get(0).name) + if (Strings.isNullOrEmpty(casts.toString())) " " else " / $casts"
                    )
        }

        interface Delegate {
            fun onItemClick(view: View, subject: Subject)
        }

        override fun onClick(view: View) {
            delegate.onItemClick(view, subject)
        }
    }

    companion object {
        private val diffCallback = object : DiffUtil.ItemCallback<Subject>() {
            override fun areItemsTheSame(oldItem: Subject, newItem: Subject): Boolean =
                oldItem.id == newItem.id

            override fun areContentsTheSame(oldItem: Subject, newItem: Subject): Boolean =
                oldItem == newItem
        }
    }

}
