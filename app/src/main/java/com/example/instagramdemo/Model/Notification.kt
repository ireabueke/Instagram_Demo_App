package com.example.instagramdemo.Model

class Notification {

    private var userid: String = ""
    private var text: String = ""
    private var postid: String = ""
    var ispost = false

    constructor()
    constructor(userid: String, text: String, postid: String, ispost: Boolean) {
        this.userid = userid
        this.text = text
        this.postid = postid
        this.ispost = ispost
    }

    fun getUserId(): String = userid
    fun getText(): String = text
    fun getPostId(): String = postid


    fun setUserId(userid: String) = userid
    fun setTex(text: String) = text
    fun setPostId(postid: String) = postid


}