package com.example.linkshortcuts

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns

/**
 * פורמט קובץ הקיצור (.link):
 * שורה אחת בלבד בפורמט:  file|<uri>   או   folder|<uri>
 */
object LinkFile {

    const val MIME = "application/x-link"
    const val EXTENSION = ".link"

    fun encode(kind: String, target: Uri): String = "$kind|$target"

    /** מחזיר זוג (סוג, יעד) או null אם התוכן אינו קיצור תקין */
    fun decode(text: String): Pair<String, Uri>? {
        val line = text.lineSequence().map { it.trim() }.firstOrNull { it.isNotEmpty() } ?: return null
        val idx = line.indexOf('|')
        if (idx <= 0) return null
        val kind = line.substring(0, idx)
        if (kind != "file" && kind != "folder") return null
        return kind to Uri.parse(line.substring(idx + 1))
    }

    fun displayName(context: Context, uri: Uri): String {
        return runCatching {
            context.contentResolver.query(uri, arrayOf(OpenableColumns.DISPLAY_NAME), null, null, null)
                ?.use { c -> if (c.moveToFirst()) c.getString(0) else null }
        }.getOrNull() ?: uri.lastPathSegment ?: uri.toString()
    }
}
