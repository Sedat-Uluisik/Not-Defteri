package com.sedat.note.adapter

import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.RequestManager
import com.sedat.note.R
import com.sedat.note.model.NoteImage
import javax.inject.Inject

class ImageAdapter @Inject constructor(val glide: RequestManager): RecyclerView.Adapter<ImageAdapter.Holder>() {

    //iki liste farkını hesaplayıp recycler da gerekli yerleri günceller.
    //Asenkron çalışır.
    private val diffUtil = object :DiffUtil.ItemCallback<NoteImage>(){
        override fun areItemsTheSame(oldItem: NoteImage, newItem: NoteImage): Boolean {
            return oldItem == newItem
        }

        override fun areContentsTheSame(oldItem: NoteImage, newItem: NoteImage): Boolean {
            return oldItem == newItem
        }

    }

    private val recylerListDiffer = AsyncListDiffer(this, diffUtil)

    var Images: List<NoteImage>
        get() = recylerListDiffer.currentList
        set(value) = recylerListDiffer.submitList(value)

    //Resime tıklandığına tam ekran olması için.
    private var onImageClickListener: ((String) -> Unit) ?= null
    fun setOnImageClickListener(listener: (String) -> Unit){
        onImageClickListener = listener
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ImageAdapter.Holder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.row_item_images, parent, false)
        return Holder(view)
    }

    override fun getItemCount(): Int {
        return Images.size
    }

    override fun onBindViewHolder(holder: ImageAdapter.Holder, position: Int) {
        val noteImage = Images[position]

        val imageView = holder.itemView.findViewById<ImageView>(R.id.note_image)
        glide.load(noteImage.url).into(imageView)

        imageView.setOnClickListener {
            onImageClickListener?.let {
                it(noteImage.url)
            }
        }
    }

    class Holder(itemView: View): RecyclerView.ViewHolder(itemView)

}