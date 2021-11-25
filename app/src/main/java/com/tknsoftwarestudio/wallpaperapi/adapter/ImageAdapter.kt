package com.tknsoftwarestudio.wallpaperapi.adapter

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.tknsoftwarestudio.wallpaperapi.R
import com.tknsoftwarestudio.wallpaperapi.view.SetWallpaperActivity
import com.tknsoftwarestudio.wallpaperapi.models.Photo

class ImageAdapter(var context : Context,var list : ArrayList<Photo>) : RecyclerView.Adapter<ImageAdapter.ImageViewHolder>() {

    class ImageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        val imageView = itemView.findViewById<ImageView>(R.id.imageView)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ImageViewHolder {
        val view : View = LayoutInflater.from(context).inflate(R.layout.image_view_holder,parent,false)

        return ImageViewHolder(view)
    }

    override fun onBindViewHolder(holder: ImageViewHolder, position: Int) {
        Glide.with(context).load(list[position].urls.regular).into(holder.imageView)

        holder.imageView.setOnClickListener {
            val intent = Intent(context, SetWallpaperActivity::class.java)
            intent.putExtra("image",list[position].urls.regular)
            intent.putExtra("downloadUrl",list[position].urls.full)
            intent.putExtra("alt_description",list[position].alt_description)
            intent.putExtra("created_at",list[position].created_at)
            intent.putExtra("user_name",list[position].user.name)
            intent.putExtra("user_profile_image",list[position].user.profile_image.medium)
            intent.putExtra("user_bio",list[position].user.bio)


            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            context.startActivity(intent)
        }
    }

    override fun getItemCount(): Int {
        return list.size
    }


}