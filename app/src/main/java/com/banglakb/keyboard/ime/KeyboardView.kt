package com.banglakb.keyboard.ime

import android.content.Context
import android.graphics.Color
import android.graphics.Typeface
import android.os.Handler
import android.os.Looper
import android.util.AttributeSet
import android.view.Gravity
import android.view.HapticFeedbackConstants
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.PopupWindow
import android.widget.TextView
import com.banglakb.keyboard.data.KeyModel
import com.banglakb.keyboard.data.KeyType
import com.banglakb.keyboard.data.KeyboardLayoutData
import com.banglakb.keyboard.util.AppTheme
import com.banglakb.keyboard.util.Prefs

interface KeyboardActionListener {
    fun onKey(key: KeyModel, isShifted: Boolean)
    fun onRepeatableBackspace()
}

class KeyboardView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : LinearLayout(context, attrs) {

    var listener: KeyboardActionListener? = null
    var isShifted: Boolean = false
        set(value) {
            field = value
            render()
        }
    var isCapsLock: Boolean = false

    private var currentLayoutData: KeyboardLayoutData? = null
    private val prefs = Prefs(context)
    private val mainHandler = Handler(Looper.getMainLooper())
    private var backspaceRepeatRunnable: Runnable? = null
    private var activePopup: PopupWindow? = null

    init {
        orientation = VERTICAL
        setPadding(dp(4), dp(6), dp(4), dp(6))
    }

    fun setKeyboardLayout(data: KeyboardLayoutData) {
        currentLayoutData = data
        render()
    }

    private fun render() {
        removeAllViews()
        val data = currentLayoutData ?: return
        val heightPct = prefs.keyboardHeightPercent / 100f

        for (row in data.rows) {
            val rowLayout = LinearLayout(context).apply {
                orientation = HORIZONTAL
                layoutParams = LayoutParams(
                    LayoutParams.MATCH_PARENT,
                    (dp(52) * heightPct).toInt()
                ).also { it.setMargins(0, dp(2), 0, dp(2)) }
            }
            for (key in row) {
                rowLayout.addView(buildKeyView(key))
            }
            addView(rowLayout)
        }
    }

    private fun buildKeyView(key: KeyModel): View {
        val label = if (isShifted || isCapsLock) key.shiftedLabel else key.label
        val tv = TextView(context).apply {
            text = if (key.type == KeyType.SPACE) "" else label
            gravity = Gravity.CENTER
            textSize = if (key.type == KeyType.CHARACTER) 20f else 16f
            setTextColor(currentTextColor())
            typeface = Typeface.DEFAULT
            isClickable = true
            isFocusable = true
            background = keyBackground()
            layoutParams = LayoutParams(0, LayoutParams.MATCH_PARENT, key.widthWeight).also {
                it.setMargins(dp(2), 0, dp(2), 0)
            }
        }

        tv.setOnTouchListener { v, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    v.alpha = 0.6f
                    if (prefs.keyPopupEnabled && key.type == KeyType.CHARACTER) showPopup(v, label)
                    if (prefs.hapticEnabled) v.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
                    if (key.type == KeyType.BACKSPACE) startBackspaceRepeat()
                }
                MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                    v.alpha = 1f
                    dismissPopup()
                    stopBackspaceRepeat()
                }
            }
            false
        }

        tv.setOnClickListener {
            listener?.onKey(key, isShifted || isCapsLock)
        }

        return tv
    }

    private fun startBackspaceRepeat() {
        stopBackspaceRepeat()
        val runnable = object : Runnable {
            override fun run() {
                listener?.onRepeatableBackspace()
                mainHandler.postDelayed(this, 60)
            }
        }
        backspaceRepeatRunnable = runnable
        mainHandler.postDelayed(runnable, 400) // initial delay before repeat kicks in
    }

    private fun stopBackspaceRepeat() {
        backspaceRepeatRunnable?.let { mainHandler.removeCallbacks(it) }
        backspaceRepeatRunnable = null
    }

    private fun showPopup(anchor: View, label: String) {
        dismissPopup()
        val popupText = TextView(context).apply {
            text = label
            textSize = 26f
            gravity = Gravity.CENTER
            setTextColor(Color.WHITE)
            setBackgroundColor(Color.parseColor("#333333"))
            setPadding(dp(16), dp(8), dp(16), dp(8))
        }
        val popup = PopupWindow(popupText, dp(48), dp(48))
        popup.isTouchable = false
        popup.showAsDropDown(anchor, 0, -dp(70))
        activePopup = popup
    }

    private fun dismissPopup() {
        activePopup?.dismiss()
        activePopup = null
    }

    private fun keyBackground() = android.graphics.drawable.GradientDrawable().apply {
        cornerRadius = dp(6).toFloat()
        setColor(keyFillColor())
    }

    private fun keyFillColor(): Int = when (prefs.theme) {
        AppTheme.LIGHT -> Color.parseColor("#FFFFFF")
        AppTheme.DARK -> Color.parseColor("#3A3A3C")
        AppTheme.AMOLED -> Color.parseColor("#101010")
    }

    private fun currentTextColor(): Int = when (prefs.theme) {
        AppTheme.LIGHT -> Color.parseColor("#1C1C1E")
        AppTheme.DARK, AppTheme.AMOLED -> Color.parseColor("#F2F2F7")
    }

    fun applyBackgroundForTheme() {
        setBackgroundColor(
            when (prefs.theme) {
                AppTheme.LIGHT -> Color.parseColor("#ECECEC")
                AppTheme.DARK -> Color.parseColor("#1C1C1E")
                AppTheme.AMOLED -> Color.BLACK
            }
        )
    }

    private fun dp(value: Int): Int = (value * resources.displayMetrics.density).toInt()
}
