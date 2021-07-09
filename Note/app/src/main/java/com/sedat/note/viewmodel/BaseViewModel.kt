package com.sedat.note.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlin.coroutines.CoroutineContext

abstract class BaseViewModel(application: Application): AndroidViewModel(application), CoroutineScope {
    //Viewmodel de coroutine scope açmak, kullanmak için.

    private var job = Job() //yapılacak iş.

    override val coroutineContext: CoroutineContext
        get() = job + Dispatchers.Main  //önce arka planda işini yap sonra main thead'a dön.

    override fun onCleared() {
        super.onCleared()
        job.cancel()
    }

}