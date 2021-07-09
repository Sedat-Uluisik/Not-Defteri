package com.sedat.note.viewmodel

import android.app.Application
import android.content.Context
import android.net.Uri
import android.widget.Toast
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sedat.note.model.Note
import com.sedat.note.model.NoteImage
import com.sedat.note.room.Repository
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import java.io.File
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
        private val repository: Repository,
        @ApplicationContext private val application: Context
): BaseViewModel(application as Application) {

    //Kategoriler arasında gezinmek için kullanılacak id.
    val note_id = MutableLiveData<Int>()

    fun saveNote(note: Note){
        launch {
            repository.saveData(note)
        }
    }

    //listedeki objeleri kaydetmek için -> dao.saveAll(*liste.toTypedArray()) kullabilir.(burada gerek yok)

    fun deleteNote(note: Note){
        launch {
            repository.deleteNote(note)  //notu db den siler.
            val imageList = repository.getNoteImages(note.id)
            for (i in imageList){
                deleteNoteImageFromFile(i.url)  //nota ait resimleri kaydedildiği klasörden siler.
            }
            repository.deleteNoteImageFromRootId(note.id)  //nota ait resimlerin url lerini db den siler.
        }
    }

    //Ana not silindiğinde alt kategori ve resimlerin silinmesi için.
    fun deleteRootNoteAndSubNotes(noteId: Int){
        launch {
            repository.deleteNoteFromId(noteId)//notu db den siler.
            val imageList = repository.getNoteImages(noteId)
            for (i in imageList){
                deleteNoteImageFromFile(i.url)//nota ait resimleri kaydedildiği klasörden siler.
            }
            repository.deleteNoteImageFromRootId(noteId)//nota ait resimlerin url lerini db den siler.
        }
        deleteRootNoteAndSubNotes2(noteId)
    }

    private fun deleteRootNoteAndSubNotes2(root_id: Int){  //Algoritma gereği ana ve alt kategorileri silmek için iki fun kullandım.
        launch {
            val noteList: List<Note> = repository.getNotesFromRootId(root_id)

            for (i in noteList){
                repository.deleteNote(i)
                val imageList = repository.getNoteImages(i.id)
                for (j in imageList){
                    deleteNoteImageFromFile(j.url)//nota ait resimlerin url lerini db den siler.
                }
                repository.deleteNoteImageFromRootId(i.id)

                deleteRootNoteAndSubNotes2(i.id)
            }
        }
    }

    private val notesList = MutableLiveData<List<Note>>()
    val allNotes: LiveData<List<Note>>
        get() = notesList
    fun getAllMainNotes(){
        launch {
            notesList.value = repository.observeData()
        }
    }

    /*suspend fun getSpecialNote(noteId: Int): Note{
        return repository.getSpecialNote(noteId)
    }*/

    fun getSpecialNote(noteId: Int): Note {
        var note: Note
        runBlocking {
            note = repository.getSpecialNote(noteId)
        }
        return note
    }

    val searchedNote: LiveData<List<Note>>
        get() = notesList
    fun searchNote(string_: String){
        if (string_.isEmpty())
            return

        launch {
            notesList.value = repository.searchNote(string_)
        }
    }

    fun getSubNotes(rootId: Int){
        launch {
            notesList.value = repository.getSubNotes(rootId)
        }
    }

    fun updateNote(newNote: Note){
        launch {
            repository.updateNote(newNote)
        }
    }

    //Resim ile ilgili işlemler.
    fun insertNoteImage(noteImage: NoteImage){
        launch {
            repository.insertNoteImage(noteImage) //db ye resim url sini kaydeder.
        }
    }

    fun deleteNoteImageFromId(imageId: Int){
        launch {
            repository.deleteNoteImageFromId(imageId)  //db den resim url sini siler.
        }
    }

    //Resimlerin kaydedildiği klasörden resmi silmek için.
    fun deleteNoteImageFromFile(uri: String){
        val file = File(Uri.parse(uri).path.toString())
        if(file.exists())
            if(file.delete())
                Toast.makeText(application, "Resim silindi", Toast.LENGTH_SHORT).show()
    }

    fun getNoteImages(root_id: Int): List<NoteImage>{
        val images: List<NoteImage>
        runBlocking {
            images = repository.getNoteImages(root_id)
        }

        return images
    }
}