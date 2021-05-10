package base.foxizz

import android.content.Context
import android.content.SharedPreferences
import android.os.Handler
import android.os.Looper
import android.view.inputmethod.InputMethodManager
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.preference.PreferenceManager
import base.foxizz.BaseApplication.Companion.baseApplication

/**
 * 基础属性
 */

//主线程/UI线程处理者
val mlh = Handler(Looper.getMainLooper())

//默认SharedPreferences
val dsp: SharedPreferences = PreferenceManager.getDefaultSharedPreferences(baseApplication)

//本地广播接收管理器
val lbm = LocalBroadcastManager.getInstance(baseApplication)

//软键盘管理器
val imm = baseApplication.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
