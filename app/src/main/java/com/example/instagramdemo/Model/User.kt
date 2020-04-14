package com.example.instagramdemo.Model

class User {
    private var username: String = ""
    private var fullname: String = ""
    private var bio: String = ""
    private var image: String = ""
    private var uid: String = ""

    constructor()
    constructor(username: String, fullname: String, bio: String, image: String, uid: String) {
        this.username = username
        this.fullname = fullname
        this.bio = bio
        this.image = image
        this.uid = uid
    }

    fun getUsername(): String = username
    fun setUsername(username: String) = this.username
    fun getFullname(): String = fullname
    fun setFullname(fullname: String) = this.fullname
    fun getBio(): String = bio
    fun setBio(bio: String) = this.bio
    fun getImage(): String = image
    fun setImage(image: String) = this.image
    fun getUid(): String = uid
    fun setUid(uid: String) = this.uid

}