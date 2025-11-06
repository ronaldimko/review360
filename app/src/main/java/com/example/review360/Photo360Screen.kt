package com.example.review360

import android.graphics.Bitmap
import android.net.Uri
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@Composable
fun Photo360Screen(uri: Uri) {
    val context = LocalContext.current
    var bitmap by remember(uri) { mutableStateOf<Bitmap?>(null) }
    var error by remember(uri) { mutableStateOf<String?>(null) }

    LaunchedEffect(uri) {
        try {
            bitmap = withContext(Dispatchers.IO) {
                decodeBitmapSampledFromUri(
                    context = context,
                    uri = uri,
                    reqMaxWidth = 4096,
                    reqMaxHeight = 2048
                )
            }
        } catch (t: Throwable) {
            error = t.message ?: "Ошибка загрузки изображения"
        }
    }

    when {
        bitmap != null -> AndroidView(
            modifier = Modifier.fillMaxSize(),
            factory = { ctx -> SimplePanoramaView(ctx).apply { this.bitmap = bitmap } },
            update = { it.bitmap = bitmap }
        )
        error != null -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("Не удалось открыть фото 360°:\n$error")
        }
        else -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
    }
}
