package com.custom.bang

import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.speech.RecognizerIntent
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat

/**
 * Transparent trampoline activity that launches the system speech recognizer,
 * grabs the recognized text, hands it back to the keyboard service through
 * [VoiceInputBridge], then closes itself.
 */
class VoiceTypingActivity : AppCompatActivity() {

    private val speechLauncher = registerForActivityResultCompat()
    private val micPermissionLauncher = registerForActivityResultCompat2()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.RECORD_AUDIO)
            != PackageManager.PERMISSION_GRANTED
        ) {
            micPermissionLauncher.launch(android.Manifest.permission.RECORD_AUDIO)
        } else {
            startRecognizer()
        }
    }

    private fun startRecognizer() {
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, "bn-BD")
            putExtra(RecognizerIntent.EXTRA_PROMPT, "বলুন...")
        }
        try {
            speechLauncher.launch(intent)
        } catch (e: Exception) {
            Toast.makeText(this, "Voice input not available on this device", Toast.LENGTH_SHORT).show()
            finish()
        }
    }

    private fun registerForActivityResultCompat() =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            val text = result.data
                ?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
                ?.firstOrNull()
            if (!text.isNullOrEmpty()) {
                VoiceInputBridge.onResult?.invoke(text)
            }
            finish()
        }

    private fun registerForActivityResultCompat2() =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
            if (granted) {
                startRecognizer()
            } else {
                Toast.makeText(this, "Microphone permission required", Toast.LENGTH_SHORT).show()
                finish()
            }
        }

    override fun onDestroy() {
        super.onDestroy()
    }
}
