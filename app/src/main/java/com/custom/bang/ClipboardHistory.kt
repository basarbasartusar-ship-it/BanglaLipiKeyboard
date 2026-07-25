package com.custom.bang

import android.content.ClipboardManager
import android.content.Context

/**
 * Keeps a small rolling history of text copied on the device while the
 * keyboard is active, so the clipboard icon can show recent items to
 * re-insert. Backed by SharedPreferences so it survives keyboard restarts.
 */
class ClipboardHistory(context: Context) {

    private val prefs = context.getSharedPreferences("clipboard_history", Context.MODE_PRIVATE)
    private val clipboardManager =
        context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager

    private val listener = ClipboardManager.OnPrimaryClipChangedListener {
        val clip = clipboardManager.primaryClip ?: return@OnPrimaryClipChangedListener
        if (clip.itemCount == 0) return@OnPrimaryClipChangedListener
        val text = clip.getItemAt(0).coerceToText(context)?.toString() ?: return@OnPrimaryClipChangedListener
        if (text.isNotBlank()) addItem(text)
    }

    fun start() {
        clipboardManager.addPrimaryClipChangedListener(listener)
    }

    fun stop() {
        clipboardManager.removePrimaryClipChangedListener(listener)
    }

    private fun addItem(text: String) {
        val current = getHistory().toMutableList()
        current.remove(text)
        current.add(0, text)
        while (current.size > MAX_ITEMS) current.removeAt(current.size - 1)
        prefs.edit().putString(KEY, current.joinToString(SEPARATOR)).apply()
    }

    fun getHistory(): List<String> {
        val raw = prefs.getString(KEY, "") ?: ""
        if (raw.isEmpty()) return emptyList()
        return raw.split(SEPARATOR).filter { it.isNotBlank() }
    }

    fun clear() {
        prefs.edit().remove(KEY).apply()
    }

    companion object {
        private const val KEY = "history"
        private const val SEPARATOR = "\u0001"
        private const val MAX_ITEMS = 15
    }
}
