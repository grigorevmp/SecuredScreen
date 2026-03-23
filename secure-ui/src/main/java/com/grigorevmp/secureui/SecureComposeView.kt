package com.grigorevmp.secureui

import androidx.compose.ui.platform.ComposeView

fun ComposeView.applySecureComposePolicy(
    enabled: Boolean = SecurityModeStore.get(context).isContentHidden.value,
    config: SecureScreenConfig = SecureScreenConfig(),
) {
    applySecurePolicy(enabled = enabled, config = config)
}
