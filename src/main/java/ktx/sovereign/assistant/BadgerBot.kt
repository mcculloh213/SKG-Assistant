package ktx.sovereign.assistant

import android.os.Bundle
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import ktx.sovereign.assistant.client.AssistantClient
import ktx.sovereign.assistant.data.Result
import ktx.sovereign.assistant.data.model.Response

class BadgerBot(
    private val client: AssistantClient
) : CoroutineScope by CoroutineScope(Dispatchers.IO) {
    init { launch { client.createSession() } }
    suspend fun sendMessage(message: String): Response = withContext(coroutineContext) {
        when (val response = client.message(message)) {
            is Result.Success -> response.data
            is Result.Error -> Response(
                text = response.message
            )
        }
    }
    suspend fun sendMessage(bundle: Bundle): Response = withContext(coroutineContext) {
        when (val response = client.message(bundle)) {
            is Result.Success -> response.data
            is Result.Error -> Response(
                    text = response.message
            )
        }
    }
}