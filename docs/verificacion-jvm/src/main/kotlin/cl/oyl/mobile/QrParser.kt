package cl.oyl.mobile

fun qrProductId(value: String): Long? {
    val match = Regex("(\\d+)$").find(value.trim()) ?: return null
    return match.groupValues[1].toLongOrNull()
}

