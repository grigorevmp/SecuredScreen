package com.grigorevmp.secureui

import android.os.Build
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.view.WindowManager
import androidx.annotation.RequiresApi

object SecureUiPolicy {

    fun apply(
        window: Window,
        root: View?,
        enabled: Boolean,
        config: SecureScreenConfig,
    ) {
        applyToWindow(
            window = window,
            enabled = enabled,
            blockScreenshots = config.blockScreenshots,
        )
        root ?: return
        applyToViewTree(
            root = root,
            enabled = enabled,
            blockAccessibilityTree = config.blockAccessibilityTree,
            scrubAccessibilityPayload = config.scrubAccessibilityPayload,
            markAccessibilityDataSensitive = config.markAccessibilityDataSensitive,
            blockAssistAndAutofill = config.blockAssistAndAutofill,
            filterTouchesWhenObscured = config.filterTouchesWhenObscured,
        )
    }

    fun applyToWindow(
        window: Window,
        enabled: Boolean,
        blockScreenshots: Boolean,
    ) {
        if (!blockScreenshots) {
            return
        }
        if (enabled) {
            window.addFlags(WindowManager.LayoutParams.FLAG_SECURE)
        } else {
            window.clearFlags(WindowManager.LayoutParams.FLAG_SECURE)
        }
    }

    fun applyToViewTree(
        root: View,
        enabled: Boolean,
        blockAccessibilityTree: Boolean = true,
        scrubAccessibilityPayload: Boolean = true,
        markAccessibilityDataSensitive: Boolean = true,
        blockAssistAndAutofill: Boolean = true,
        filterTouchesWhenObscured: Boolean = true,
    ) {
        if (blockAccessibilityTree) {
            root.importantForAccessibility = if (enabled) {
                View.IMPORTANT_FOR_ACCESSIBILITY_NO_HIDE_DESCENDANTS
            } else {
                View.IMPORTANT_FOR_ACCESSIBILITY_AUTO
            }
        }

        if (scrubAccessibilityPayload) {
            SecureAccessibilityScrubber.applyRecursively(root, enabled)
        }

        if (markAccessibilityDataSensitive && Build.VERSION.SDK_INT >= 34) {
            Api34Impl.setSensitive(root, enabled)
        }

        updateViewTree(root, enabled, blockAssistAndAutofill, filterTouchesWhenObscured)
    }

    private fun updateViewTree(
        view: View,
        enabled: Boolean,
        blockAssistAndAutofill: Boolean,
        filterTouchesWhenObscured: Boolean,
    ) {
        if (blockAssistAndAutofill) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                view.importantForAutofill = if (enabled) {
                    View.IMPORTANT_FOR_AUTOFILL_NO_EXCLUDE_DESCENDANTS
                } else {
                    View.IMPORTANT_FOR_AUTOFILL_AUTO
                }
                if (enabled) {
                    view.setAutofillHints(*emptyArray<String>())
                }
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                setAssistBlockedIfAvailable(view, enabled)
            }
        }

        if (filterTouchesWhenObscured) {
            view.filterTouchesWhenObscured = enabled
        }

        if (view is ViewGroup) {
            for (index in 0 until view.childCount) {
                updateViewTree(
                    view = view.getChildAt(index),
                    enabled = enabled,
                    blockAssistAndAutofill = blockAssistAndAutofill,
                    filterTouchesWhenObscured = filterTouchesWhenObscured,
                )
            }
        }
    }

    @RequiresApi(34)
    private object Api34Impl {
        fun setSensitive(view: View, enabled: Boolean) {
            view.setAccessibilityDataSensitive(
                if (enabled) {
                    View.ACCESSIBILITY_DATA_SENSITIVE_YES
                } else {
                    View.ACCESSIBILITY_DATA_SENSITIVE_AUTO
                },
            )
        }
    }

    private fun setAssistBlockedIfAvailable(view: View, enabled: Boolean) {
        runCatching {
            View::class.java
                .getMethod("setAssistBlocked", Boolean::class.javaPrimitiveType)
                .invoke(view, enabled)
        }
    }
}
