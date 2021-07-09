package com.sedat.note.fragment

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.core.widget.addTextChangedListener
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.sedat.note.R
import com.sedat.note.databinding.FragmentAddNoteBinding
import com.sedat.note.model.Note
import com.sedat.note.viewmodel.MainViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.*

class AddNoteFragment : Fragment() {

//Dispatchers.Default -> CPU'yu çok yoran, uzun süreli işlemlerde kullanılır.
//Dispatchers.IO -> İnternetten, veritabanından veri çekme işleme gibi işlemlerde kullanılır.
//Dispatchers.Main -> UI işlemlerini, kullanıcının göreceği işlemleri yapmada kullanılır.
//Dispatchers.Unconfined -> Çağrıldığı yere göre kendisi ayarlar

    private var fragmentbinding: FragmentAddNoteBinding ?= null
    private val binding get() = fragmentbinding!!

    private lateinit var viewModel: MainViewModel
    private lateinit var oldNote: Note  //notu güncellemede kullanıldı.
    private var noteId: Int = -1
    private var isSubNote: Boolean = false

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        fragmentbinding = FragmentAddNoteBinding.inflate(inflater, container, false)
        val view = binding.root
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel = ViewModelProvider(requireActivity()).get(MainViewModel::class.java)

        //Nota tıklama ile gelindiğinde tıklanılan notu getir.
        arguments?.let {
            noteId = AddNoteFragmentArgs.fromBundle(it).noteId
            isSubNote = AddNoteFragmentArgs.fromBundle(it).isSubNote
            if(noteId != -1 && !isSubNote){ //fetch selected note

                /*CoroutineScope(Dispatchers.IO).launch {
                    val noteResult = async {
                        viewModel.getSpecialNote(noteId)
                    }
                    /*val demo = async {
                        gelen data
                    }*/

                    //await() ile async içindekileri işleyip verinin gelmesini bekler. veri geldiğinde atama işlemi yapar.
                    //Bu kullanım genelde iki verinin farklı zamanlarda indirilip aynı anda gösterilmesini, işlenmesini sağlar.
                    //println(demo)
                    binding.noteText.setText(noteResult.await().note_text)
                    oldNote = noteResult.await()
                }*/

                val noteResult = viewModel.getSpecialNote(noteId)
                binding.noteText.setText(noteResult.note_text)
                oldNote = noteResult
            }
        }

        binding.backBtn.setOnClickListener {
            findNavController().popBackStack()
        }

        //her silme ya da yazma işleminde sürekli kontol ediliyor bunu için herhangi bir sorun çıkmasın diye Job işlemi kullanıldı.
        var job: Job ?= null
        binding.noteText.addTextChangedListener {
            job?.cancel()
            job = lifecycleScope.launch {
                delay(500)  //Yazma işlemi bittikten 500 ms sonra işlemleri başlatır.
                it?.let {
                    if(it.toString().isNotEmpty())
                        binding.saveBtn.visibility = View.VISIBLE
                    else
                        binding.saveBtn.visibility = View.GONE
                }
            }
        }

        binding.saveBtn.setOnClickListener {

            if (noteId != -1 && !isSubNote){  //Not güncellemesi yapar.
                val newNote = Note(
                        oldNote.id,
                        oldNote.root_id,
                        binding.noteText.text.toString(),
                        System.currentTimeMillis(),
                        oldNote.number_of_sub_note,
                        oldNote.number_of_image
                )
                viewModel.updateNote(newNote)
                Toast.makeText(context, "Not güncellendi", Toast.LENGTH_SHORT).show()
            }else if (noteId != -1 && isSubNote ){  //Nota alt kategori ekler.

                CoroutineScope(Dispatchers.IO).launch {
                    oldNote = viewModel.getSpecialNote(noteId)

                    oldNote.number_of_sub_note += 1
                    val newRootNote = oldNote

                    val subNote = Note(  //alt kategoriyi oluşturacak not.
                            0,
                            oldNote.id,
                            binding.noteText.text.toString(),
                            System.currentTimeMillis(),
                            0,
                            0
                    )
                    viewModel.saveNote(subNote)
                    viewModel.updateNote(newRootNote)

                }

                Toast.makeText(context, "Alt kategori eklendi.", Toast.LENGTH_SHORT).show()
            }
            else if(noteId == -1 && !isSubNote){  //Yeni not ekle.

                val note = Note(
                        0,
                        0,
                        binding.noteText.text.toString(),
                        System.currentTimeMillis(),
                        0,
                        0
                )

                //viewmodel içinde coroutine scope açmasaydık buradaki kullanım uygulanacaktı.
                /*CoroutineScope(Dispatchers.IO).launch {
                    viewModel.saveNoteFromMain(note)
                }*/
                viewModel.saveNote(note)
                Toast.makeText(context, "Not kaydedildi.", Toast.LENGTH_SHORT).show()
            }

            it.visibility = View.GONE
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        fragmentbinding = null
    }
}

