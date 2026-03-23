package com.grigorevmp.secureui

@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
annotation class SecureScreen(
    val blockAccessibilityTree: Boolean = true,
    val scrubAccessibilityPayload: Boolean = true,
    val markAccessibilityDataSensitive: Boolean = true,
    val blockAssistAndAutofill: Boolean = true,
    val blockScreenshots: Boolean = true,
    val filterTouchesWhenObscured: Boolean = true,
)

data class SecureScreenConfig(
    val blockAccessibilityTree: Boolean = true,
    val scrubAccessibilityPayload: Boolean = true,
    val markAccessibilityDataSensitive: Boolean = true,
    val blockAssistAndAutofill: Boolean = true,
    val blockScreenshots: Boolean = true,
    val filterTouchesWhenObscured: Boolean = true,
)

internal fun SecureScreen.toConfig(): SecureScreenConfig = SecureScreenConfig(
    blockAccessibilityTree = blockAccessibilityTree,
    scrubAccessibilityPayload = scrubAccessibilityPayload,
    markAccessibilityDataSensitive = markAccessibilityDataSensitive,
    blockAssistAndAutofill = blockAssistAndAutofill,
    blockScreenshots = blockScreenshots,
    filterTouchesWhenObscured = filterTouchesWhenObscured,
)
