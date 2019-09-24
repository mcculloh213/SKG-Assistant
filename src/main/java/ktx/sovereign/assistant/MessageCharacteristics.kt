package ktx.sovereign.assistant

import ktx.sovereign.assistant.data.model.ClientState
import ktx.sovereign.assistant.data.model.Screen

sealed class MessageCharacteristics {
    object Input : MessageCharacteristics() {
        val INPUT_TYPE: Key<String> = Key (
                name = "badgerbot.input.INPUT_TYPE",
                type = String::class.java
        )
        val MESSAGE_TEXT: Key<String> = Key (
                name = "badgerbot.input.MESSAGE_TEXT",
                type = String::class.java
        )

        const val INPUT_TYPE_TEXT: String = "text"
    }
    object Intent : MessageCharacteristics() {
        val INCOMING_INTENT: Key<String> = Key (
                name = "badgerbot.intent.INCOMING_INTENT",
                type = String::class.java
        )
        val OUTGOING_INTENT: Key<String> = Key (
                name = "badgerbot.intent.OUTGOING_INTENT",
                type = String::class.java
        )
        val INCOMING_INTENT_CONFIDENCE: Key<Double> = Key (
                name = "badgerbot.intent.INCOMING_INTENT_CONFIDENCE",
                type = Double::class.java
        )
        val OUTGOING_INTENT_CONFIDENCE: Key<Double> = Key (
                name = "badgerbot.intent.OUTGOING_INTENT_CONFIDENCE",
                type = Double::class.java
        )

        const val ACTION_READ_SCREEN: String = "Action_Read_Screen"
    }
    object Context : MessageCharacteristics() {
        val RESPOND_TO_INTENT: Key<String> = Key (
                name = "badgerbot.context.RESPOND_TO_INTENT",
                type = String::class.java
        )
        val CLIENT_STATE: Key<ClientState> = Key (
                name = "badgerbot.context.STATE",
                type = ClientState::class.java
        )
        val SCREEN_STATE: Key<Screen> = Key (
                name = "badgerbot.context.state.SCREEN",
                type = Screen::class.java
        )
        val CURRENT_TRACKABLE: Key<String> = Key (
                name = "badgerbot.context.state.screen.TRACKABLE",
                type = String::class.java
        )
        val CURRENT_CONTEXT: Key<String> = Key (
                name = "badgerbot.context.state.screen.CONTEXT",
                type = String::class.java
        )
        val SCREEN_STATE_ERROR: Key<Boolean> = Key (
                name = "badgerbot.context.state.screen.ERROR",
                type = Boolean::class.java
        )

        const val KEY_IN_RESPONSE_TO: String = "response"
        const val KEY_STATE: String = "state"
        const val KEY_SCREEN: String = "screen"
        const val KEY_SCREEN_TRACKABLE: String = "trackable"
        const val KEY_SCREEN_CONTEXT: String = "context"
        const val KEY_SCREEN_ERROR: String = "error"
    }
}

data class Key<T>(
        val name: String,
        val type: Class<T>
) {
    override fun toString(): String = name
}