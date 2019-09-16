package ktx.sovereign.assistant.data.model

import android.net.Uri
import com.stfalcon.chatkit.commons.models.IMessage
import com.stfalcon.chatkit.commons.models.IUser
import java.util.*

data class Message(
    private val id: String,
    private val createdAt: Date,
    private val user: IUser,
    private val text: String,
    val data: Uri? = null
) : IMessage {
    override fun getId(): String = id
    override fun getCreatedAt(): Date = createdAt
    override fun getUser(): IUser = user
    override fun getText(): String = text
}