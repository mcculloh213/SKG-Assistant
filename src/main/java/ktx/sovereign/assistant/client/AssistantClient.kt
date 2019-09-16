package ktx.sovereign.assistant.client

import ktx.sovereign.assistant.data.model.Response
import ktx.sovereign.assistant.data.Result

interface AssistantClient {
    suspend fun createSession()
    fun message(text: String): Result<Response>
    fun dispose()
}