package com.sedat.note.fragment

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import com.bumptech.glide.Glide
import com.bumptech.glide.RequestManager
import com.google.android.material.snackbar.Snackbar
import com.sedat.note.R
import com.sedat.note.databinding.FragmentSelectImageBinding
import com.sedat.note.model.NoteImage
import com.sedat.note.viewmodel.MainViewModel
import dagger.hilt.android.AndroidEntryPoint
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.OutputStream
import java.lang.Exception
import javax.inject.Inject

@AndroidEntryPoint
class SelectImageFragment @Inject constructor(
    val glide: RequestManager
) : Fragment() {

    private var selectedPicture: Uri ?= null
    private var selectedBitmap: Bitmap ?= null

    private lateinit var activityResultLauncher: ActivityResultLauncher<Intent>
    private lateinit var permissionLauncher: ActivityResultLauncher<String>

    private lateinit var viewModel: MainViewModel

    private var fragmentBinding: FragmentSelectImageBinding ?= null
    private val _binding get() = fragmentBinding!!

    private var root_id: Int = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        registerLauncher()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        fragmentBinding = FragmentSelectImageBinding.inflate(inflater, container, false)
        val view = _binding.root

        viewModel = ViewModelProvider(requireActivity()).get(MainViewModel::class.java)

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        arguments?.let {
            //Resim eklenecek notun id si. Veritabanına resmin root(kök)id sine yazılacak
            root_id = SelectImageFragmentArgs.fromBundle(it).rootId
        }

        _binding.noteImageView.setOnClickListener {
            selectImage(view)
        }

        _binding.doneBtnFromImage.setOnClickListener {
            if(selectedBitmap != null && root_id != -1){

                val alphabet: List<Char> = ('a'..'z') + ('A'..'Z') + ('0'..'9')
                val randomUid: String = List(5) { alphabet.random() }.joinToString("")

                val imageUrl = saveImageToFile(selectedBitmap!!, randomUid)

                val noteImage = NoteImage(
                    0,
                    root_id,
                    imageUrl.toString()
                )

                viewModel.insertNoteImage(noteImage)

                //nota resim eklendiğinde sahip olduğu resim sayısı güncellendi.
                val rootNote = viewModel.getSpecialNote(root_id)
                rootNote.number_of_image += 1
                viewModel.updateNote(rootNote)

                Toast.makeText(context, "Image Saved", Toast.LENGTH_SHORT).show()
            }else
                Toast.makeText(context, "Please try again.", Toast.LENGTH_SHORT).show()
        }
    }

    private fun selectImage(view: View){
        activity?.let {
            if(ContextCompat.checkSelfPermission(requireActivity().applicationContext, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED){
                if (ActivityCompat.shouldShowRequestPermissionRationale(requireActivity(), Manifest.permission.READ_EXTERNAL_STORAGE)){
                    Snackbar.make(view, "Permission needed for gallery", Snackbar.LENGTH_INDEFINITE).setAction("Give Permission",
                        View.OnClickListener {
                            permissionLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE)
                        }).show()
                }else{
                    permissionLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE)
                }
            }else{
                val intentToGallery = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                activityResultLauncher.launch(intentToGallery)
            }
        }
    }

    private fun registerLauncher(){
        activityResultLauncher = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ){result ->
            if(result.resultCode == AppCompatActivity.RESULT_OK){
                val intentFromResult = result.data
                if(intentFromResult != null){

                    selectedPicture = intentFromResult.data
                    _binding.doneBtnFromImage.visibility = View.VISIBLE
                    try {
                        if(Build.VERSION.SDK_INT >= 28){
                            glide.load(selectedPicture).into(_binding.noteImageView)
                        }else{
                            selectedBitmap = MediaStore.Images.Media.getBitmap(requireActivity().contentResolver, selectedPicture)
                            //_binding.noteImageView.setImageBitmap(selectedBitmap)
                            glide.load(selectedBitmap).into(_binding.noteImageView)
                        }
                    }catch (e:IOException){
                        e.printStackTrace()
                    }
                }
            }
        }
        permissionLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()){result ->
            if(result){
                //permission granted
                val intentToGallery = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                activityResultLauncher.launch(intentToGallery)
            }else{
                //permission denied
                Toast.makeText(requireContext(), "Permission nedded!", Toast.LENGTH_LONG).show()
            }
        }
    }

    //Seçilen resim uygulama dosyaları içine kaydedildi.
    private fun saveImageToFile(bitmap: Bitmap, uid: String): Uri{
        val dir = File(requireContext().getExternalFilesDir("/"), "Pictures")
        if(!dir.exists())
            dir.mkdir()

        val file = File(dir, "${uid}.jpg")

        try {
            val stream: OutputStream = FileOutputStream(file)
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream)
            stream.flush()
            stream.close()
        }catch (e:Exception){
            e.printStackTrace()
        }

        return Uri.parse(file.absolutePath)
    }
}