package com.example.linkshortcuts

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.DocumentsContract
import android.widget.Toast

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

        if (!LinkFile.displayName(this, source).endsWith(LinkFile.EXTENSION, ignoreCase = true)) {
            // קובץ אחר שנפתח בטעות דרך אותה אפשרות. יוצאים בשקט.
            finish()
            return
        }

        try {
            val text = contentResolver.openInputStream(source)?.bufferedReader(Charsets.UTF_8)?.use { it.readText() }
                ?: throw IllegalStateException("הקובץ ריק או לא נגיש")

            val (kind, target) = LinkFile.decode(text)
                ?: throw IllegalArgumentException("פורמט קיצור לא תקין")

            if (kind == "folder") openFolder(target) else openFile(target)
        } catch (e: Exception) {
            Toast.makeText(this, "לא ניתן לפתוח את הקיצור: ${e.message}", Toast.LENGTH_LONG).show()
        }

        finish()
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
