package com.sedat.note.fragment

import android.app.AlertDialog
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.PopupMenu
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.core.widget.addTextChangedListener
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.*
import com.sedat.note.R
import com.sedat.note.adapter.ImageAdapter
import com.sedat.note.adapter.NoteAdapter
import com.sedat.note.databinding.FragmentHomeBinding
import com.sedat.note.model.Note
import com.sedat.note.viewmodel.MainViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.*
import javax.inject.Inject

@AndroidEntryPoint
class HomeFragment: Fragment() {

//Dispatchers.Default -> CPU'yu çok yoran, uzun süreli işlemlerde kullanılır.
//Dispatchers.IO -> İnternetten, veritabanından veri çekme işleme gibi işlemlerde kullanılır.
//Dispatchers.Main -> UI işlemlerini, kullanıcının göreceği işlemleri yapmada kullanılır.
//Dispatchers.Unconfined -> Çağrıldığı yere göre kendisi ayarlar

    private var fragmentBinding: FragmentHomeBinding ?= null
    private val binding get() = fragmentBinding!!

    private lateinit var viewModel: MainViewModel
    private val adapter = NoteAdapter()

    //layout(not) sağa sürüklendiğinde silme işlemi için.
    private val swipeCallBack = object :ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.RIGHT){
        override fun onMove(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder, target: RecyclerView.ViewHolder): Boolean {
            return true
        }

        override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
            val layoutPosition = viewHolder.layoutPosition  //silinecek notun pozisyonu
            val selectedNote = adapter.Notes[layoutPosition]  //silinecek not adapter den alındı
            //Eğer viewmodel içinde coroutine scope yerine suspend fun kullansaydık bu kullanım uygulanacaktı.
            /*CoroutineScope(Dispatchers.IO).launch {
                viewModel.deleteNote(selectedNote)
            }*/

            if(selectedNote.root_id != 0){  //silinen her alt kategori için ana notun alt kategori sayısı bir azaltılıyor.(alt kategorileri silmek için)
                val rootNote = viewModel.getSpecialNote(selectedNote.root_id)
                if(rootNote != null){  //ana notu silip sonra kalan alt kategorileri silerken sorun çıkmaması için.
                    rootNote.number_of_sub_note -=1

                    viewModel.updateNote(rootNote)
                }
                viewModel.deleteNote(selectedNote)
                Toast.makeText(context, "silindi", Toast.LENGTH_SHORT).show()
            }else {  //Silinen not ilk eklenen ana not. Burada not ve sahip olduğu tüm alt kategoriler ve resimleri silinir.
                //viewModel.deleteNote(selectedNote)
                DeleteNoteOrSubNotes(selectedNote)
            }
        }
        //ana notun silinmesi durumunda tüm alt notları silinecek.

    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        fragmentBinding = FragmentHomeBinding.inflate(inflater, container, false)
        val view = binding.root
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel = ViewModelProvider(requireActivity()).get(MainViewModel::class.java)
        viewModel.getAllMainNotes()

        binding.fab.setOnClickListener {
            findNavController().navigate(HomeFragmentDirections.actionHomeFragmentToAddNoteFragment())
        }

        binding.recylerNote.adapter = adapter
        //binding.recylerNote.layoutManager = GridLayoutManager(requireContext(), 2)
        ItemTouchHelper(swipeCallBack).attachToRecyclerView(binding.recylerNote) //sağa sürüklenince notun silinmesi için.
        //Recyler item lerinin boyutları farklı olabildiği için standart bir boyutlandırmayı kaldırır, veri içeriğine göre layout u boyutlandırır.
        binding.recylerNote.layoutManager = StaggeredGridLayoutManager(2, LinearLayoutManager.VERTICAL)

        adapter.setOnNoteClickListener {
            findNavController().navigate(HomeFragmentDirections.actionHomeFragmentToAddNoteFragment(it, false)) // it -> tıklanılan notun id'si.
        }
        adapter.setOnNoteLongClickListener { noteId, view_ ->
            popupMenu(noteId, view_)
        }
        //Alt kategorileri getirmek için.
        adapter.setOnSubBtnClikListener {
            viewModel.getSubNotes(it)
            viewModel.note_id.postValue(it)
            binding.backBtnForSubNotes.visibility = View.VISIBLE //kategoriler arası gezinme butonu.
        }

        //Nota ait resimleri getirmek için.
        adapter.setOnNoteImageBtnClickListener {
            findNavController().navigate(HomeFragmentDirections.actionHomeFragmentToNoteImagesFragment(it))
        }

        binding.backBtnForSubNotes.setOnClickListener {
            backFromCategory(it)
        }

        var job: Job ?= null
        binding.serachEdittext.addTextChangedListener {
            job?.cancel()
            job = lifecycleScope.launch {
                delay(900)
                it?.let {
                    if(it.toString().isNotEmpty())
                        viewModel.searchNote(it.toString())
                    else{
                        viewModel.getAllMainNotes()
                    }
                }
            }
        }

        subscribeToObservers()
    }

    fun subscribeToObservers(){
        viewModel.allNotes.observe(viewLifecycleOwner, Observer {
            adapter.Notes = it
        })
        viewModel.searchedNote.observe(viewLifecycleOwner, Observer {
            adapter.Notes = it
        })
    }

    private fun backFromCategory(backBtn: View){
        //Kategoriler arasında gezinme işlemi için.
        val note_id = viewModel.note_id.value
        val root_id = viewModel.getSpecialNote(note_id!!).root_id
        if(root_id == 0){
            backBtn.visibility = View.GONE
            viewModel.getAllMainNotes()
            viewModel.note_id.postValue(0)
        }else{
            viewModel.getSubNotes(root_id)
            viewModel.note_id.postValue(root_id)
        }
    }

    private fun popupMenu(rootNoteId: Int, view: View){
        val popupMenu = PopupMenu(context, view)
        popupMenu.inflate(R.menu.popup_menu)
        popupMenu.setOnMenuItemClickListener { menuItem ->
            when(menuItem.itemId){
                R.id.insertSubNote ->{
                    findNavController().navigate(HomeFragmentDirections.actionHomeFragmentToAddNoteFragment(rootNoteId, true))
                    return@setOnMenuItemClickListener true
                }
                R.id.insertImage ->{
                    findNavController().navigate(HomeFragmentDirections.actionHomeFragmentToSelectImageFragment(rootNoteId))
                    return@setOnMenuItemClickListener true
                }
                else -> return@setOnMenuItemClickListener false
            }
        }
        //popup menüdeki ikonların görünmesi için kullanıldı.
        try {
            val fieldPopup = PopupMenu::class.java.getDeclaredField("mPopup")
            fieldPopup.isAccessible = true

            val mPopup = fieldPopup.get(popupMenu)  //setForceShowIcon -> değişken ismi değil, sabit.
            mPopup.javaClass.getDeclaredMethod("setForceShowIcon", Boolean::class.java).invoke(mPopup, true)

        }catch (e:Exception){
            e.printStackTrace()
        }finally {
            popupMenu.show()
        }
    }

    private fun DeleteNoteOrSubNotes(note: Note){
        //Sadece seçilen not mu silinsin yoksa sahip olduğu alt kategoriler de silisin mi? (resimler de dahil) kontrolü.

        val view = View.inflate(context, R.layout.custom_alert_dialog, null)

        val alertDialog: AlertDialog.Builder = AlertDialog.Builder(context)
        alertDialog.setView(view)

        val alertDis: AlertDialog = alertDialog.create()

        val onlyNoteBtn = view.findViewById<Button>(R.id.onlyNoteBtn)
        val allNoteBtn = view.findViewById<Button>(R.id.allNoteBtn)
        val cancelBtn = view.findViewById<Button>(R.id.cancelBtn)

        alertDis.show()

        onlyNoteBtn.setOnClickListener {
            viewModel.deleteNote(note)
            alertDis.dismiss()
            viewModel.getAllMainNotes()
        }

        allNoteBtn.setOnClickListener {
            viewModel.deleteRootNoteAndSubNotes(note.id)
            alertDis.dismiss()
            viewModel.getAllMainNotes()
        }

        cancelBtn.setOnClickListener {
            alertDis.dismiss()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        fragmentBinding = null
    }
}