package com.sedat.note

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class BaseApplication:Application()

/*
Hilt'in kullanılacağı önceden bildirildi.
Manifeste ekleme yapılması gerekir.
 */