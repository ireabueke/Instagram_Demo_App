package com.example.instagramdemo.Model

class Story {

    private var imageurl: String = ""
    private var timestart: Long = 0
    private var timeend: Long = 0
    private var storyid: String = ""
    private var userid: String = ""

    constructor()
    constructor(imageurl: String, timestart: Long, timeend: Long, storyid: String, userid: String) {
        this.imageurl = imageurl
        this.timestart = timestart
        this.timeend = timeend
        this.storyid = storyid
        this.userid = userid
    }

    fun getImageUrl(): String = imageurl
    fun getTimeStart(): Long = timestart
    fun getTimeEnd(): Long = timeend
    fun getStoryID(): String = storyid
    fun getUserId(): String = userid

    fun setImageUrl(imageurl: String) = imageurl
    fun setTimeStart(timestart: Long) = timestart
    fun setTimeEnd(timeend: Long) = timeend
    fun setToryID(storyid: String) = storyid
    fun setUserId(userid: String) = userid

}