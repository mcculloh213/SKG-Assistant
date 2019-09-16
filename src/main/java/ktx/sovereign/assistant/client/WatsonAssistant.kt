package ktx.sovereign.assistant.client

import android.net.Uri
import android.util.Log
import com.ibm.cloud.sdk.core.security.basicauth.BasicAuthConfig
import com.ibm.cloud.sdk.core.service.exception.NotFoundException
import com.ibm.cloud.sdk.core.service.exception.RequestTooLargeException
import com.ibm.cloud.sdk.core.service.exception.ServiceResponseException
import com.ibm.watson.assistant.v2.Assistant
import com.ibm.watson.assistant.v2.model.*
import ktx.sovereign.assistant.data.model.Response
import ktx.sovereign.assistant.data.Result
import java.util.concurrent.atomic.AtomicReference

class WatsonAssistant : AssistantClient {
    private val session: AtomicReference<Session> = AtomicReference()
    private val watson = Assistant(ASSISTANT_VERSION, BasicAuthConfig.Builder()
        .username(ASSISTANT_USERNAME)
        .password(ASSISTANT_PASSWORD)
        .build()
    )

    override suspend fun createSession() {
        try {
            val resp = watson.createSession(CreateSessionOptions.Builder()
                .assistantId(ASSISTANT_ID)
                .build()
            ).execute()
            session.set(Session(
                token = resp.result.sessionId
            ))
        } catch (ex: Exception) {
            Log.e("WatsonAssistant", "Failed to create session")
            ex.printStackTrace()
        }
    }
    override fun message(text: String): Result<Response> {
        return try {
            val msg = watson.message(prepareMessage(text)).execute().result
            val uri = msg.context?.skills?.get("main skill")?.userDefined?.remove("uri") as String?
            Result.Success(Response(
                text = msg.output.generic[0].text,
                data = uri?.let { Uri.parse(it) }
            ))
        } catch (ex: NotFoundException) {
            Result.Error("Sorry, I was lost, but I think I know where I am again.", ex)
        } catch (ex: RequestTooLargeException) {
            Result.Error("That's way too much data!", ex)
        } catch (ex: ServiceResponseException) {
            Result.Error("Whoops, I dropped the ball there.", ex)
        }
    }
    override fun dispose() {
        watson.deleteSession(
            DeleteSessionOptions.Builder()
                .assistantId(ASSISTANT_ID)
                .sessionId(session.get().token)
                .build()
        ).execute()
    }

    private fun prepareMessage(message: String): MessageOptions = MessageOptions.Builder()
        .assistantId(ASSISTANT_ID)
        .sessionId(session.get().token)
        .input(MessageInput.Builder().apply {
            messageType("text")
            options(buildMessageOptions())
            text(message)
        }.build()).build()
    private fun buildMessageOptions(): MessageInputOptions = MessageInputOptions().apply {
        isRestart = true
        isReturnContext = true
    }
    data class Session(val token: String)
    companion object {
        @JvmStatic private val ASSISTANT_VERSION: String = "2019-02-28"
        @JvmStatic private val ASSISTANT_USERNAME: String = "effda992-a72d-4eeb-8468-67fb25029404"
        @JvmStatic private val ASSISTANT_PASSWORD: String = "xTr4eQYoLroS"
        @JvmStatic private val ASSISTANT_ID: String = "37afbda3-2711-4340-a0f5-d3c929dd5cf5"
    }
}