package dimensional.knats.tools

internal const val CR = 0x0D.toByte()
internal const val LF = 0x0A.toByte()
internal const val TAB = '\t'.code.toByte()
internal const val SPACE = ' '.code.toByte()
internal const val COLON = ':'.code.toByte()

internal val WHITESPACE = byteArrayOf(SPACE, TAB)
internal val CRLF = byteArrayOf(CR, LF)

