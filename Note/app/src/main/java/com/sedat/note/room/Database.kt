package com.sedat.note.room

import androidx.room.Database
import androidx.room.RoomDatabase
import com.sedat.note.model.Note
import com.sedat.note.model.NoteImage

@Database(entities = [Note::class, NoteImage::class], version = 1)
abstract class Database: RoomDatabase() {
    abstract fun noteDao(): NoteDao
}