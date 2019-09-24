package ktx.sovereign.assistant

import android.content.UriMatcher
import android.net.Uri

object DomainNameSystem {
    @JvmStatic private val matcher: UriMatcher = UriMatcher(UriMatcher.NO_MATCH).apply {
        // Home URI's
        addURI(Host.Home, "/", Home.INDEX)

        // Notes URI's
        addURI(Host.Notes, "/", Notes.INDEX)
        addURI(Host.Notes, "/index", Notes.INDEX)
        addURI(Host.Notes, "/create", Notes.CREATE)
        addURI(Host.Notes, "/#", Notes.READ)
        addURI(Host.Notes, "/#/update", Notes.UPDATE)
        addURI(Host.Notes, "/#/delete", Notes.DELETE)

        // Gallery URI's
        addURI(Host.Gallery, "/", Gallery.INDEX)
        addURI(Host.Gallery, "/index", Gallery.INDEX)
        addURI(Host.Gallery, "/import", Gallery.CREATE)
        addURI(Host.Gallery, "/*", Gallery.READ)
        addURI(Host.Gallery, "/*/update", Gallery.UPDATE)
        addURI(Host.Gallery, "/*/delete", Gallery.DELETE)

        // Content URI's
        addURI(Host.Content, "/", Content.INDEX)
        addURI(Host.Content, "/index", Content.INDEX)
        addURI(Host.Content, "/create", Content.CREATE)
        addURI(Host.Content, "/*", Content.READ)
        addURI(Host.Content, "/*/update", Content.UPDATE)
        addURI(Host.Content, "/*/delete", Content.DELETE)

        // Reader URI's
        addURI(Host.Reader, "/", Reader.INDEX)
        addURI(Host.Reader, "/index", Reader.INDEX)
        addURI(Host.Reader, "/recognize", Reader.CREATE)

        // Camera URI's
        addURI(Host.Camera, "/", Camera.PHOTO)
        addURI(Host.Camera, "/photo", Camera.PHOTO)
        addURI(Host.Camera, "/video", Camera.VIDEO)

        // Task URI's
        addURI(Host.Tasks, "/", Tasks.INDEX)
        addURI(Host.Tasks, "/index", Tasks.INDEX)
        addURI(Host.Tasks, "/create", Tasks.CREATE)
        addURI(Host.Tasks, "/#", Tasks.READ)
        addURI(Host.Tasks, "/#/update", Tasks.UPDATE)
        addURI(Host.Tasks, "/#/delete", Tasks.DELETE)

        // Viewer URI's
        addURI(Host.Viewer, "/", Viewer.INDEX)
        addURI(Host.Viewer, "/index", Viewer.INDEX)
    }
    @JvmStatic private val SCHEME: String = "badgerapp"

    @JvmStatic private val OP_INDEX: String = "index"
    @JvmStatic private val OP_CREATE: String = "create"
    @JvmStatic private val OP_READ: String = "read"
    @JvmStatic private val OP_UPDATE: String = "update"
    @JvmStatic private val OP_DELETE: String = "delete"
    @JvmStatic private val OP_PHOTO: String = "photo"

    @JvmStatic fun validate(uri: Uri): Uri = when (matcher.match(uri)) {
        Notes.INDEX -> sanitize(uri, Host.Notes, OP_INDEX)
        Notes.CREATE -> sanitize(uri, Host.Notes, OP_CREATE)
        Notes.READ -> sanitize(uri, Host.Notes, OP_READ)
        Notes.UPDATE -> sanitize(uri, Host.Notes, OP_UPDATE)
        Notes.DELETE -> sanitize(uri, Host.Notes, OP_DELETE)
        Gallery.INDEX -> sanitize(uri, Host.Gallery, OP_INDEX)
        Content.INDEX -> sanitize(uri, Host.Content, OP_INDEX)
        Reader.INDEX -> sanitize(uri, Host.Reader, OP_INDEX)
        Camera.PHOTO -> sanitize(uri, Host.Camera, OP_PHOTO)
        Viewer.INDEX -> sanitize(uri, Host.Viewer, OP_INDEX)
        else -> sanitize(uri, Host.Home, OP_INDEX)
    }
    @JvmStatic fun match(uri: Uri) = matcher.match(uri)

    @JvmStatic private fun sanitize(dirty: Uri, authority: String, operation: String): Uri {
        return Uri.Builder()
            .scheme(SCHEME)
            .authority(authority)
            .appendPath(operation)
            .build()
    }

    object Notes {
        const val INDEX: Int = 0x00
        const val CREATE: Int = 0x01
        const val READ: Int = 0x02
        const val UPDATE: Int = 0x03
        const val DELETE: Int = 0x04
    }
    object Gallery {
        const val INDEX: Int = 0x10
        const val CREATE: Int = 0x11
        const val READ: Int = 0x12
        const val UPDATE: Int = 0x13
        const val DELETE: Int = 0x14
    }
    object Content {
        const val INDEX: Int = 0x20
        const val CREATE: Int = 0x21
        const val READ: Int = 0x22
        const val UPDATE: Int = 0x23
        const val DELETE: Int = 0x24
    }
    object Reader {
        const val INDEX: Int = 0x30
        const val CREATE: Int = 0x31
    }
    object Camera {
        const val PHOTO: Int = 0x40
        const val VIDEO: Int = 0x41
    }
    object Tasks {
        const val INDEX: Int = 0x50
        const val CREATE: Int = 0x51
        const val READ: Int = 0x52
        const val UPDATE: Int = 0x53
        const val DELETE: Int = 0x54
    }
    object Viewer {
        const val INDEX: Int = 0x60
    }
    object Home {
        const val INDEX: Int = 0xFF
    }
}