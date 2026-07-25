package com.custom.bang

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.view.inputmethod.InputMethodManager
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        try {
            setContentView(R.layout.activity_main)

            findViewById<android.widget.Button>(R.id.btnEnableKeyboard).setOnClickListener {
                startActivity(Intent(Settings.ACTION_INPUT_METHOD_SETTINGS))
            }

            findViewById<android.widget.Button>(R.id.btnSwitchKeyboard).setOnClickListener {
                val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
                imm.showInputMethodPicker()
            }
        } catch (t: Throwable) {
            val trace = android.util.Log.getStackTraceString(t)
            AlertDialog.Builder(this)
                .setTitle("Crash caught: ${t.javaClass.simpleName}")
                .setMessage(trace)
                .setPositiveButton("OK", null)
                .setCancelable(false)
                .show()
        }
    }
}
