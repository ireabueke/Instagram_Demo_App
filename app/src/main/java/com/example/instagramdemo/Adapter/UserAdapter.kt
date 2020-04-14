package com.example.instagramdemo.Adapter

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.annotation.NonNull
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.RecyclerView
import com.example.instagramdemo.Fragments.ProfileFragment
import com.example.instagramdemo.MainActivity
import com.example.instagramdemo.Model.User
import com.example.instagramdemo.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.user_item_layout.view.*

class UserAdapter(
    val mContext:
    Context, val mUser: List<User>, val isFragment: Boolean = false
) :
    RecyclerView.Adapter<UserAdapter.ViewHolder>() {

    val firebaseUser: FirebaseUser? = FirebaseAuth.getInstance().currentUser

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = (LayoutInflater.from(mContext)
            .inflate(R.layout.user_item_layout, parent, false))
        return ViewHolder(view)
    }

    override fun getItemCount(): Int {
        return mUser.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val user = mUser[position]
        holder.setUsers(user, position)

    }

    inner class ViewHolder(@NonNull itemView: View) : RecyclerView.ViewHolder(itemView) {

        fun setUsers(user: User, position: Int) {
            itemView.user_name_search.text = user.getUsername()
            itemView.user_full_name_search.text = user.getFullname()
            Picasso.get().load(user.getImage()).placeholder(R.drawable.profile)
                .into(itemView.user_image_search)

            itemView.setOnClickListener {
                if (isFragment) {
                    val pref = mContext.getSharedPreferences("PREFS", Context.MODE_PRIVATE).edit()
                    pref.putString("profileId", user.getUid().toString())
                    pref.apply()
                    (mContext as FragmentActivity).supportFragmentManager.beginTransaction()
                        .replace(R.id.fragment_container, ProfileFragment()).commit()
                } else {
                    val intent = Intent(mContext, MainActivity::class.java)
                    intent.putExtra("publisherId", user.getUid())
                    mContext.startActivity(intent)
                }
            }

            checkFollowingStatus(user.getUid(), itemView.follow_btn_search)
            //setting up follow and un follow button
            itemView.follow_btn_search.setOnClickListener {
                if (itemView.follow_btn_search.text.toString() == "Follow") {
                    //follow user
                    firebaseUser?.uid.let { it1 ->
                        FirebaseDatabase.getInstance().reference
                            .child("Follow").child(it1.toString())
                            .child("Following").child(user.getUid())
                            .setValue(true).addOnCompleteListener {
                                if (it.isSuccessful) {
                                    firebaseUser?.uid.let {
                                        FirebaseDatabase.getInstance().reference
                                            .child("Follow").child(user.getUid())
                                            .child("Followers").child(it.toString())
                                            .setValue(true).addOnCompleteListener {
                                                if (it.isSuccessful) {
                                                    notifyDataSetChanged()
                                                }
                                            }
                                    }
                                }
                            }
                    }
                    addNotification(user.getUid())
                    // un follow user
                } else {
                    firebaseUser?.uid.let {
                        FirebaseDatabase.getInstance().reference
                            .child("Follow").child(it.toString())
                            .child("Following").child(user.getUid())
                            .removeValue().addOnCompleteListener {
                                if (it.isSuccessful) {
                                    firebaseUser?.uid.let {
                                        FirebaseDatabase.getInstance().reference
                                            .child("Follow").child(user.getUid())
                                            .child("Followers").child(it.toString())
                                            .removeValue().addOnCompleteListener {
                                                if (it.isSuccessful) {
                                                    notifyDataSetChanged()
                                                }
                                            }
                                    }
                                }
                            }
                    }
                }
            }
        }
    }

    //change text on rhe following button
    private fun checkFollowingStatus(uid: String, followBtn: Button?) {
        val followingRef = firebaseUser?.uid.let {
            FirebaseDatabase.getInstance().reference
                .child("Follow").child(it.toString())
                .child("Following")
        }
        followingRef.addValueEventListener(object : ValueEventListener {
            override fun onCancelled(p0: DatabaseError) {
            }

            override fun onDataChange(dataSnapshot: DataSnapshot) {
                if (dataSnapshot.child(uid).exists()) {
                    followBtn?.text = "Following"
                } else {
                    followBtn?.text = "Follow"
                }
            }
        })
    }

    private fun addNotification(userId: String) {
        val notitRef = FirebaseDatabase.getInstance().reference.child("Notification").child(userId)
        val notiMap = HashMap<String, Any>()
        notiMap["userid"] = firebaseUser!!.uid
        notiMap["text"] = "started following you"
        notiMap["postid"] = ""
        notiMap["ispost"] = false

        notitRef.push().setValue(notiMap)
    }

}