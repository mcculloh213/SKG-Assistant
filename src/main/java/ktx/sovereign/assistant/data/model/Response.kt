package ktx.sovereign.assistant.data.model

import android.net.Uri

data class Response (
    val text: String,
    val data: Uri? = null,
    val broadcast: String? = null
)