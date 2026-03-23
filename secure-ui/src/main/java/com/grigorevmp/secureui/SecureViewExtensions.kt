package com.grigorevmp.secureui

import android.view.View
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

fun View.applySecurePolicy(
    enabled: Boolean = true,
    config: SecureScreenConfig = SecureScreenConfig(),
) {
    SecureUiPolicy.applyToViewTree(
        root = this,
        enabled = enabled,
        blockAccessibilityTree = config.blockAccessibilityTree,
        scrubAccessibilityPayload = config.scrubAccessibilityPayload,
        markAccessibilityDataSensitive = config.markAccessibilityDataSensitive,
        blockAssistAndAutofill = config.blockAssistAndAutofill,
        filterTouchesWhenObscured = config.filterTouchesWhenObscured,
    )
}

fun View.bindSecurityMode(
    owner: LifecycleOwner,
    store: SecurityModeStore = SecurityModeStore.get(context),
    config: SecureScreenConfig = SecureScreenConfig(),
) {
    owner.lifecycleScope.launch {
        owner.repeatOnLifecycle(Lifecycle.State.STARTED) {
            store.isContentHidden.collect { hidden ->
                applySecurePolicy(enabled = hidden, config = config)
            }
        }
    }
}
