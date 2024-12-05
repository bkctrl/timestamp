package org.timestamp.lib.dto

import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import java.time.LocalDateTime
import java.time.OffsetDateTime
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter

object OffsetDateTimeSerializer : KSerializer<OffsetDateTime> {
    private val formatter = DateTimeFormatter.ISO_OFFSET_DATE_TIME

    override val descriptor = PrimitiveSerialDescriptor("OffsetDateTime", PrimitiveKind.STRING)

    override fun deserialize(decoder: kotlinx.serialization.encoding.Decoder): OffsetDateTime {
        return OffsetDateTime.parse(decoder.decodeString(), formatter).toLocal()
    }

    override fun serialize(encoder: kotlinx.serialization.encoding.Encoder, value: OffsetDateTime) {
        encoder.encodeString(value.format(formatter))
    }
}

fun utcNow(): OffsetDateTime {
    return OffsetDateTime.now().toUtc()
}

fun OffsetDateTime.toUtc(): OffsetDateTime {
    return this.withOffsetSameInstant(ZoneOffset.UTC)
}

fun OffsetDateTime.toLocal(): OffsetDateTime {
    return this.withOffsetSameInstant(OffsetDateTime.now().offset)
}

fun LocalDateTime.toOffset(): OffsetDateTime {
    return this.atOffset(OffsetDateTime.now().offset)
}