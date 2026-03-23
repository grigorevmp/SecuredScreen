package com.grigorevmp.secureui

import android.os.Build
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import java.util.Collections
import java.util.WeakHashMap

internal object SecureAccessibilityScrubber {

    private val originalDelegates = Collections.synchronizedMap(
        WeakHashMap<View, View.AccessibilityDelegate?>(),
    )

    fun applyRecursively(
        root: View,
        enabled: Boolean,
    ) {
        updateDelegate(root, enabled)
        if (root is ViewGroup) {
            for (index in 0 until root.childCount) {
                applyRecursively(root.getChildAt(index), enabled)
            }
        }
    }

    private fun updateDelegate(
        view: View,
        enabled: Boolean,
    ) {
        if (enabled) {
            if (!originalDelegates.containsKey(view)) {
                val originalDelegate = view.accessibilityDelegate
                originalDelegates[view] = originalDelegate
                view.accessibilityDelegate = ScrubbingDelegate()
            }
            return
        }

        if (originalDelegates.containsKey(view)) {
            view.accessibilityDelegate = originalDelegates.remove(view)
        }
    }

    private class ScrubbingDelegate : View.AccessibilityDelegate() {

        override fun sendAccessibilityEvent(host: View, eventType: Int) = Unit

        override fun sendAccessibilityEventUnchecked(
            host: View,
            event: AccessibilityEvent,
        ) {
            sanitizeEvent(event)
        }

        override fun dispatchPopulateAccessibilityEvent(
            host: View,
            event: AccessibilityEvent,
        ): Boolean {
            sanitizeEvent(event)
            return true
        }

        override fun onPopulateAccessibilityEvent(
            host: View,
            event: AccessibilityEvent,
        ) {
            sanitizeEvent(event)
        }

        override fun onInitializeAccessibilityEvent(
            host: View,
            event: AccessibilityEvent,
        ) {
            sanitizeEvent(event)
            event.className = View::class.java.name
        }

        override fun onInitializeAccessibilityNodeInfo(
            host: View,
            info: AccessibilityNodeInfo,
        ) {
            super.onInitializeAccessibilityNodeInfo(host, info)
            sanitizeNode(host, info)
        }

        override fun onRequestSendAccessibilityEvent(
            host: ViewGroup,
            child: View,
            event: AccessibilityEvent,
        ): Boolean {
            sanitizeEvent(event)
            return false
        }

        override fun performAccessibilityAction(
            host: View,
            action: Int,
            args: Bundle?,
        ): Boolean = false

        override fun getAccessibilityNodeProvider(host: View) = null

        private fun sanitizeEvent(event: AccessibilityEvent) {
            event.text.clear()
            event.contentDescription = null
        }

        private fun sanitizeNode(
            host: View,
            info: AccessibilityNodeInfo,
        ) {
            info.text = null
            info.contentDescription = null
            info.className = View::class.java.name
            info.packageName = host.context.packageName
            info.isClickable = false
            info.isLongClickable = false
            info.isFocusable = false
            info.isScrollable = false
            info.isEnabled = false
            info.isVisibleToUser = false
            info.setViewIdResourceName(null)
            info.setError(null)
            info.setCollectionInfo(null)
            info.setCollectionItemInfo(null)
            info.setRangeInfo(null)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                info.setHintText(null)
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                info.isScreenReaderFocusable = false
                info.isShowingHintText = false
                info.setTooltipText(null)
                info.setPaneTitle(null)
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                info.setTextEntryKey(false)
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                info.setStateDescription(null)
            }
            if (Build.VERSION.SDK_INT >= 36) {
                info.setContainerTitle(null)
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                info.uniqueId = null
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                info.actionList.toList().forEach(info::removeAction)
            }
            if (host is ViewGroup) {
                for (index in 0 until host.childCount) {
                    info.removeChild(host.getChildAt(index))
                }
            }
            info.extras.clear()
        }
    }
}
