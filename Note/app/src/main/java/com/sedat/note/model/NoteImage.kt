package com.sedat.note.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "T_IMAGES")
data class NoteImage(
    @PrimaryKey(autoGenerate = true)
    var id: Int,
    var root_id: Int,
    var url: String
)