package com.grigorevmp.secureui

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

abstract class SecureActivity : AppCompatActivity() {

    protected val securityModeStore: SecurityModeStore by lazy(LazyThreadSafetyMode.NONE) {
        SecurityModeStore.get(this)
    }

    private val secureConfig: SecureScreenConfig? by lazy(LazyThreadSafetyMode.NONE) {
        javaClass.getAnnotation(SecureScreen::class.java)?.toConfig()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val config = secureConfig ?: return
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                securityModeStore.isContentHidden.collect { hidden ->
                    applySecureState(hidden, config)
                    onSecureModeChanged(hidden)
                }
            }
        }
    }

    override fun onPostCreate(savedInstanceState: Bundle?) {
        super.onPostCreate(savedInstanceState)
        applyCurrentSecureState()
    }

    override fun onContentChanged() {
        super.onContentChanged()
        applyCurrentSecureState()
    }

    protected open fun onSecureModeChanged(hidden: Boolean) = Unit

    protected fun isSecureModeEnabled(): Boolean = securityModeStore.isContentHidden.value

    private fun applyCurrentSecureState() {
        val config = secureConfig ?: return
        applySecureState(isSecureModeEnabled(), config)
    }

    private fun applySecureState(hidden: Boolean, config: SecureScreenConfig) {
        SecureUiPolicy.apply(
            window = window,
            root = findViewById<View>(android.R.id.content),
            enabled = hidden,
            config = config,
        )
    }
}
