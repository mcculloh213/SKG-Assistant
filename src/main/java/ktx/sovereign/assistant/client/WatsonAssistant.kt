package ktx.sovereign.assistant.client

import android.net.Uri
import android.os.Bundle
import android.util.Log
import com.ibm.cloud.sdk.core.security.basicauth.BasicAuthConfig
import com.ibm.cloud.sdk.core.service.exception.NotFoundException
import com.ibm.cloud.sdk.core.service.exception.RequestTooLargeException
import com.ibm.cloud.sdk.core.service.exception.ServiceResponseException
import com.ibm.watson.assistant.v2.Assistant
import com.ibm.watson.assistant.v2.model.*
import ktx.sovereign.assistant.MessageCharacteristics
import ktx.sovereign.assistant.data.model.Response
import ktx.sovereign.assistant.data.Result
import ktx.sovereign.assistant.data.model.ClientState
import java.util.concurrent.atomic.AtomicReference

class WatsonAssistant : AssistantClient {
    private val session: AtomicReference<Session> = AtomicReference()
    private val lastContext: AtomicReference<MessageContext?> = AtomicReference()
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
            val msg = BadgerBotContext.Wrapper(watson.message(prepareMessage(text)).execute().result)
            Result.Success(Response(
                    text = msg.text,
                    data = msg.deepLink,
                    broadcast = msg.broadcast
            )).also { lastContext.set(msg.context) }
        } catch (ex: NotFoundException) {
            Result.Error("Sorry, I was lost, but I think I know where I am again.", ex)
        } catch (ex: RequestTooLargeException) {
            Result.Error("That's way too much data!", ex)
        } catch (ex: ServiceResponseException) {
            Result.Error("Whoops, I dropped the ball there.", ex)
        }
    }
    override fun message(bundle: Bundle): Result<Response> {
        val ctx = lastContext.get() ?: return Result.Error("I'm sorry, what was I doing?")
        return try {
            val out = Message.orchestrate(session.get().token, ctx, bundle)
            val msg = BadgerBotContext.Wrapper(watson.message(out).execute().result)
            Result.Success(Response(
                    text = msg.text,
                    data = msg.deepLink,
                    broadcast = msg.broadcast
            )).also { lastContext.set(msg.context) }
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
            .context(BadgerBotContext.Builder().commit())
            .input(MessageInput.Builder().apply {
                messageType(MESSAGE_INPUT_TYPE_TEXT)
                options(buildMessageOptions())
                text(message)
            }.build()).build()
    private fun buildMessageOptions(): MessageInputOptions = MessageInputOptions().apply {
        isRestart = true
        isReturnContext = true
    }
    data class Session(val token: String)
    private sealed class BadgerBotContext {
        class Wrapper(private val _resp: MessageResponse) {
            val context: MessageContext
                get() = _resp.context
            val intents: List<RuntimeIntent>
                get() = _resp.output.intents
            val text: String
                get() = _resp.output.generic[0].text
            val deepLink: Uri?
                get() = (_resp.context?.skills?.get(MESSAGE_CONTEXT_SKILLS_KEY)?.userDefined?.remove(KEY_URI) as String?)?.let { Uri.parse(it) }
            val broadcast: String?
                get() = _resp.context?.skills?.get(MESSAGE_CONTEXT_SKILLS_KEY)?.userDefined?.remove(KEY_BROADCAST) as String?
        }
        class Builder(last: MessageContext? = null) {
            private val context: MessageContext = MessageContext().apply {
                last?.let {
                    global = it.global
                    skills = it.skills
                }
            }
            fun setRespondingTo(intent: String): Builder {
                context.userDefined.putIfAbsent(
                        MessageCharacteristics.Context.KEY_IN_RESPONSE_TO,
                        intent
                )
                return this
            }
            fun setClientState(state: ClientState): Builder {
                context.userDefined[MessageCharacteristics.Context.KEY_STATE] = mutableMapOf<String, Any>(
                        Pair (
                                first = MessageCharacteristics.Context.KEY_SCREEN,
                                second = mutableMapOf<String, Any?>(
                                        Pair (
                                                first = MessageCharacteristics.Context.KEY_SCREEN_TRACKABLE,
                                                second = state.screen.trackable
                                        ),
                                        Pair (
                                                first = MessageCharacteristics.Context.KEY_SCREEN_CONTEXT,
                                                second = state.screen.context
                                        ),
                                        Pair (
                                                first = MessageCharacteristics.Context.KEY_SCREEN_ERROR,
                                                second = state.screen.error
                                        )
                                )
                        )
                )
                return this
            }
            private val MessageContext.userDefined: MutableMap<String, Any>
                get() = skills[MESSAGE_CONTEXT_SKILLS_KEY].userDefined
            private val MessageContext.turnCount
                get() = global.system.turnCount
            private fun MessageContext.incrementTurnCount() { global?.system?.apply { turnCount += 1 } }
            fun commit(): MessageContext = context.also { it.incrementTurnCount() }
        }
        companion object {
            @JvmStatic val KEY_URI: String = "uri"
            @JvmStatic val KEY_BROADCAST: String = "broadcast"
        }
    }
    sealed class Message {
        class OptionsBuilder(token: String) : Message() {
            private val builder = MessageOptions.Builder()
                    .assistantId(ASSISTANT_ID)
                    .sessionId(token)
            fun setInputOptions(options: MessageInput): OptionsBuilder {
                builder.input(options)
                return this
            }
            fun setContext(context: MessageContext): OptionsBuilder {
                builder.context(context)
                return this
            }
            fun commit(): MessageOptions = builder.build()
        }
        class InputBuilder : Message() {
            private val builder = MessageInput.Builder()
            fun useDefaultMessageType(): InputBuilder {
                builder.messageType(MessageCharacteristics.Input.INPUT_TYPE_TEXT)
                return this
            }
            fun useEmptyMessage(): InputBuilder {
                builder.text("")
                return this
            }
            fun setOptions(options: MessageInputOptions): InputBuilder {
                builder.options(options)
                return this
            }
            fun commit(): MessageInput = builder.build()
        }
        class InputOptionsBuilder : Message() {
            private val options = MessageInputOptions().apply { isReturnContext = true }
            fun setDebug(): InputOptionsBuilder {
                options.isDebug = true
                return this
            }
            fun setRestart(): InputOptionsBuilder {
                options.isRestart = true
                return this
            }
            fun includeAlternateIntents(): InputOptionsBuilder {
                options.isAlternateIntents = true
                return this
            }
            fun commit(): MessageInputOptions = options
        }
        class IntentBuilder() : Message() {
            constructor(list: List<RuntimeIntent>) : this() { intents.addAll(list) }
            constructor(intent: String, confidence: Double) : this() { addIntent(intent, confidence) }
            private val intents: MutableList<RuntimeIntent> = mutableListOf()
            fun addIntent(intent: String, confidence: Double): IntentBuilder {
                intents.add(toRuntimeIntent(intent, confidence))
                return this
            }
            fun addIntent(intent: RuntimeIntent): IntentBuilder {
                intents.add(intent)
                return this
            }
            fun commit() = mutableListOf(intents)
            private fun toRuntimeIntent(intent: String, confidence: Double) = RuntimeIntent().apply {
                setIntent(intent)
                setConfidence(confidence)
            }
        }
        companion object {
            fun orchestrate(sessionToken: String, lastContext: MessageContext, bundle: Bundle): MessageOptions {
                val builder = OptionsBuilder(sessionToken)
                builder.setInputOptions(InputBuilder().apply {
                    useDefaultMessageType()
                    useEmptyMessage()
                    setOptions(InputOptionsBuilder().apply {
                        setDebug()
                    }.commit())
                }.commit())
                builder.setContext(BadgerBotContext.Builder(lastContext).apply {
                    bundle.getString(MessageCharacteristics.Context.RESPOND_TO_INTENT.toString())?.let {
                        setRespondingTo(it)
                    }
                    bundle.getParcelable<ClientState>(MessageCharacteristics.Context.CLIENT_STATE.toString())?.let {
                        setClientState(it)
                    }
                }.commit())
                return builder.commit()
            }
        }
    }

    companion object {
        @JvmStatic private val ASSISTANT_VERSION: String = "2019-02-28"
        @JvmStatic private val ASSISTANT_USERNAME: String = "effda992-a72d-4eeb-8468-67fb25029404"
        @JvmStatic private val ASSISTANT_PASSWORD: String = "xTr4eQYoLroS"
        @JvmStatic private val ASSISTANT_ID: String = "37afbda3-2711-4340-a0f5-d3c929dd5cf5"

        @JvmStatic private val MESSAGE_CONTEXT_SKILLS_KEY: String = "main skill"
        @JvmStatic private val MESSAGE_INPUT_TYPE_TEXT: String = "text"
    }
}