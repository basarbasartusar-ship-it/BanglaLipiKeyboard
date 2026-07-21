package com.example.banglalipi

import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.view.Gravity
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val layout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
            setPadding(48, 48, 48, 48)
        }

        val instructions = TextView(this).apply {
            this.text = "বাংলা লিপি কীবোর্ড ব্যবহার করতে:\n\n" +
                "১) নিচের বাটনে চেপে কীবোর্ডটি সক্রিয় (enable) করুন\n" +
                "২) যেকোনো টেক্সট বক্সে ট্যাপ করে কীবোর্ড আইকনে চেপে " +
                "\"বাংলা লিপি\" বেছে নিন"
            textSize = 16f
            setPadding(0, 0, 0, 48)
        }

        val enableButton = Button(this).apply {
            setText("কীবোর্ড সক্রিয় করুন")
            setOnClickListener {
                startActivity(Intent(Settings.ACTION_INPUT_METHOD_SETTINGS))
            }
        }

        val switchButton = Button(this).apply {
            setText("কীবোর্ড পরিবর্তন করুন")
            setOnClickListener {
                val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
                imm.showInputMethodPicker()
            }
        }

        layout.addView(instructions)
        layout.addView(enableButton)
        layout.addView(switchButton)
        setContentView(layout)
    }
}
