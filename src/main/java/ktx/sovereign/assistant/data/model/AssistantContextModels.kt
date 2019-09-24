@file:JvmName("AssistantContext")
package ktx.sovereign.assistant.data.model

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.android.parcel.Parcelize

@Parcelize
data class ClientState (
        @SerializedName("screen") val screen: Screen
) : Parcelable

@Parcelize
data class Screen (
        @SerializedName("trackable") val trackable: String? = null,
        @SerializedName("context") val context: String? = null,
        @SerializedName("error") val error: Boolean = false
) : Parcelable