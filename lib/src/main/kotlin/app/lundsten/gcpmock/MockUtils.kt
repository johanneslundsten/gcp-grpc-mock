package app.lundsten.gcpmock

import com.google.common.hash.Hashing
import java.util.Base64
import kotlin.text.Charsets.UTF_8

class MockUtils {
    companion object {
        fun serializeString(secretValue: String): SerializedString {
            val crc = Hashing.crc32c().hashBytes(secretValue.toByteArray()).padToLong()
            val serialized = Base64.getEncoder().encodeToString(secretValue.toByteArray(UTF_8))
            return SerializedString(serialized, crc)
        }
    }
}

data class SerializedString(val b64: String, val crc: Long)
