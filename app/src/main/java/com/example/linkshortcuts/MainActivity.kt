package com.example.linkshortcuts

import android.net.Uri
import android.os.Bundle
import android.view.Gravity
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.result.contract.ActivityResultContracts
import android.content.Intent

class MainActivity : ComponentActivity() {

    /** היעד שנבחר ומחכה לשמירה בקובץ .link. בפורמט "סוג|uri" */
    private var pendingTarget: String? = null
    private lateinit var status: TextView

    private val pickFile = registerForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        uri?.let { onTargetPicked("file", it) }
    }

    private val pickFolder = registerForActivityResult(ActivityResultContracts.OpenDocumentTree()) { uri ->
        uri?.let { onTargetPicked("folder", it) }
    }

    private val saveLink = registerForActivityResult(ActivityResultContracts.CreateDocument(LinkFile.MIME)) { uri ->
        uri?.let { saveShortcutFile(it) }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER_HORIZONTAL
            setPadding(48, 120, 48, 48)
        }

        val title = TextView(this).apply {
            text = getString(com.example.linkshortcuts.R.string.app_name)
            textSize = 26f
            gravity = Gravity.CENTER
        }

        status = TextView(this).apply {
            text = "בחר יעד לקיצור"
            textSize = 16f
            gravity = Gravity.CENTER
            setPadding(0, 40, 0, 40)
        }

        val btnFile = Button(this).apply {
            text = "קיצור לקובץ או לאפליקציה"
            setOnClickListener { pickFile.launch(arrayOf("*/*")) }
        }

        val btnFolder = Button(this).apply {
            text = "קיצור לתיקייה"
            setOnClickListener { pickFolder.launch(null) }
        }

        val help = TextView(this).apply {
            text = "אחרי הבחירה תבחר היכן לשמור את קובץ ה-.link. " +
                    "לחיצה עליו בסייר הקבצים תפתח את היעד מיד, בלי להציג את האפליקציה."
            textSize = 14f
            gravity = Gravity.CENTER
            setPadding(0, 48, 0, 0)
        }

        root.addView(title)
        root.addView(status)
        root.addView(btnFile)
        root.addView(btnFolder)
        root.addView(help)
        setContentView(root)
    }

    private fun onTargetPicked(kind: String, uri: Uri) {
        // שמירת הרשאה קבועה כדי שהקיצור יעבוד גם אחרי הפעלה מחדש של המכשיר
        runCatching {
            contentResolver.takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        pendingTarget = LinkFile.encode(kind, uri)
        status.text = "נבחר: ${LinkFile.displayName(this, uri)}\nעכשיו בחר היכן לשמור את הקיצור"
        saveLink.launch("קיצור${LinkFile.EXTENSION}")
    }

    private fun saveShortcutFile(uri: Uri) {
        val data = pendingTarget
        if (data == null) {
            Toast.makeText(this, "לא נבחר יעד", Toast.LENGTH_SHORT).show()
            return
        }
        try {
            contentResolver.openOutputStream(uri, "wt")?.use { it.write(data.toByteArray(Charsets.UTF_8)) }
                ?: throw IllegalStateException("לא ניתן לכתוב לקובץ")
            status.text = "הקיצור נשמר בהצלחה"
        } catch (e: Exception) {
            Toast.makeText(this, "השמירה נכשלה: ${e.message}", Toast.LENGTH_LONG).show()
        } finally {
            pendingTarget = null
        }
    }
}
