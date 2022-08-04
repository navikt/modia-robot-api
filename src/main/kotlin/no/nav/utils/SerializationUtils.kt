package no.nav.utils

import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import java.util.*
import kotlin.reflect.KClass

object UUIDSerializer : KSerializer<UUID> {
    override val descriptor = PrimitiveSerialDescriptor("UUID", PrimitiveKind.STRING)

    override fun deserialize(decoder: Decoder): UUID {
        return UUID.fromString(decoder.decodeString())
    }

    override fun serialize(encoder: Encoder, value: UUID) {
        encoder.encodeString(value.toString())
    }
}

abstract class EnumSerializer<T : Enum<T>>(clazz: KClass<T>, private val defaultValue: T) : KSerializer<T> {
    override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor(
        serialName = requireNotNull(clazz.qualifiedName),
        kind = PrimitiveKind.STRING
    )
    private val deserializeLUT = clazz.java.enumConstants.associateBy { it.serialName }
    private val serializeLUT = deserializeLUT.swapKeyValue()

    override fun deserialize(decoder: Decoder): T {
        return deserializeLUT[decoder.decodeString()] ?: defaultValue
    }

    override fun serialize(encoder: Encoder, value: T) {
        encoder.encodeString(serializeLUT.getValue(value))
    }

    private val Enum<T>.serialName: String
        get() = this::class.java.getField(name).getAnnotation(SerialName::class.java)?.value ?: name
}