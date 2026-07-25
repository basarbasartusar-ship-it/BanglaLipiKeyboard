package com.custom.bang

import android.net.Uri

/**
 * An InputMethodService cannot call startActivityForResult itself, so voice
 * typing and image/GIF picking are done through small transparent trampoline
 * activities (VoiceTypingActivity / ImagePickerActivity). Those activities
 * report their result back to the running keyboard service through these
 * simple singleton callback holders.
 */
object VoiceInputBridge {
    var onResult: ((String) -> Unit)? = null
}

object ImagePickerBridge {
    var onImagePicked: ((Uri) -> Unit)? = null
}
