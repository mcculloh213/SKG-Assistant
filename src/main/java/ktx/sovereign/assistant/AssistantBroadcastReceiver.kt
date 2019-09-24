package ktx.sovereign.assistant

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class AssistantBroadcastReceiver(
        private val block: (Context, Intent) -> Unit
) : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) = block.invoke(context, intent)
    companion object {
        @JvmStatic val ACTION_READ_SCREEN: String = "badgerbot.action.READ_SCREEN"
        @JvmStatic val ACTION_RETURN_TO_SENDER: String = "badgerbot.action.RETURN_TO_SENDER"

        @JvmStatic val EXTRA_RESULTS: String = "badgerbot.extra.RESULTS"
    }
}