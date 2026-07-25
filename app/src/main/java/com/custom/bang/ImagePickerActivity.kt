package com.custom.bang

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity

/**
 * Transparent trampoline activity that opens the system gallery/document
 * picker for images and GIFs, then hands the picked URI back to the keyboard
 * service through [ImagePickerBridge].
 */
class ImagePickerActivity : AppCompatActivity() {

    private val pickerLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            val uri = result.data?.data
            if (uri != null) {
                // Take a persistable read permission so the IME can access it afterwards.
                try {
                    contentResolver.takePersistableUriPermission(
                        uri, Intent.FLAG_GRANT_READ_URI_PERMISSION
                    )
                } catch (_: SecurityException) {
                    // Some providers don't support persistable permissions; ignore.
                }
                ImagePickerBridge.onImagePicked?.invoke(uri)
            } else {
                Toast.makeText(this, "No image selected", Toast.LENGTH_SHORT).show()
            }
            finish()
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val wantGif = intent.getBooleanExtra(EXTRA_WANT_GIF, false)
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = if (wantGif) "image/gif" else "image/*"
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        try {
            pickerLauncher.launch(intent)
        } catch (e: Exception) {
            Toast.makeText(this, "No gallery app found", Toast.LENGTH_SHORT).show()
            finish()
        }
    }

    companion object {
        const val EXTRA_WANT_GIF = "want_gif"
    }
}
