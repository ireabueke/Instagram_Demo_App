package com.example.instagramdemo.Fragments


import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.instagramdemo.Adapter.UserAdapter
import com.example.instagramdemo.Model.User

import com.example.instagramdemo.R
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.android.synthetic.main.fragment_search.*
import kotlinx.android.synthetic.main.fragment_search.view.*

class SearchFragment : Fragment() {

    private var recyclerView: RecyclerView? = null
    private var userAdapter: UserAdapter? = null
    private var mUser: MutableList<User>? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_search, container, false)
        recyclerView = view.findViewById(R.id.recycler_view_search)
        recyclerView?.setHasFixedSize(true)
        recyclerView?.layoutManager = LinearLayoutManager(context)

        mUser = ArrayList()
        userAdapter = context?.let { UserAdapter(it, mUser as ArrayList<User>, true) }
        recyclerView?.adapter = userAdapter

        view.search_edit_text.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if (view.search_edit_text.text.toString().isNotEmpty()) {
                    recyclerView?.visibility = View.VISIBLE
                    searchUser(s.toString().toLowerCase())
                }
            }

            override fun afterTextChanged(s: Editable?) {}
        })

        return view
    }

    private fun searchUser(input: String) {
        val query = FirebaseDatabase.getInstance().reference
            .child("Users")
            .orderByChild("fullname")
            .startAt(input)
            .endAt(input + "\uf8ff")

        query.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                mUser?.clear()
                for (snapshot in dataSnapshot.children) {
//                    val user = dataSnapshot.getValue(User::class.java)
//                    val fullName =snapshot.child("fullname").value.toString()
//                    val userName =snapshot.child("username").value.toString()
//                    val bio =snapshot.child("bio").value.toString()
//                    val image =snapshot.child("image").value.toString()
//                    val uid =snapshot.child("uid").value.toString()
//
//                    User(userName,fullName,bio,image,uid)
//                    if (user != null){
//                        mUser?.add(User(userName,fullName,bio,image,uid))
//                    }

                    val user = snapshot.getValue(User::class.java)
                    if (user != null) {
                        mUser?.add(user)
                    }
                }
                userAdapter?.notifyDataSetChanged()
            }

            override fun onCancelled(p0: DatabaseError) {}
        })
    }

//    private fun retrieveUsers() {
//        val userRef = FirebaseDatabase.getInstance().reference.child("Users")
//        userRef.addValueEventListener(object : ValueEventListener{
//            override fun onDataChange(dataSnapshot: DataSnapshot) {
//                if(view?.search_edit_text?.text.toString() == ""){
//                    mUser?.clear()
//                    for (snapshot in dataSnapshot.children){
//                        val user = snapshot.getValue(User::class.java)
//                        if (user != null){
//                            mUser?.add(user)
//                        }
//                    }
//                    userAdapter?.notifyDataSetChanged()
//                }
//            }
//
//            override fun onCancelled(p0: DatabaseError) {}
//        })
//    }


}
