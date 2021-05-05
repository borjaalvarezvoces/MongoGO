package com.borja.mongogo

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide

class PhotoAdapter(private val photos: List<Photo>) :
    RecyclerView.Adapter<PhotoAdapter.ViewHolder>() {
    /*
        el context se puede utilizar de cualquier vista en este caso del parent.
    */
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater
            .from(parent.context)
            .inflate(R.layout.view_photo, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val photo = photos[position]
        holder.bind(photo)
    }

    override fun getItemCount(): Int = photos.size

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {

        private val cover = view.findViewById<ImageView>(R.id.cover_id)
        private val title = view.findViewById<TextView>(R.id.title_id)

        fun bind(photo: Photo) {
            title.text = photo.title
            Glide.with(cover.context).load(photo.cover).into(cover)

        }
    }
}