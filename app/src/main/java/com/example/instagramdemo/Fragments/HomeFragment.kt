package com.example.instagramdemo.Fragments


import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.instagramdemo.Adapter.PostAdapter
import com.example.instagramdemo.Adapter.StoryAdapter
import com.example.instagramdemo.Model.Post
import com.example.instagramdemo.Model.Story

import com.example.instagramdemo.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

/**
 * A simple [Fragment] subclass.
 */
class HomeFragment : Fragment() {

    private var postAdapter: PostAdapter? = null
    private var storyAdapter: StoryAdapter? = null
    private var storyList: List<Story>? = null
    private var postList: MutableList<Post>? = null
    private var followingList: MutableList<String>? = null
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_home, container, false)

        var recyclerView: RecyclerView? = null
        recyclerView = view.findViewById(R.id.recycler_view_home)
        val linearLayoutManager = LinearLayoutManager(context)
        linearLayoutManager.reverseLayout = true
        linearLayoutManager.stackFromEnd = true
        recyclerView?.layoutManager = linearLayoutManager

        postList = ArrayList()
        postAdapter = context?.let { PostAdapter(it, postList as ArrayList<Post>) }
        recyclerView?.adapter = postAdapter


        var recyclerViewStory: RecyclerView? = null
        recyclerViewStory = view.findViewById(R.id.recycler_view_story)
        //recyclerView.setHasFixedSize(true)
        val linearLayoutManagerStory =
            LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
        recyclerViewStory?.layoutManager = linearLayoutManagerStory

        storyList = ArrayList()
        storyAdapter = context?.let { StoryAdapter(it, storyList as ArrayList<Story>) }
        recyclerViewStory?.adapter = storyAdapter

        checkFollowings()
        return view
    }

    private fun checkFollowings() {
        followingList = ArrayList()
        val followingRef = FirebaseDatabase.getInstance().reference
            .child("Follow").child(FirebaseAuth.getInstance().currentUser!!.uid)
            .child("Following")
        followingRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(p0: DataSnapshot) {
                if (p0.exists()) {
                    (followingList as ArrayList<String>).clear()
                    for (snapshot in p0.children) {
                        snapshot.key?.let { (followingList as ArrayList<String>).add(it) }
                    }
                    retrievePosts()
                    retrieveStory()
                }
            }

            override fun onCancelled(p0: DatabaseError) {}
        })
    }


    private fun retrievePosts() {
        val postRef = FirebaseDatabase.getInstance().reference.child("Posts")
        postRef.addValueEventListener(object : ValueEventListener {

            override fun onDataChange(p0: DataSnapshot) {
                postList?.clear()
                for (snapshot in p0.children) {
                    val post = snapshot.getValue(Post::class.java)
                    for (id in followingList as ArrayList<String>) {

                        if (post!!.getPublisher() == id) {
                            postList?.add(post)
                        }
                        postAdapter!!.notifyDataSetChanged()
                    }
                }
            }

            override fun onCancelled(p0: DatabaseError) {}
        })
    }

    //retrieve stories data from database and pass it to story model class
    private fun retrieveStory() {
        val storyRef = FirebaseDatabase.getInstance().reference.child("Story")
        //.child(FirebaseAuth.getInstance().currentUser!!.uid)
        storyRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(p0: DataSnapshot) {
                val timeCurent = System.currentTimeMillis()
                (storyList as ArrayList<Story>).clear()

                //come back to me
                (storyList as ArrayList<Story>).add(
                    Story(
                        "", 0,
                        0, "", FirebaseAuth.getInstance().currentUser!!.uid
                    )
                )

                for (id in followingList!!) {
                    var countStory = 0
                    var story: Story? = null
                    for (snapshot in p0.child(id).children) {
                        story = snapshot.getValue(Story::class.java)

                        if (timeCurent > story!!.getTimeStart() && timeCurent < story.getTimeEnd()) {
                            countStory++
                        }
                    }
                    if (countStory > 0) {
                        (storyList as ArrayList<Story>).add(story!!)
                        storyAdapter!!.notifyDataSetChanged()
                    }

                }
                //storyAdapter!!.notifyDataSetChanged()
            }

            override fun onCancelled(p0: DatabaseError) {}
        })
    }

}
