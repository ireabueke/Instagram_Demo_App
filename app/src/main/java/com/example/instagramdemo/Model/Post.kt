package com.example.instagramdemo.Model

class Post {
    private var postid = ""
    private var postimage = ""
    private var publisher = ""
    private var description = ""

    constructor()
    constructor(postid: String, postimage: String, publisher: String, description: String) {
        this.postid = postid
        this.postimage = postimage
        this.publisher = publisher
        this.description = description
    }

    fun getPostId(): String = postid
    fun getPostImage(): String = postimage
    fun getPublisher(): String = publisher
    fun getDescription(): String = description

    fun setPostId(postid: String) = postid
    fun setPostImage(postimage: String) = postimage
    fun setPublisher(publisher: String) = publisher
    fun setDescription(description: String) = description


}
