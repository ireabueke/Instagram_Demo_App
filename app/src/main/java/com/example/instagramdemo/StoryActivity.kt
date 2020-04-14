package com.example.instagramdemo

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.MotionEvent
import android.view.View
import android.widget.Toast
import com.example.instagramdemo.Model.Story
import com.example.instagramdemo.Model.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.squareup.picasso.Picasso
import jp.shts.android.storiesprogressview.StoriesProgressView
import kotlinx.android.synthetic.main.activity_story.*

class StoryActivity : AppCompatActivity(), StoriesProgressView.StoriesListener {

    var currentUserId: String = ""
    var userId: String = ""
    var imagesList: List<String>? = null
    var storiesIdList: List<String>? = null
    var storyProgressVew: StoriesProgressView? = null
    private var counter: Int = 0
    private var pressTime = 0L
    private var limit = 500L
    private val onTouchListener = View.OnTouchListener { v, event ->
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                pressTime = System.currentTimeMillis()
                storyProgressVew!!.pause()
                return@OnTouchListener false
            }
            MotionEvent.ACTION_UP -> {
                val now = System.currentTimeMillis()
                storyProgressVew!!.resume()
                return@OnTouchListener limit < now - pressTime
            }
        }
        false
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_story)

        currentUserId = FirebaseAuth.getInstance().currentUser!!.uid
        userId = intent.getStringExtra("userId")!!
        //counter = intent.getIntExtra("counter",0)
        storyProgressVew = findViewById(R.id.stories_progress)

        layout_seen.visibility = View.GONE
        delete_story.visibility = View.GONE

        if (userId.equals(currentUserId)) {
            layout_seen.visibility = View.VISIBLE
            delete_story.visibility = View.VISIBLE
        }
        userInfo(userId)
        getStories(userId)


        val reverse: View = findViewById(R.id.reverse)
        reverse.setOnClickListener { storyProgressVew!!.reverse() }
        reverse.setOnTouchListener(onTouchListener)

        val skip: View = findViewById(R.id.skip)
        skip.setOnClickListener { storyProgressVew!!.skip() }
        skip.setOnTouchListener(onTouchListener)

        seen_number.setOnClickListener {
            val intent = Intent(this@StoryActivity, ShowUsersActivity::class.java)
            intent.putExtra("id", userId)
            intent.putExtra("storyId", storiesIdList!![counter])
            intent.putExtra("title", "Views")
            startActivity(intent)
        }

        delete_story.setOnClickListener {
            val ref = FirebaseDatabase.getInstance().reference.child("Story")
                .child(userId).child(storiesIdList!![counter])
            ref.removeValue().addOnCompleteListener {
                if (it.isSuccessful) {
                    Toast.makeText(this, "Deleted....", Toast.LENGTH_LONG).show()
                }
            }
        }

    }

    private fun getStories(userId: String) {
        imagesList = ArrayList()
        storiesIdList = ArrayList()

        val ref = FirebaseDatabase.getInstance().reference.child("Story")
            .child(userId)
        ref.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(p0: DataSnapshot) {
                (imagesList as ArrayList<String>).clear()
                (storiesIdList as ArrayList<String>).clear()

                for (snapshot in p0.children) {

                    val story: Story? = snapshot.getValue(Story::class.java)
                    val timeCurrent = System.currentTimeMillis()

                    if (timeCurrent > story!!.getTimeStart() && timeCurrent < story.getTimeEnd()) {
                        (imagesList as ArrayList<String>).add(story.getImageUrl())
                        (storiesIdList as ArrayList<String>).add(story.getStoryID())
                    }
                }
                storyProgressVew!!.setStoriesCount((imagesList as ArrayList<String>).size)
                storyProgressVew!!.setStoryDuration(5000L)
                storyProgressVew!!.setStoriesListener(this@StoryActivity)
                storyProgressVew!!.startStories(counter)
                Picasso.get().load(imagesList!![counter]).placeholder(R.drawable.profile)
                    .into(image_stories_display)
                addViewToStory(storiesIdList!![counter])
                seenNumber(storiesIdList!![counter])
            }

            override fun onCancelled(p0: DatabaseError) {}
        })
    }

    private fun addViewToStory(storyId: String) {
        val ref = FirebaseDatabase.getInstance().reference
            .child("Story").child(userId).child(storyId)
            .child("views").child(currentUserId).setValue(true)
    }

    private fun userInfo(userId: String) {
        val userRef = FirebaseDatabase.getInstance().reference.child("Users").child(userId)
        userRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(p0: DataSnapshot) {

                if (p0.exists()) {
                    val user = p0.getValue(User::class.java)
                    Picasso.get().load(user!!.getImage()).placeholder(R.drawable.profile)
                        .into(story_profile_image)
                    story_owner.text = user.getUsername()
                }
            }

            override fun onCancelled(p0: DatabaseError) {}
        })
    }

    private fun seenNumber(storyId: String) {
        val ref = FirebaseDatabase.getInstance().reference.child("Story")
            .child(userId).child(storyId).child("views")
        ref.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(p0: DataSnapshot) {
                seen_number.text = "${p0.childrenCount} view"
            }

            override fun onCancelled(p0: DatabaseError) {}
        })
    }

    override fun onComplete() {
        finish()
    }

    override fun onPrev() {
        Picasso.get().load(imagesList!![--counter]).placeholder(R.drawable.profile)
            .into(image_stories_display)
        addViewToStory(storiesIdList!![counter])
    }

    override fun onNext() {
        Picasso.get().load(imagesList!![++counter]).placeholder(R.drawable.profile)
            .into(image_stories_display)
        seenNumber(storiesIdList!![counter])
        addViewToStory(storiesIdList!![counter])
    }

    override fun onPause() {
        super.onPause()
        storyProgressVew!!.pause()
    }

    override fun onResume() {
        super.onResume()
        storyProgressVew!!.resume()
    }

    override fun onDestroy() {
        super.onDestroy()
        storyProgressVew!!.destroy()
    }
}
