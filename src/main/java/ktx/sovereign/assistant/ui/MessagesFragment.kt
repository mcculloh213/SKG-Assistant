package ktx.sovereign.assistant.ui

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import com.squareup.picasso.Picasso
import com.stfalcon.chatkit.commons.ImageLoader
import com.stfalcon.chatkit.messages.MessageInput
import com.stfalcon.chatkit.messages.MessagesListAdapter
import com.stfalcon.chatkit.utils.DateFormatter
import kotlinx.android.synthetic.main.fragment_messages.*

import ktx.sovereign.assistant.R
import ktx.sovereign.assistant.data.AssistantViewModel
import ktx.sovereign.assistant.data.model.Message
import java.util.*

class MessagesFragment : Fragment(),
    MessageInput.InputListener, DateFormatter.Formatter {
    private lateinit var _model: AssistantViewModel
    private val loader = ImageLoader { imageView, url, _ ->
        Picasso.get().load(url).into(imageView)
    }
    private val adapter = MessagesListAdapter<Message>("0", loader).also {
        it.setDateHeadersFormatter(this)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _model = ViewModelProvider(viewModelStore, AssistantViewModel.Factory())
            .get(AssistantViewModel::class.java)
        _model.latestMessage.observe(this, androidx.lifecycle.Observer {
            val message = it ?: return@Observer
            adapter.addToStart(message, true)
            message.data?.let { uri ->
                startActivity(Intent().apply { data = uri })
            }
        })
    }
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? = inflater.inflate(R.layout.fragment_messages, container, false)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        message_list.setAdapter(adapter)
        input.setInputListener(this)
    }
    override fun onSubmit(input: CharSequence?): Boolean {
        input?.let { _model.sendMessage(it.toString()) }
        return true
    }
    override fun format(date: Date): String = when {
        DateFormatter.isToday(date) -> "Today"
        DateFormatter.isYesterday(date) -> "Yesterday"
        else -> DateFormatter.format(date, DateFormatter.Template.STRING_DAY_MONTH_YEAR)
    }
}
