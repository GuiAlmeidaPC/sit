package com.sit.data

import com.sit.domain.Block
import com.sit.domain.BlockType
import com.sit.domain.RepeatBlock
import com.sit.domain.SimpleBlock

/**
 * Lightweight, dependency-free encoder for a polymorphic [Block] list.
 *
 * Format:
 *   blocks  := block ("|" block)*
 *   block   := "S:" id ":" TYPE ":" durationSec
 *            | "R:" id ":" repeats ":" step ("!" step)*
 *   step    := TYPE "=" durationSec
 *
 * `id` is a UUID-shaped string (no `:`, `|`, `!`, or `=` characters).
 */
internal object BlocksCodec {

    fun encode(blocks: List<Block>): String =
        blocks.joinToString("|") { encodeBlock(it) }

    fun decode(serialized: String?): List<Block> {
        if (serialized.isNullOrBlank()) return emptyList()
        return serialized.split('|').mapNotNull { runCatching { decodeBlock(it) }.getOrNull() }
    }

    private fun encodeBlock(block: Block): String = when (block) {
        is SimpleBlock -> "S:${block.id}:${block.type.name}:${block.durationSec}"
        is RepeatBlock -> buildString {
            append("R:").append(block.id).append(':').append(block.repeats)
            if (block.steps.isNotEmpty()) {
                append(':')
                block.steps.joinTo(this, "!") { "${it.type.name}=${it.durationSec}" }
            } else {
                append(':')
            }
        }
    }

    private fun decodeBlock(token: String): Block {
        return when {
            token.startsWith("S:") -> {
                val parts = token.removePrefix("S:").split(':', limit = 3)
                require(parts.size == 3) { "Bad SimpleBlock token: $token" }
                SimpleBlock(
                    id = parts[0],
                    type = BlockType.valueOf(parts[1]),
                    durationSec = parts[2].toInt(),
                )
            }
            token.startsWith("R:") -> {
                val parts = token.removePrefix("R:").split(':', limit = 3)
                require(parts.size >= 2) { "Bad RepeatBlock token: $token" }
                val id = parts[0]
                val repeats = parts[1].toInt()
                val stepsRaw = parts.getOrNull(2).orEmpty()
                val steps = if (stepsRaw.isBlank()) emptyList() else stepsRaw
                    .split('!')
                    .mapIndexed { index, stepToken ->
                        val (typeName, dur) = stepToken.split('=', limit = 2)
                        SimpleBlock(
                            id = "$id-s$index",
                            type = BlockType.valueOf(typeName),
                            durationSec = dur.toInt(),
                        )
                    }
                RepeatBlock(id = id, repeats = repeats, steps = steps)
            }
            else -> error("Unknown block token: $token")
        }
    }
}
