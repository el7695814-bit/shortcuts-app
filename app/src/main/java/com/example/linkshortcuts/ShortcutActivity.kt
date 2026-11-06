package com.example.linkshortcuts

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.DocumentsContract
import android.provider.Settings
import android.widget.Toast
import java.io.File

/**
 * מסך ללא ממשק. נפתח כשלוחצים על קובץ .link, פותח את היעד ומיד נסגר.
 */
class ShortcutActivity : Activity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val source = intent?.data
        if (source == null) {
            finish()
            return
        }

        // סייר קבצים שולח נתיב ישיר (file://). לקריאה כזו נדרשת גישה לכל הקבצים
        if (source.scheme == "file" && !hasAllFilesAccess()) {
            Toast.makeText(this, "צריך לאשר גישה לכל הקבצים עבור האפליקציה, ואז לנסות שוב", Toast.LENGTH_LONG).show()
            startActivity(
                Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION)
                    .setData(Uri.parse("package:$packageName"))
            )
            finish()
            return
        }

        try {
            val name = LinkFile.displayName(this, source)
            if (!name.endsWith(LinkFile.EXTENSION, ignoreCase = true)) {
                // קובץ אחר שנפתח בטעות דרך אותה אפשרות. יוצאים בשקט.
                finish()
                return
            }

            val text = readText(source)
            val (kind, target) = LinkFile.decode(text)
                ?: throw IllegalArgumentException("פורמט קיצור לא תקין")

            if (kind == "folder") openFolder(target) else openFile(target)
        } catch (e: Exception) {
            Toast.makeText(this, "לא ניתן לפתוח את הקיצור: ${e.message}", Toast.LENGTH_LONG).show()
        }

        finish()
    }

    private fun hasAllFilesAccess(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            Environment.isExternalStorageManager()
        } else {
            true
        }
    }

    private fun readText(source: Uri): String {
        return if (source.scheme == "file") {
            val path = source.path ?: throw IllegalArgumentException("נתיב לא תקין")
            File(path).readText(Charsets.UTF_8)
        } else {
            contentResolver.openInputStream(source)
                ?.bufferedReader(Charsets.UTF_8)
                ?.use { it.readText() }
                ?: throw IllegalStateException("הקובץ ריק או לא נגיש")
        }
    }

    private fun openFile(target: Uri) {
        val mime = contentResolver.getType(target) ?: "*/*"
        val intent = Intent(Intent.ACTION_VIEW)
            .setDataAndType(target, mime)
            .addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_ACTIVITY_NEW_TASK)
        startActivity(intent)
    }

    private fun openFolder(treeUri: Uri) {
        // ממירים עץ תיקיות לכתובת של התיקייה עצמה, כדי שסייר הקבצים יפתח אותה
        val docUri = DocumentsContract.buildDocumentUriUsingTree(
            treeUri,
            DocumentsContract.getTreeDocumentId(treeUri)
        )
        val intent = Intent(Intent.ACTION_VIEW)
            .setDataAndType(docUri, DocumentsContract.Document.MIME_TYPE_DIR)
            .addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_ACTIVITY_NEW_TASK)
        startActivity(intent)
    }
}
