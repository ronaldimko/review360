package com.example.review360

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { Viewer360App() }
    }
}

@Composable
fun Viewer360App() {
    MaterialTheme {
        Surface(Modifier.fillMaxSize()) {
            ContentScreen()
        }
    }
}

@Composable
private fun ContentScreen() {
    val context = LocalContext.current
    var selectedUri by remember { mutableStateOf<Uri?>(null) }
    var pickedType by remember { mutableStateOf<String?>(null) }

    val openDocument = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        if (uri != null) {
            selectedUri = uri
            pickedType = context.contentResolver.getType(uri)
            try {
                val flags = Intent.FLAG_GRANT_READ_URI_PERMISSION or
                        Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION
                context.contentResolver.takePersistableUriPermission(uri, flags)
            } catch (e: Exception) {
                Log.e("REVIEW360", "URI permission error", e)
            }
        }
    }

    Column(
        Modifier.fillMaxSize().padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Button(
            onClick = { openDocument.launch(arrayOf("video/*", "image/*")) },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Выбрать фото или видео 360°")
        }

        Spacer(Modifier.height(16.dp))

        when {
            selectedUri == null -> Text("Выберите 360° видео или фото.")
            isVideoFile(context, selectedUri!!, pickedType) -> Video360Screen(uri = selectedUri!!)
            isImageFile(context, selectedUri!!, pickedType) -> Photo360Screen(uri = selectedUri!!)
            else -> Text("Формат файла не поддерживается.")
        }
    }
}

private fun isVideoFile(context: android.content.Context, uri: Uri, mime: String?): Boolean {
    val type = mime ?: context.contentResolver.getType(uri) ?: ""
    return type.startsWith("video/") || uri.toString().endsWith(".mp4")
}

private fun isImageFile(context: android.content.Context, uri: Uri, mime: String?): Boolean {
    val type = mime ?: context.contentResolver.getType(uri) ?: ""
    return type.startsWith("image/") || uri.toString().endsWith(".jpg")
}
