package com.github.jing332.tts_server_android.service.systts.help

import android.media.MediaDataSource
import android.os.Build
import androidx.annotation.RequiresApi

@RequiresApi(api = Build.VERSION_CODES.M)
class ByteArrayMediaDataSource(data: ByteArray) : MediaDataSource() {
    private var data: ByteArray?
    override fun readAt(position: Long, buffer: ByteArray, offset: Int, size: Int): Int {
        if (position >= data!!.size) {
            return -1
        }
        val endPosition = (position + size).toInt()
        var size2 = size
        if (endPosition > data!!.size) {
            size2 -= endPosition - data!!.size
        }
        data?.let { System.arraycopy(it, position.toInt(), buffer, offset, size2) }
        return size2
    }

    override fun getSize(): Long {
        return data!!.size.toLong()
    }

    override fun close() {
        data = null
    }

    init {
        this.data = data
    }
}