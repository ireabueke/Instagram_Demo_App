package com.example.instagramdemo.Fragments


import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.instagramdemo.Adapter.NotificationAdapter
import com.example.instagramdemo.Model.Notification
import com.example.instagramdemo.Model.Post
import com.example.instagramdemo.Model.User

import com.example.instagramdemo.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.activity_comment.*
import kotlinx.android.synthetic.main.fragment_profile.view.*
import java.util.*
import kotlin.collections.ArrayList

/**
 * A simple [Fragment] subclass.
 */
class NotificationsFragment : Fragment() {

    private var notificationList: List<Notification>? = null
    private var notificationAdapter: NotificationAdapter? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_notifications, container, false)

        var recyclerView: RecyclerView
        recyclerView = view.findViewById(R.id.recycler_view_notification)
        recyclerView?.setHasFixedSize(true)
        recyclerView?.layoutManager = LinearLayoutManager(context)
        notificationList = ArrayList()
        notificationAdapter =
            NotificationAdapter(context!!, notificationList as ArrayList<Notification>)
        recyclerView.adapter = notificationAdapter

        readNotification()

        return view
    }

    private fun readNotification() {
        val notitRef = FirebaseDatabase.getInstance().reference.child("Notification")
            .child(FirebaseAuth.getInstance().currentUser!!.uid)
        notitRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                if (dataSnapshot.exists()) {
                    (notificationList as ArrayList<Notification>).clear()
                    for (snapshot in dataSnapshot.children) {
                        val notification = snapshot.getValue(Notification::class.java)
                        (notificationList as ArrayList<Notification>).add(notification!!)
                    }
                    Collections.reverse(notificationList)
                    notificationAdapter!!.notifyDataSetChanged()
                }
            }

            override fun onCancelled(p0: DatabaseError) {}
        })
    }


}
