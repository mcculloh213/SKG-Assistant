package ktx.sovereign.assistant.data

import android.app.Application
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.util.Log
import androidx.lifecycle.*
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import ktx.sovereign.assistant.AssistantBroadcastReceiver
import ktx.sovereign.assistant.BadgerBot
import ktx.sovereign.assistant.DomainNameSystem
import ktx.sovereign.assistant.client.WatsonAssistant
import ktx.sovereign.assistant.data.model.Message
import ktx.sovereign.assistant.data.model.Sender
import java.util.*

class AssistantViewModel private constructor(
        application: Application,
        private val assistant: BadgerBot,
        private val user: Sender
) : AndroidViewModel(application), CoroutineScope by CoroutineScope(Dispatchers.Default) {
    private val agent: Sender = Sender("1", "BadgerBot", "https://robohash.org/badgerbot")
    private val _messages = MutableLiveData<List<Message>>()
    val messages: LiveData<List<Message>> = _messages
    private val _latestMessage = MutableLiveData<Message>()
    val latestMessage: LiveData<Message> = _latestMessage
    private val receiver: BroadcastReceiver = AssistantBroadcastReceiver { _, intent ->
        Log.i("BadgerBot", "Broadcast Received: ${intent.action}")
        when (intent.action) {
            AssistantBroadcastReceiver.ACTION_RETURN_TO_SENDER -> {
                intent.getBundleExtra(AssistantBroadcastReceiver.EXTRA_RESULTS)?.let { args -> secretMessage(args) }
            }
        }
    }
    private val filter: IntentFilter = IntentFilter().apply {
        addAction(AssistantBroadcastReceiver.ACTION_RETURN_TO_SENDER)
    }

    init {
        context.registerReceiver(receiver, filter)
    }

    fun sendMessage(message: String) = launch {
        _latestMessage.postValue(Message(
                id = "",
                createdAt = Date(),
                user = user,
                text = message
        ))
        val response = assistant.sendMessage(message)
        _latestMessage.postValue(Message (
                id = "",
                createdAt = Date(),
                user = agent,
                text = response.text,
                data = response.data?.let {
                    Log.i("Dirty", "$it")
                    DomainNameSystem.validate(it)
                }
        ))
        response.broadcast?.let { action -> sendBroadcast(action) }
    }
    override fun onCleared() {
        getApplication<Application>().unregisterReceiver(receiver)
        super.onCleared()
    }

    private fun secretMessage(bundle: Bundle) = launch {
        val response = assistant.sendMessage(bundle)
        _latestMessage.postValue(Message (
                id = "",
                createdAt = Date(),
                user = agent,
                text = response.text,
                data = response.data?.let { DomainNameSystem.validate(it) }
        ))
    }
    private fun sendBroadcast(action: String) = launch {
        Log.i("BadgerBot", "Broadcast: $action")
        with (Intent(action)) {
            context.sendBroadcast(this)
        }
    }

    protected val AndroidViewModel.context: Context
        get() = getApplication()

    class Factory(
            private val application: Application
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel?> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(AssistantViewModel::class.java)) {
                return AssistantViewModel(
                        application = application,
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