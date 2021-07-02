package com.mrikso.ressimplify.core

import java.io.InputStream


class DetectExt {

    public fun getExtension(resPath: String): String {
        return resPath.substringAfterLast('.')
    }

    public fun getExtension(inputStream: InputStream, resPath: String): String {
        val extStream = detectExtension(inputStream)
        if (extStream.isNullOrEmpty())
            return getExtension(resPath)
        return extStream
    }

    private fun detectExtension(inputStream: InputStream): String? {
        val headers = ByteArray(4)
        val read: Int = inputStream.read(headers, 0, 4)
        if (read == headers.size) {
            return when (bytesToHex(headers)) {
                "03080000" -> "xml"
                "89504E47" -> "png"
                "47494638" -> "gif"
                "4F676753" -> "ogg"
                "FFD8FFDB",
                "FFD8FFE0",
                "FFD8FFE1" -> "jpg"
                else -> null
            }
        }

        /* val read1: Int = inputStream.read(headers, 8, 4)
         if (read1 == headers.size) {
             if (bytesToHex(headers) == "57454250")
                 return "webp"
         }*/
        return null
    }

    private val hexArray = "0123456789ABCDEF".toCharArray()

    private fun bytesToHex(bytes: ByteArray): String {
        val hexChars = CharArray(bytes.size * 2)
        for (j in bytes.indices) {
            val v = bytes[j].toInt() and 0xFF

            hexChars[j * 2] = hexArray[v ushr 4]
            hexChars[j * 2 + 1] = hexArray[v and 0x0F]
        }
        return String(hexChars)
    }
}