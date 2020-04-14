package com.example.instagramdemo.Fragments


import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.instagramdemo.AccountSettingsActivity
import com.example.instagramdemo.Adapter.myImagesAdapter
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
import kotlinx.android.synthetic.main.fragment_profile.*
import kotlinx.android.synthetic.main.fragment_profile.view.*
import kotlinx.android.synthetic.main.fragment_profile.view.recycler_view_uploaded_pictures
import java.util.*
import kotlin.collections.ArrayList

/**
 * A simple [Fragment] subclass.
 */
class ProfileFragment : Fragment() {

    private lateinit var profileID: String
    private lateinit var firebaseUser: FirebaseUser
    var postList: List<Post>? = null
    var postSavedList: List<Post>? = null
    var savedImage: List<String>? = null
    var ImagesAdapter: myImagesAdapter? = null
    var ImagesAdapter2: myImagesAdapter? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_profile, container, false)
        firebaseUser = FirebaseAuth.getInstance().currentUser!!
        val pref = context?.getSharedPreferences("PREFS", Context.MODE_PRIVATE)
        if (pref != null) {
            this.profileID = pref.getString("profileId", "none")!!
        }
        if (profileID == firebaseUser.uid) {
            view.edit_account_btn.text = "Edit profile"
        } else if (profileID != firebaseUser.uid) {
            checkFollowAndFollowing()
        }

        //recycler view uploaded images
        val recyclerView: RecyclerView = view.findViewById(R.id.recycler_view_uploaded_pictures)
        val linearLayoutManager: LinearLayoutManager = GridLayoutManager(context, 3)
        recyclerView.setHasFixedSize(true)
        recyclerView.layoutManager = linearLayoutManager
        postList = ArrayList()
        ImagesAdapter = context?.let { myImagesAdapter(it, postList as ArrayList<Post>) }
        recyclerView.adapter = ImagesAdapter

        //recycler view saved images
        val recyclerViewSavedImages: RecyclerView = view.findViewById(R.id.recycler_saved_pictures)
        val linearLayoutManagerSavedImages: LinearLayoutManager = GridLayoutManager(context, 3)
        recyclerViewSavedImages.setHasFixedSize(true)
        recyclerViewSavedImages.layoutManager = linearLayoutManagerSavedImages
        postSavedList = ArrayList()
        ImagesAdapter2 = context?.let { myImagesAdapter(it, postSavedList as ArrayList<Post>) }
        recyclerViewSavedImages.adapter = ImagesAdapter2


        view.edit_account_btn.setOnClickListener {
            val getButtonText = it.edit_account_btn.text.toString()
            when {
                getButtonText == "Edit profile" -> startActivity(
                    Intent(context, AccountSettingsActivity::class.java)
                )

                getButtonText == "Follow" -> {
                    firebaseUser.uid.let {
                        FirebaseDatabase.getInstance().reference
                            .child("Follow").child(it)
                            .child("Following").child(profileID)
                            .setValue(true)
                    }
                    firebaseUser.uid.let {
                        FirebaseDatabase.getInstance().reference
                            .child("Follow").child(profileID)
                            .child("Followers").child(it)
                            .setValue(true)
                    }
                    addNotification()

                }
                getButtonText == "Following" -> {
                    firebaseUser.uid.let {
                        FirebaseDatabase.getInstance().reference
                            .child("Follow").child(it)
                            .child("Following").child(profileID)
                            .removeValue()
                    }
                    firebaseUser.uid.let {
                        FirebaseDatabase.getInstance().reference
                            .child("Follow").child(profileID)
                            .child("Followers").child(it)
                            .removeValue()
                    }
                }

            }
        }

        view.followers.setOnClickListener {
            val intent = Intent(context, ShowUsersActivity::class.java)
            intent.putExtra("id", profileID)
            intent.putExtra("title", "followers")
            startActivity(intent)
        }

        view.followings.setOnClickListener {
            val intent = Intent(context, ShowUsersActivity::class.java)
            intent.putExtra("id", profileID)
            intent.putExtra("title", "following")
            startActivity(intent)
        }

        recyclerView.visibility = View.VISIBLE
        recyclerViewSavedImages.visibility = View.GONE

        view.uploaded_images_btn.setOnClickListener {
            recyclerView.visibility = View.VISIBLE
            recyclerViewSavedImages.visibility = View.GONE
        }

        view.images_save_btn.setOnClickListener {
            recyclerView.visibility = View.GONE
            recyclerViewSavedImages.visibility = View.VISIBLE
        }

        getFollowers()
        getFollowings()
        userProfile()
        myPhotos()
        getTotalNumberOfPost()
        mySaveImages()
        return view
    }

    private fun checkFollowAndFollowing() {
        val followingRef = firebaseUser.uid.let {
            FirebaseDatabase.getInstance().reference
                .child("Follow").child(it)
                .child("Following")
        }
        if (followingRef != null) {
            followingRef.addValueEventListener(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    if (dataSnapshot.child(profileID).exists()) {
                        view?.edit_account_btn?.text = "Following"
                    } else {
                        view?.edit_account_btn?.text = "Follow"
                    }
                }

                override fun onCancelled(p0: DatabaseError) {}
            })
        }
    }

    private fun getFollowers() {
        val followingRef = FirebaseDatabase.getInstance().reference
            .child("Follow").child(profileID)
            .child("Following")
        followingRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(p0: DataSnapshot) {
                if (p0.exists()) {
                    view?.following?.text = p0.childrenCount.toString()
                }
            }

            override fun onCancelled(p0: DatabaseError) {

            }
        })
    }

    private fun getFollowings() {
        val followingRef = FirebaseDatabase.getInstance().reference
            .child("Follow").child(profileID)
            .child("Followers")

        followingRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(p0: DataSnapshot) {
                if (p0.exists()) {
                    view?.follower?.text = p0.childrenCount.toString()
                }
            }

            override fun onCancelled(p0: DatabaseError) {

            }
        })
    }

    private fun myPhotos() {
        var postRef = FirebaseDatabase.getInstance().reference.child("Posts")
        postRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(p0: DataSnapshot) {
                if (p0.exists()) {
                    (postList as ArrayList<Post>).clear()
                    for (snapshot in p0.children) {
                        val post = snapshot.getValue(Post::class.java)!!
                        if (post.getPublisher().equals(profileID)) {
                            (postList as ArrayList<Post>).add(post)
                        }
                        Collections.reverse(postList)
                        ImagesAdapter!!.notifyDataSetChanged()
                    }
                }
            }

            override fun onCancelled(p0: DatabaseError) {}
        })
    }

    private fun userProfile() {
        val userRef = FirebaseDatabase.getInstance().reference.child("Users").child(profileID)
        userRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(p0: DataSnapshot) {

                //if (context != null){return}

                if (p0.exists()) {
                    val user = p0.getValue<User>(User::class.java)
                    Picasso.get().load(user!!.getImage()).placeholder(R.drawable.profile_icon)
                        .into(view?.profile_image)
                    view?.profile_fragment_userName?.text = user.getUsername()
                    view?.full_profile_frag?.text = user.getFullname()
                    view?.bio_profile_frag?.text = user.getBio()
                }
            }

            override fun onCancelled(p0: DatabaseError) {}
        })
    }

    private fun getTotalNumberOfPost() {
        val postRef = FirebaseDatabase.getInstance().reference.child("Posts")//.child(profileID)

        postRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(p0: DataSnapshot) {
                if (p0.exists()) {
                    var counter = 0

                    for (snapshot in p0.children) {
                        val post = snapshot.getValue(Post::class.java)
                        if (post?.getPublisher().equals(profileID)) {
                            counter++
                        }
                    }
                    total_posts.text = " $counter"
                }
            }

            override fun onCancelled(p0: DatabaseError) {}
        })
    }

    override fun onStop() {
        super.onStop()

        val pref = context?.getSharedPreferences("PREFS", Context.MODE_PRIVATE)?.edit()
        pref?.putString("profileId", firebaseUser.uid)
        pref?.apply()
    }

    override fun onPause() {
        super.onPause()
        val pref = context?.getSharedPreferences("PREFS", Context.MODE_PRIVATE)?.edit()
        pref?.putString("profileId", firebaseUser.uid)
        pref?.apply()
    }

    override fun onDestroy() {
        super.onDestroy()

        val pref = context?.getSharedPreferences("PREFS", Context.MODE_PRIVATE)?.edit()
        pref?.putString("profileId", firebaseUser.uid)
        pref?.apply()
    }

    //function to handle images saved by the user
    private fun mySaveImages() {
        savedImage = ArrayList()
        FirebaseDatabase.getInstance().reference.child("Save").child(firebaseUser.uid)
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(p0: DataSnapshot) {
                    if (p0.exists())
                        for (snapshot in p0.children) {
                            (savedImage as ArrayList<String>).add(snapshot.key!!)
                            readSaveImagesData()
                        }
                }

                override fun onCancelled(p0: DatabaseError) {

                }
            })
    }

    private fun readSaveImagesData() {
        val postRef = FirebaseDatabase.getInstance().reference.child("Posts")
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    if (dataSnapshot.exists()) {
                        (postSavedList as ArrayList<Post>).clear()
                        for (snapshot in dataSnapshot.children) {
                            val post = snapshot.getValue(Post::class.java)
                            for (key in savedImage!!) {
                                if (post!!.getPostId().equals(key)) {
                                    (postSavedList as ArrayList<Post>).add(post)
                                }
                            }
                        }
                        ImagesAdapter2!!.notifyDataSetChanged()
                    }
                }

                override fun onCancelled(p0: DatabaseError) {}
            })
    }

    private fun addNotification() {
        val notitRef = FirebaseDatabase.getInstance().reference.child("Notification").child(
            firebaseUser.uid
        )
        val notiMap = HashMap<String, Any>()
        notiMap["userid"] = firebaseUser.uid
        notiMap["text"] = "started following you"
        notiMap["postid"] = ""
        notiMap["ispost"] = false

        notitRef.push().setValue(notiMap)
    }

}
