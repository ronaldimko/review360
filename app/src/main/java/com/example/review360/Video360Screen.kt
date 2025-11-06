package com.example.review360

import android.net.Uri
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.common.MediaItem
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.video.spherical.SphericalGLSurfaceView

@androidx.annotation.OptIn(androidx.media3.common.util.UnstableApi::class)
@Composable
fun Video360Screen(uri: Uri) {
    AndroidView(
        modifier = Modifier.fillMaxSize(),
        factory = { context ->
            val exoPlayer = ExoPlayer.Builder(context).build()
            val sphericalView = SphericalGLSurfaceView(context)

            exoPlayer.setVideoSurfaceView(sphericalView)
            exoPlayer.setMediaItem(MediaItem.fromUri(uri))
            exoPlayer.prepare()
            exoPlayer.play()

            sphericalView
        }
    )
}
