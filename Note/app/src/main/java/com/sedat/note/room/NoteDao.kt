package com.sedat.note.room

import androidx.room.*
import com.sedat.note.model.Note
import com.sedat.note.model.NoteImage

@Dao
interface NoteDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNote(note: Note)

    @Delete
    suspend fun deleteNote(note: Note)

    @Query("DELETE FROM T_NOTES WHERE id = :note_id")
    suspend fun deleteNoteFromId(note_id: Int)

    @Query("SELECT * FROM T_NOTES WHERE root_id = 0")
    suspend fun observeMainNotes(): List<Note>  //ilk eklenen notarı getirir.(alt kategorilere sahipse 1. not)

    @Query("SELECT * FROM T_NOTES WHERE id = :noteId")
    suspend fun getSpecialNote(noteId: Int): Note  //Seçilen notu görmek için.

    @Query("SELECT * FROM T_NOTES WHERE root_id = :root_id")
    suspend fun getNotesFromRootId(root_id: Int): List<Note>  //Silinen notun alt kategorilerini de silmek için kullanılıcak.

    @Query("SELECT * FROM T_NOTES WHERE note_text LIKE '%' || :string_ || '%' ")
    suspend fun searchNote(string_: String): List<Note>

    @Query("SELECT * FROM T_NOTES WHERE root_id = :rootId")
    suspend fun getSubNotes(rootId: Int): List<Note>

    @Update
    suspend fun updateNote(vararg newNote: Note)

    //Resim ile ilgili işlemler.
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNoteImage(noteImage: NoteImage)

    @Query("DELETE FROM T_IMAGES WHERE id = :image_id")
    suspend fun deleteNoteImageFromId(image_id: Int)

    @Query("DELETE FROM T_IMAGES WHERE root_id = :root_id")
    suspend fun deleteNoteImageFromRootId(root_id: Int)

    @Query("SELECT * FROM T_IMAGES WHERE root_id = :root_id")
    suspend fun getNoteImages(root_id: Int): List<NoteImage>
}