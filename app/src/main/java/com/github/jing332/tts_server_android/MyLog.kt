package com.github.jing332.tts_server_android

import android.graphics.Color
import java.io.Serializable

class MyLog(var level: Int, var msg: String) : Serializable {
    /*fun toText(): String {
        return LogLevel.toString(level)
    }*/

    fun toColor(): Int {
        return when {
            level == LogLevel.WARN -> {
                Color.rgb(255, 215, 0) /* 金色 */
            }
            level <= LogLevel.ERROR -> {
                Color.RED
            }
            else -> {
                Color.GRAY
            }
        }
    }

}

object LogLevel {
    const val PANIC = 0
    const val FATIL = 1
    const val ERROR = 2
    const val WARN = 3
    const val INFO = 4
    const val DEBUG = 5
    const val TRACE = 6

    fun toString(level: Int): String {
        when (level) {
            PANIC -> return "宕机"
            FATIL -> return "致命"
            ERROR -> return "错误"
            WARN -> return "警告"
            INFO -> return "信息"
            DEBUG -> return "调试"
            TRACE -> return "详细"
        }
        return level.toString()
    }
}