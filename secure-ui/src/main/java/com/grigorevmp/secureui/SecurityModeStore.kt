package com.grigorevmp.secureui

import android.content.Context
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class SecurityModeStore private constructor(
    context: Context,
) {
    private val appContext = context.applicationContext
    private val prefs = appContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    private val hiddenFlow = MutableStateFlow(prefs.getBoolean(KEY_HIDE_CONTENT, false))

    val isContentHidden: StateFlow<Boolean> = hiddenFlow.asStateFlow()

    fun setContentHidden(hidden: Boolean) {
        if (hiddenFlow.value == hidden) {
            return
        }
        prefs.edit()
            .putBoolean(KEY_HIDE_CONTENT, hidden)
            .apply()
        hiddenFlow.value = hidden
    }

    fun toggle() {
        setContentHidden(!hiddenFlow.value)
    }

    companion object {
        private const val PREFS_NAME = "secure_ui_mode"
        private const val KEY_HIDE_CONTENT = "hide_content"

        @Volatile
        private var instance: SecurityModeStore? = null

        fun get(context: Context): SecurityModeStore {
            return instance ?: synchronized(this) {
                instance ?: SecurityModeStore(context).also { instance = it }
            }
        }
    }
}

fun Context.securityModeStore(): SecurityModeStore = SecurityModeStore.get(this)
