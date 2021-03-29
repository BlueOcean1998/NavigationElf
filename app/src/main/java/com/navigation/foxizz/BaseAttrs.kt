package com.navigation.foxizz

import android.content.Context
import android.content.SharedPreferences
import android.os.Handler
import android.os.Looper
import android.view.inputmethod.InputMethodManager
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.preference.PreferenceManager
import com.navigation.foxizz.BaseApplication.Companion.baseApplication

val mlh = Handler(Looper.getMainLooper())
val dsp: SharedPreferences = PreferenceManager.getDefaultSharedPreferences(baseApplication)
val lbm = LocalBroadcastManager.getInstance(baseApplication)
val imm = baseApplication.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
