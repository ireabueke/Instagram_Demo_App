package com.example.instagramdemo

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.instagramdemo.Adapter.CommentAdapter
import com.example.instagramdemo.Model.Comment
import com.example.instagramdemo.Model.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.activity_account_settings.*
import kotlinx.android.synthetic.main.activity_comment.*
import kotlinx.android.synthetic.main.post_layout.*

class CommentActivity : AppCompatActivity() {

    var postId = ""
    var publisherId = ""
    private var firebaseUser: FirebaseUser? = null
    var commentAdapter: CommentAdapter? = null
    var commentList: MutableList<Comment>? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_comment)
        val intent = intent
        postId = intent.getStringExtra("postId")!!
        publisherId = intent.getStringExtra("publisherId")!!
        firebaseUser = FirebaseAuth.getInstance().currentUser
        commentList = ArrayList()
        commentAdapter = CommentAdapter(this, commentList!!)//if error

        val linearLayoutManager = LinearLayoutManager(this)
        linearLayoutManager.reverseLayout = true
        recycler_view_comment.layoutManager = linearLayoutManager
        recycler_view_comment.adapter = commentAdapter

        getUsersImage()
        readComment()
        getPostImage()

        post_comment.setOnClickListener {
            if (write_comment.text.toString().isEmpty()) {
                Toast.makeText(this, "comment field can't  be empty", Toast.LENGTH_LONG).show()
            } else {
                addComment()
            }
        }
    }

    private fun addComment() {
        val commentRef = FirebaseDatabase.getInstance().reference.child("Comments").child(postId)
        val commentMap = HashMap<String, Any>()

        commentMap["comment"] = write_comment.text.toString()
        commentMap["publisher"] = firebaseUser!!.uid
        commentRef.push().setValue(commentMap)
        addNotification()
        write_comment.text.clear()
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
    }


    private fun getUsersImage() {
        val userRef =
            FirebaseDatabase.getInstance().reference.child("Users").child(firebaseUser!!.uid)
        userRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(p0: DataSnapshot) {

                if (p0.exists()) {
                    val user = p0.getValue<User>(User::class.java)
                    Picasso.get().load(user!!.getImage()).placeholder(R.drawable.profile_icon)
                        .into(profile_image_comment)

                }
            }

            override fun onCancelled(p0: DatabaseError) {}
        })
    }

    private fun getPostImage() {
        val postRef =
            FirebaseDatabase.getInstance().reference.child("Posts").child(postId).child("postimage")
        postRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(p0: DataSnapshot) {

                if (p0.exists()) {
                    val image = p0.value.toString()
                    Picasso.get().load(image).placeholder(R.drawable.profile_icon)
                        .into(post_image_comment)

                }
            }

            override fun onCancelled(p0: DatabaseError) {}
        })
    }

    private fun readComment() {
        val commentRef = FirebaseDatabase.getInstance().reference.child("Comments")
            .child(postId)
        commentRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(p0: DataSnapshot) {
                if (p0.exists()) {
                    commentList?.clear()
                    for (snpashot in p0.children) {
                        val comment = snpashot.getValue(Comment::class.java)
                        commentList!!.add(comment!!)
                    }
                    commentAdapter?.notifyDataSetChanged()
                }
            }

            override fun onCancelled(p0: DatabaseError) {}
        })
    }

    private fun addNotification() {
        val notitRef =
            FirebaseDatabase.getInstance().reference.child("Notification").child(publisherId)
        val notiMap = HashMap<String, Any>()
        notiMap["userid"] = firebaseUser!!.uid
        notiMap["text"] = "Commented ${write_comment!!.text}"
        notiMap["postid"] = postId
        notiMap["ispost"] = true

        notitRef.push().setValue(notiMap)
    }
}
