package com.grigorevmp.secureui

import android.content.Context
import android.content.ContextWrapper
import androidx.activity.ComponentActivity
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.semantics.clearAndSetSemantics

fun Modifier.secureSemantics(): Modifier = clearAndSetSemantics { }

fun Modifier.secureSemantics(enabled: Boolean): Modifier {
    return if (enabled) {
        secureSemantics()
    } else {
        this
    }
}

@Composable
fun rememberSecurityModeStore(): SecurityModeStore {
    val context = LocalContext.current
    return remember(context.applicationContext) {
        SecurityModeStore.get(context)
    }
}

@Composable
fun rememberSecureContentHidden(): State<Boolean> {
    val store = rememberSecurityModeStore()
    return store.isContentHidden.collectAsState()
}

@Composable
fun ApplySecureComposeHostPolicy(
    config: SecureScreenConfig = SecureScreenConfig(),
) {
    val hidden = rememberSecureContentHidden().value
    val view = LocalView.current

    DisposableEffect(view, hidden, config) {
        view.applySecurePolicy(enabled = hidden, config = config)
        view.context.findActivity()?.let { activity ->
            SecureUiPolicy.applyToWindow(
                window = activity.window,
                enabled = hidden,
                blockScreenshots = config.blockScreenshots,
            )
        }
        onDispose { }
    }
}

private fun Context.findActivity(): ComponentActivity? {
    var currentContext: Context? = this
    while (currentContext is ContextWrapper) {
        if (currentContext is ComponentActivity) {
            return currentContext
        }
        currentContext = currentContext.baseContext
    }
    return null
}
