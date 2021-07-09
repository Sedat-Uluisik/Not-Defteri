package com.sedat.note.room

import androidx.lifecycle.LiveData
import com.sedat.note.model.Note
import com.sedat.note.model.NoteImage
import javax.inject.Inject

class Repository @Inject constructor(
        private val dao: NoteDao
) {

    suspend fun saveData(note: Note) = dao.insertNote(note)

    suspend fun deleteNote(note: Note) = dao.deleteNote(note)

    suspend fun deleteNoteFromId(noteId: Int) = dao.deleteNoteFromId(noteId)

    suspend fun observeData(): List<Note>{
        return dao.observeMainNotes()
    }

    suspend fun getSpecialNote(noteId: Int): Note{
        return dao.getSpecialNote(noteId)
    }

    suspend fun getNotesFromRootId(rootId: Int): List<Note>{  //Silinen notun alt kategorilerini de silmek için kullanılıcak.
        return dao.getNotesFromRootId(rootId)
    }

    suspend fun searchNote(string_: String): List<Note>{
        return dao.searchNote(string_)
    }

    suspend fun getSubNotes(rootId: Int): List<Note>{
        return dao.getSubNotes(rootId)
    }

    suspend fun updateNote(newNote: Note) = dao.updateNote(newNote)

    //Veritabanındaki resim ile ilgili işlemler. (vt ye resim url leri üzerinden işlem yapıldı.)

    suspend fun insertNoteImage(noteImage: NoteImage) = dao.insertNoteImage(noteImage)

    suspend fun deleteNoteImageFromId(imageId: Int) = dao.deleteNoteImageFromId(imageId)

    suspend fun deleteNoteImageFromRootId(rootId: Int) = dao.deleteNoteImageFromRootId(rootId)

    suspend fun getNoteImages(root_id: Int): List<NoteImage>{
        return dao.getNoteImages(root_id)
    }
}