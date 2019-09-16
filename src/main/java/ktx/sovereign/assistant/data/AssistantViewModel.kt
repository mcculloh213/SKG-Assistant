package ktx.sovereign.assistant.data

import android.content.Intent
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import ktx.sovereign.assistant.BadgerBot
import ktx.sovereign.assistant.DomainNameSystem
import ktx.sovereign.assistant.client.WatsonAssistant
import ktx.sovereign.assistant.data.model.Message
import ktx.sovereign.assistant.data.model.Sender
import java.util.*

class AssistantViewModel private constructor(
    private val assistant: BadgerBot,
    private val user: Sender
) : ViewModel(), CoroutineScope by CoroutineScope(Dispatchers.Default) {
    private val agent: Sender = Sender("1", "BadgerBot", "https://robohash.org/badgerbot")
    private val _messages = MutableLiveData<List<Message>>()
    val messages: LiveData<List<Message>> = _messages
    private val _latestMessage = MutableLiveData<Message>()
    val latestMessage: LiveData<Message> = _latestMessage

    fun sendMessage(message: String) = launch {
        _latestMessage.postValue(Message(
            id = "",
            createdAt = Date(),
            user = user,
            text = message
        ))
        val response = assistant.sendMessage(message)
        _latestMessage.postValue(Message(
            id = "",
            createdAt = Date(),
            user = agent,
            text = response.text,
            data = response.data?.let { DomainNameSystem.validate(it) }
        ))
    }

    class Factory : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel?> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(AssistantViewModel::class.java)) {
                return AssistantViewModel(
                    assistant = BadgerBot(
                        client = WatsonAssistant()
                    ),
                    user = Sender(
                        id = "0",
                        name = "That Guy"
                    )
                ) as T
            }
            throw ClassCastException("Unknown ViewModel Type")
        }

    }
}