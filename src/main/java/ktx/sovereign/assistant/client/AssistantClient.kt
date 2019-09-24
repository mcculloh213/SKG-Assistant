package ktx.sovereign.assistant.client

import android.os.Bundle
import ktx.sovereign.assistant.data.model.Response
import ktx.sovereign.assistant.data.Result

interface AssistantClient {
    suspend fun createSession()
    fun message(text: String): Result<Response>
    fun message(bundle: Bundle): Result<Response>
    fun dispose()
}