package com.sedat.note.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.sedat.note.R
import com.sedat.note.model.Note
import java.text.SimpleDateFormat
import java.util.*

class NoteAdapter: RecyclerView.Adapter<NoteAdapter.Holder>() {

    //iki liste farkını hesaplayıp recycler da gerekli yerleri günceller.
    //Asenkron çalışır.
    private val diffUtil = object : DiffUtil.ItemCallback<Note>(){
        override fun areItemsTheSame(oldItem: Note, newItem: Note): Boolean {
            return oldItem == newItem
        }

        override fun areContentsTheSame(oldItem: Note, newItem: Note): Boolean {
            return oldItem == newItem
        }
    }

    private val recylerListDiffer = AsyncListDiffer(this, diffUtil)

    var Notes: List<Note>
    get() = recylerListDiffer.currentList
    set(value) = recylerListDiffer.submitList(value)

    //Notlara tıklama işleminde yapılacakları fragment ten kontrol etmek için
    private var onNoteClickListener: ((Int) -> Unit) ?= null
    fun setOnNoteClickListener(listener: (Int) -> Unit){
        onNoteClickListener = listener
    }

    //Nota uzun tıklama işleminde yapılacakları fragment ten kontrol etmek için.(popup menü açılacak)
    private var onNoteLongClickListener: ((Int, View) -> Unit) ?= null
    fun setOnNoteLongClickListener(listener: (Int, View) -> Unit){
        onNoteLongClickListener = listener
    }

    //Alt kategori butonuna tıklama için.
    private var onSubBtnClickListener: ((Int) -> Unit) ?= null
    fun setOnSubBtnClikListener(listener: (Int) -> Unit){
        onSubBtnClickListener = listener
    }

    //Nota ait resimleri göstermek için.
    private var onNoteImageBtnClickListener: ((Int) -> Unit) ?= null
    fun setOnNoteImageBtnClickListener(listener: (Int) -> Unit){
        onNoteImageBtnClickListener = listener
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NoteAdapter.Holder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.row_item_layout2, parent, false)
        return Holder(view)
    }

    override fun getItemCount(): Int {
        return Notes.size
    }

    override fun onBindViewHolder(holder: NoteAdapter.Holder, position: Int) {
        val note = Notes[position]
        val noteText = holder.itemView.findViewById<TextView>(R.id.note_text)
        val noteTimeText = holder.itemView.findViewById<TextView>(R.id.note_time_text)
        val hasSubNoteBtn = holder.itemView.findViewById<ImageView>(R.id.hasSubNoteBtn)
        val hasImageForNote = holder.itemView.findViewById<ImageView>(R.id.hasImageFromNote)

        //Alt kategori varsa butonu göster/gizle işlemi.
        if(note.number_of_sub_note > 0)
            hasSubNoteBtn.visibility = View.VISIBLE
        else
            hasSubNoteBtn.visibility = View.GONE

        //Nota ait resim varsa ikonu göster/gizle işlemi.
        if(note.number_of_image > 0)
            hasImageForNote.visibility = View.VISIBLE
        else
            hasImageForNote.visibility = View.GONE

        //Alt kategori butonu işlemi. (Not alt kategori içeriyorsa buton aktif olur)
        hasSubNoteBtn.setOnClickListener {
            onSubBtnClickListener?.let {
                it(note.id)
            }
        }

        //Resim butonu işlemi. (Not resim içeriyorsa buton aktif olur)
        hasImageForNote.setOnClickListener {
            onNoteImageBtnClickListener?.let {
                it(note.id)
            }
        }

        holder.itemView.apply {
            noteText.text = note.note_text
            noteTimeText.text = note.time.toString()
            noteTimeText.text = convertLongToDate(note.time)
            setOnClickListener {
                onNoteClickListener?.let {
                    it(note.id)
                }
            }
            setOnLongClickListener {
                onNoteLongClickListener?.let {
                    it(note.id, this)
                }
                return@setOnLongClickListener true
            }
        }
    }

    class Holder(itemView: View): RecyclerView.ViewHolder(itemView)

    private fun convertLongToDate(time: Long): String{
        val date = Date(time)
        val format = SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.ENGLISH)
        return format.format(date).toString()
    }

}