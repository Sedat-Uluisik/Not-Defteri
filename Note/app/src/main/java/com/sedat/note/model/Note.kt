package com.sedat.note.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "T_NOTES")
data class Note(
    @PrimaryKey(autoGenerate = true)
    var id: Int,
    var root_id: Int,
    var note_text: String,
    var time: Long,
    var number_of_sub_note: Int,
    var number_of_image: Int
)