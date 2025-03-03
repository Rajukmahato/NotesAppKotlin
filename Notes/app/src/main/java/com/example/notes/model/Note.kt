package com.example.notes.model

data class Note(
    var id: String = "",
    var title: String = "",
    var content: String = "",
    var timestamp: Long = System.currentTimeMillis(),
    var userId: String = ""
) 