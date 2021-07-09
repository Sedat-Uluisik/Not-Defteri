package com.sedat.note.fragment

import android.graphics.Matrix
import android.net.Uri
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.ImageView
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.RequestManager
import com.bumptech.glide.load.resource.bitmap.Rotate
import com.sedat.note.R
import com.sedat.note.adapter.ImageAdapter
import com.sedat.note.databinding.FragmentNoteImagesBinding
import com.sedat.note.viewmodel.MainViewModel
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class NoteImagesFragment @Inject constructor(
    val imageAdapter: ImageAdapter,
    val glide: RequestManager
): Fragment() {

    private var fragmentBinding: FragmentNoteImagesBinding ?= null
    private val _binding get() = fragmentBinding!!

    private lateinit var viewModel: MainViewModel

    //private var imageAdapter = ImageAdapter()  //glide'i adapter içinde inject etmek için fragmentFactory sınıfı kullanıldı.
    private var note_id: Int = -1
    private var imageUrl: String = ""

    //Resimi silmek için sağa sürükleme işlemi.
    private val swipeCallBack = object :ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.RIGHT){
        override fun onMove(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder, target: RecyclerView.ViewHolder): Boolean {
            return true
        }

        //Resmin sağa sürüklenmesiyle silinmesi için.
        override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
            val imageLayoutPosition = viewHolder.layoutPosition //silinecek resmin recyler daki pozisyonu.
            val selectedImage = imageAdapter.Images[imageLayoutPosition]
            val root_id = selectedImage.root_id  //Eklenen notun sahip olduğu resim sayısını azaltmak için kullanılacak, notun herhangi bir resme sahip olduğunu belli etmek için bir butonun aktif/pasif olmasında kullanılacak.
            val image_uri = selectedImage.url

            val rootNote = viewModel.getSpecialNote(root_id)
            rootNote.number_of_image -= 1
            viewModel.updateNote(rootNote)

            viewModel.deleteNoteImageFromId(selectedImage.id)  //Resim url si veritabanından silindi.
            viewModel.deleteNoteImageFromFile(image_uri)  //Resim bulunduğu dosyadan silindi.
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            note_id = NoteImagesFragmentArgs.fromBundle(it).noteId
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        fragmentBinding = FragmentNoteImagesBinding.inflate(inflater, container, false)
        viewModel = ViewModelProvider(requireActivity()).get(MainViewModel::class.java)
        val view = _binding.root
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        _binding.recylerImages.adapter = imageAdapter
        ItemTouchHelper(swipeCallBack).attachToRecyclerView(_binding.recylerImages)  //Sağa sürükleme işleminde resmin silinmesi için.
        _binding.recylerImages.layoutManager = GridLayoutManager(context, 3)

        imageAdapter.Images = viewModel.getNoteImages(note_id)

        val zoomImage = _binding.zoomImage

        imageAdapter.setOnImageClickListener {
            if(zoomImage.visibility == View.GONE){
                zoomImage.visibility = View.VISIBLE
                //_binding.zoomImage.setImageURI(Uri.parse(it))
                glide.load(it).into(_binding.zoomImage)
                imageUrl = it
            }
        }

        zoomImage.setOnClickListener {
            if(zoomImage.isVisible)
                zoomImage.visibility = View.GONE
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        imageAdapter.Images = listOf()
        fragmentBinding = null
    }


}