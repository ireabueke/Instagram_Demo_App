package com.example.instagramdemo.Model

class Comment {
    private var comment: String = ""
    private var publisher: String = ""

    constructor()

    constructor(comment: String, publisher: String) {
        this.comment = comment
        this.publisher = publisher
    }

    fun getComment(): String = comment
    fun getPublisher(): String = publisher

    fun setComment(comment: String) = comment
    fun setPublisher(publisher: String) = publisher

}