package com.sedat.note

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentFactory
import com.bumptech.glide.RequestManager
import com.sedat.note.adapter.ImageAdapter
import com.sedat.note.adapter.NoteAdapter
import com.sedat.note.fragment.HomeFragment
import com.sedat.note.fragment.NoteImagesFragment
import com.sedat.note.fragment.SelectImageFragment
import javax.inject.Inject

class FragmentFactory @Inject constructor(
    private val glide: RequestManager,
    private val imageAdapter: ImageAdapter
): FragmentFactory() {
    override fun instantiate(classLoader: ClassLoader, className: String): Fragment {
        return when(className){
            SelectImageFragment::class.java.name -> SelectImageFragment(glide)
            NoteImagesFragment::class.java.name -> NoteImagesFragment(imageAdapter, glide)

            else -> super.instantiate(classLoader, className)
        }
    }

    //glide'i adapter içinde inject etmek için bu sınıf kullanıldı.
}