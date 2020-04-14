package com.example.instagramdemo.Adapter

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.RecyclerView
import com.example.instagramdemo.AddStoryActivity
import com.example.instagramdemo.Model.Story
import com.example.instagramdemo.Model.User
import com.example.instagramdemo.R
import com.example.instagramdemo.StoryActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.squareup.picasso.Picasso
import de.hdodenhof.circleimageview.CircleImageView

class StoryAdapter(private val mContext: Context, private val mStory: List<Story>) :
    RecyclerView.Adapter<StoryAdapter.ViewHolder>() {
    var counter = 0
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return if (viewType == 0) {
            ViewHolder(
                LayoutInflater.from(mContext).inflate(R.layout.add_story_item, parent, false)
            )
        } else {
            ViewHolder(LayoutInflater.from(mContext).inflate(R.layout.story_item, parent, false))
        }
    }

    override fun getItemCount(): Int {
        return mStory.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val story = mStory[position]
        userInfo(holder, story.getUserId(), position)
        if (holder.adapterPosition != 0) {
            seenStory(holder, story.getUserId())
        }
        if (holder.adapterPosition == 0) {
            myStories(holder.addStoryTv!!, holder.addStoryBtn!!, false)
        }

        holder.itemView.setOnClickListener {
            if (holder.adapterPosition == 0) {
                myStories(holder.addStoryTv!!, holder.addStoryBtn!!, true)
            } else {
                val intent = Intent(mContext, StoryActivity::class.java)
                intent.putExtra("userId", story.getUserId())
                //intent.putExtra("counter", counter)
                mContext.startActivity(intent)
            }
        }

        holder.addStoryBtn?.setOnClickListener {
            val intent = Intent(mContext, AddStoryActivity::class.java)
            intent.putExtra("userId", story.getUserId())
            mContext.startActivity(intent)
        }

    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        //Story item
        var story_image: CircleImageView? = null
        var storyImageSeen: CircleImageView? = null
        var storyUserName: TextView? = null

        //Add story item
        var addStoryBtn: ImageView? = null
        var addStory: CircleImageView? = null
        var addStoryTv: TextView? = null

        init {
            //Story item
            story_image = itemView.findViewById(R.id.story_image)
            storyImageSeen = itemView.findViewById(R.id.story_image_seen)
            storyUserName = itemView.findViewById(R.id.story_username)

            //Add story item add_story_text
            addStoryBtn = itemView.findViewById(R.id.story_add)
            addStoryTv = itemView.findViewById(R.id.add_story_text)
        }
    }

    override fun getItemViewType(position: Int): Int {
        return if (position == 0) {
            0
        } else {
            1
        }
    }

    private fun userInfo(viewHolder: ViewHolder, userId: String, position: Int) {
        val userRef = FirebaseDatabase.getInstance().reference.child("Users").child(userId)
        userRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(p0: DataSnapshot) {

                if (p0.exists()) {
                    val user = p0.getValue(User::class.java)

                    Picasso.get().load(user!!.getImage()).placeholder(R.drawable.profile)
                        .into(viewHolder.story_image)
                    if (position != 0) {
                        Picasso.get().load(user.getImage()).placeholder(R.drawable.profile)
                            .into(viewHolder.storyImageSeen)
                        viewHolder.storyUserName!!.text = user.getUsername()
                    }
                }
            }

            override fun onCancelled(p0: DatabaseError) {}
        })
    }

    private fun myStories(textView: TextView, imageView: ImageView, click: Boolean) {
        val ref = FirebaseDatabase.getInstance().reference.child("Story")
            .child(FirebaseAuth.getInstance().currentUser!!.uid)
        ref.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(p0: DataSnapshot) {

                val timeCurent = System.currentTimeMillis()
                for (snapshot in p0.children) {
                    val story = snapshot.getValue(Story::class.java)
                    if (timeCurent > story!!.getTimeStart() && timeCurent < story.getTimeEnd()) {
                        counter++
                    }
                }
                if (click) {
                    if (counter > 0) {
                        val alertDialog = AlertDialog.Builder(mContext).create()
                        alertDialog.setButton(
                            AlertDialog.BUTTON_NEUTRAL,
                            "View Story"
                        ) { dialog, which ->
                            val intent = Intent(mContext, StoryActivity::class.java)
                            intent.putExtra("userId", FirebaseAuth.getInstance().currentUser!!.uid)
                            mContext.startActivity(intent)
                            dialog.dismiss()
                        }

                        alertDialog.setButton(
                            AlertDialog.BUTTON_POSITIVE,
                            "Add Story"
                        ) { dialog, which ->
                            val intent = Intent(mContext, AddStoryActivity::class.java)
                            intent.putExtra("userId", FirebaseAuth.getInstance().currentUser!!.uid)
                            mContext.startActivity(intent)
                            dialog.dismiss()
                        }
                        alertDialog.show()
                    } else {
                        val intent = Intent(mContext, AddStoryActivity::class.java)
                        intent.putExtra("userId", FirebaseAuth.getInstance().currentUser!!.uid)
                        mContext.startActivity(intent)
                    }
                } else {
                    if (counter > 0) {
                        textView.text = "My story"
                        imageView.visibility = View.GONE
                    } else {
                        textView.text = "Add story"
                        imageView.visibility = View.VISIBLE
                    }
                }
            }

            override fun onCancelled(p0: DatabaseError) {}
        })
    }

    private fun seenStory(viewHolder: ViewHolder, userId: String) {
        val ref = FirebaseDatabase.getInstance().reference.child("Story").child(userId)
        ref.addValueEventListener(object : ValueEventListener {
            var i = 0
            override fun onDataChange(p0: DataSnapshot) {
                for (snapshot in p0.children) {
                    if (!snapshot.child("views").child(
                            FirebaseAuth
                                .getInstance().currentUser!!.uid
                        ).exists()
                        && System.currentTimeMillis() < snapshot.getValue(Story::class.java)!!
                            .getTimeEnd()
                    ) {
                        i++
                    }
                }
                if (i > 0) {
                    viewHolder.story_image!!.visibility = View.VISIBLE
                    viewHolder.storyImageSeen!!.visibility = View.GONE
                } else {
                    viewHolder.story_image!!.visibility = View.GONE
                    viewHolder.storyImageSeen!!.visibility = View.VISIBLE
                }
            }

            override fun onCancelled(p0: DatabaseError) {}
        })
    }

}