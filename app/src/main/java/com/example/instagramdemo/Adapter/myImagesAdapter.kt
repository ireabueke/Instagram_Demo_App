package com.example.instagramdemo.Adapter

import android.content.Context
import android.media.Image
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.annotation.NonNull
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.RecyclerView
import com.example.instagramdemo.Fragments.PostDetailsFragment
import com.example.instagramdemo.Model.Post
import com.example.instagramdemo.R
import com.squareup.picasso.Picasso

class myImagesAdapter(private val mContext: Context, private val mPost: List<Post>) :
    RecyclerView.Adapter<myImagesAdapter.MyViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val view = LayoutInflater.from(mContext).inflate(R.layout.image_item_layout, parent, false)
        return MyViewHolder(view)
    }

    override fun getItemCount(): Int {
        return mPost.size
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val post = mPost[position]
        Picasso.get().load(post.getPostImage()).into(holder.image)


        holder.image.setOnClickListener {
            val editor = mContext.getSharedPreferences("PREFS", Context.MODE_PRIVATE).edit()
            editor.putString("postId", post.getPostId()).toString()
            editor.apply()
            (mContext as FragmentActivity).supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, PostDetailsFragment()).commit()
        }
    }

    inner class MyViewHolder(@NonNull itemView: View) : RecyclerView.ViewHolder(itemView) {

        val image: ImageView

        init {
            image = itemView.findViewById(R.id.post_image)
        }

    }


}