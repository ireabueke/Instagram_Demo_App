package com.example.instagramdemo.Adapter

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.NonNull
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.RecyclerView
import com.example.instagramdemo.CommentActivity
import com.example.instagramdemo.Fragments.PostDetailsFragment
import com.example.instagramdemo.Fragments.ProfileFragment
import com.example.instagramdemo.MainActivity
import com.example.instagramdemo.Model.Post
import com.example.instagramdemo.Model.User
import com.example.instagramdemo.R
import com.example.instagramdemo.ShowUsersActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.squareup.picasso.Picasso
import de.hdodenhof.circleimageview.CircleImageView
import kotlinx.android.synthetic.main.activity_comment.*

class PostAdapter(private val mContext: Context, private val mPost: List<Post>) :
    RecyclerView.Adapter<PostAdapter.viewHolder>() {

    private var firebaseUser: FirebaseUser? = null
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): viewHolder {
        val view = LayoutInflater.from(mContext).inflate(R.layout.post_layout, parent, false)
        return viewHolder(view)
    }

    override fun getItemCount(): Int {
        return mPost.size
    }

    override fun onBindViewHolder(holder: viewHolder, position: Int) {
        val post = mPost[position]

        firebaseUser = FirebaseAuth.getInstance().currentUser!!
        Picasso.get().load(post.getPostImage()).into(holder.postImage)
        if (post.getDescription().isEmpty()) {
            holder.description.visibility = View.GONE
        } else {
            holder.description.visibility = View.VISIBLE
            holder.description.text = post.getDescription()
        }
        publisherIfo(holder.usreName, holder.publisher, post.getPublisher(), holder.profileImage)
        numberOfLike(holder.likes, post.getPostId())
        isLike(post.getPostId(), holder.likeButtn)
        checkSavedStatus(post.getPostId(), holder.saveButton)

        getTotalComment(holder.comments, post.getPostId())

        holder.likes.setOnClickListener {
            val intent = Intent(mContext, ShowUsersActivity::class.java)
            intent.putExtra("id", post.getPostId())
            intent.putExtra("title", "likes")
            mContext.startActivity(intent)
        }

        holder.commentButton.setOnClickListener {
            val intentComment = Intent(mContext, CommentActivity::class.java)
            intentComment.putExtra("postId", post.getPostId())
            intentComment.putExtra("publisherId", post.getPublisher())
            mContext.startActivity(intentComment)
        }

        holder.comments.setOnClickListener {
            val intentComment = Intent(mContext, CommentActivity::class.java)
            intentComment.putExtra("postId", post.getPostId())
            intentComment.putExtra("publisherId", post.getPublisher())
            mContext.startActivity(intentComment)
        }

        holder.likeButtn.setOnClickListener {
            if (holder.likeButtn.tag == "Like") {
                FirebaseDatabase.getInstance().reference.child("Likes")
                    .child(post.getPostId()).child(firebaseUser!!.uid).setValue(true)
                addNotification(post.getPublisher(), post.getPostId())
            } else {
                FirebaseDatabase.getInstance().reference.child("Likes")
                    .child(post.getPostId()).child(firebaseUser!!.uid).removeValue()
                val intent = Intent(mContext, MainActivity::class.java)
                mContext.startActivity(intent)
            }

            holder.postImage.setOnClickListener {
                val editor = mContext.getSharedPreferences("PREFS", Context.MODE_PRIVATE).edit()
                editor.putString("postId", post.getPostId())
                editor.apply()
                (mContext as FragmentActivity).supportFragmentManager.beginTransaction()
                    .replace(R.id.fragment_container, PostDetailsFragment()).commit()
            }

            holder.publisher.setOnClickListener {
                val editor = mContext.getSharedPreferences("PREFS", Context.MODE_PRIVATE).edit()
                editor.putString("profileId", post.getPublisher())
                editor.apply()
                (mContext as FragmentActivity).supportFragmentManager.beginTransaction()
                    .replace(R.id.fragment_container, ProfileFragment()).commit()
            }
            holder.profileImage.setOnClickListener {
                val editor = mContext.getSharedPreferences("PREFS", Context.MODE_PRIVATE).edit()
                editor.putString("profileId", post.getPublisher())
                editor.apply()
                (mContext as FragmentActivity).supportFragmentManager.beginTransaction()
                    .replace(R.id.fragment_container, ProfileFragment()).commit()
            }
        }

        holder.saveButton.setOnClickListener {
            if (holder.saveButton.tag == "Save") {
                FirebaseDatabase.getInstance().reference.child("Save")
                    .child(firebaseUser!!.uid).child(post.getPostId()).setValue(true)
            } else {
                FirebaseDatabase.getInstance().reference.child("Save")
                    .child(firebaseUser!!.uid).child(post.getPostId()).removeValue()
            }
        }
    }

    private fun numberOfLike(likes: TextView, postId: String) {
        val likeRef = FirebaseDatabase.getInstance().reference.child("Likes").child(postId)
        likeRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(p0: DataSnapshot) {
                if (p0.exists()) {
                    likes.visibility = View.VISIBLE
                    likes.text = p0.childrenCount.toString() + " Likes"
                } else {
                    likes.visibility = View.GONE
                }
            }

            override fun onCancelled(p0: DatabaseError) {}
        })
    }

    private fun getTotalComment(comments: TextView, postId: String) {
        val commentRef = FirebaseDatabase.getInstance().reference.child("Comments").child(postId)
        commentRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(p0: DataSnapshot) {
                if (p0.exists()) {
                    comments.visibility = View.VISIBLE
                    comments.text = "View all " + p0.childrenCount.toString() + " comments"
                } else {
                    comments.visibility = View.GONE
                }
            }

            override fun onCancelled(p0: DatabaseError) {}
        })
    }

    private fun isLike(postId: String, likeButtn: ImageView) {
        val firebaseUser = FirebaseAuth.getInstance().currentUser
        val likeRef = FirebaseDatabase.getInstance().reference.child("Likes").child(postId)
        likeRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(p0: DataSnapshot) {
                if (p0.child(firebaseUser!!.uid).exists()) {
                    likeButtn.setImageResource(R.drawable.heart_clicked)
                    likeButtn.tag = "Liked"
                } else {
                    likeButtn.setImageResource(R.drawable.heart_not_clicked)
                    likeButtn.tag = "Like"
                }
            }

            override fun onCancelled(p0: DatabaseError) {}
        })


    }

    inner class viewHolder(@NonNull itemView: View) : RecyclerView.ViewHolder(itemView) {

        var profileImage: CircleImageView
        var postImage: ImageView
        var likeButtn: ImageView
        var commentButton: ImageView
        var saveButton: ImageView
        var usreName: TextView
        var likes: TextView
        var publisher: TextView
        var description: TextView
        var comments: TextView

        init {
            profileImage = itemView.findViewById(R.id.user_profile_image_post)
            likeButtn = itemView.findViewById(R.id.post_image_like_btn)
            postImage = itemView.findViewById(R.id.post_image)
            commentButton = itemView.findViewById(R.id.post_image_comment_btn)
            saveButton = itemView.findViewById(R.id.post_save_comment_btn)
            usreName = itemView.findViewById(R.id.user_name_post)
            likes = itemView.findViewById(R.id.likes)
            publisher = itemView.findViewById(R.id.publisher)
            description = itemView.findViewById(R.id.description)
            comments = itemView.findViewById(R.id.comments)

        }
    }

    //    postImage: ImageView,
    private fun publisherIfo(
        usreName: TextView,
        publisher: TextView,
        postId: String,
        profileImage: ImageView
    ) {
        val userRef = FirebaseDatabase.getInstance().reference.child("Users").child(postId)

        userRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(p0: DataSnapshot) {
                if (p0.exists()) {
                    val user = p0.getValue<User>(User::class.java)
//                    Picasso.get().load(post.getPostImage()).into(holder.postImage)
                    Picasso.get().load(user?.getImage()).placeholder(R.drawable.profile_icon)
                        .into(profileImage)
                    usreName.text = user?.getUsername()
                    publisher.text = user?.getFullname()
                }
            }

            override fun onCancelled(p0: DatabaseError) {}
        })
    }

    private fun checkSavedStatus(postId: String, imageView: ImageView) {
        val saveRef = FirebaseDatabase.getInstance().reference.child("Save")
            .child(firebaseUser!!.uid)
        saveRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(p0: DataSnapshot) {
                if (p0.child(postId).exists()) {
                    imageView.setImageResource(R.drawable.save_large_icon)
                    imageView.tag = "Saved"
                } else {
                    imageView.setImageResource(R.drawable.save_unfilled_large_icon)
                    imageView.tag = "Save"
                }
            }

            override fun onCancelled(p0: DatabaseError) {}
        })
    }

    private fun addNotification(userId: String, postId: String) {
        val notitRef = FirebaseDatabase.getInstance().reference.child("Notification").child(userId)
        val notiMap = HashMap<String, Any>()
        notiMap["userid"] = firebaseUser!!.uid
        notiMap["text"] = "like your post"
        notiMap["postid"] = postId
        notiMap["ispost"] = true

        notitRef.push().setValue(notiMap)
    }

}