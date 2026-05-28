package com.example.trainium2

import android.net.Uri
import android.widget.VideoView
import android.widget.FrameLayout
import android.view.Gravity
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import kotlinx.coroutines.delay

@Composable
fun SplashVideoScreen(onVideoFinished: () -> Unit) {
    val context = LocalContext.current
    var videoEnded by remember { mutableStateOf(false) }
    var fadeOut by remember { mutableStateOf(false) }
    val bgColor = Color.White // Siempre blanco independientemente del tema

    val alpha by animateFloatAsState(
        targetValue = if (fadeOut) 0f else 1f,
        animationSpec = tween(durationMillis = 600),
        label = "splashFade"
    )

    LaunchedEffect(videoEnded) {
        if (videoEnded) {
            fadeOut = true
            delay(700)
            onVideoFinished()
        }
    }

    LaunchedEffect(Unit) {
        delay(15000)
        if (!videoEnded) onVideoFinished()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(bgColor)
            .alpha(alpha),
        contentAlignment = Alignment.Center
    ) {
        AndroidView(
            factory = { ctx ->
                val frame = FrameLayout(ctx).apply {
                    setBackgroundColor(android.graphics.Color.TRANSPARENT)
                }

                val videoView = VideoView(ctx)
                val layoutParams = FrameLayout.LayoutParams(
                    FrameLayout.LayoutParams.MATCH_PARENT,
                    FrameLayout.LayoutParams.MATCH_PARENT
                ).apply {
                    gravity = Gravity.CENTER
                }
                frame.addView(videoView, layoutParams)

                val videoUri = Uri.parse("android.resource://${ctx.packageName}/${R.raw.video_app}")
                videoView.setVideoURI(videoUri)

                videoView.setOnPreparedListener { mp ->
                    mp.isLooping = false
                    val videoWidth = mp.videoWidth
                    val videoHeight = mp.videoHeight
                    if (videoWidth > 0 && videoHeight > 0) {
                        val screenWidth = frame.width
                        val screenHeight = frame.height
                        if (screenWidth > 0 && screenHeight > 0) {
                            val videoRatio = videoWidth.toFloat() / videoHeight.toFloat()
                            val screenRatio = screenWidth.toFloat() / screenHeight.toFloat()

                            val lp = videoView.layoutParams
                            if (videoRatio > screenRatio) {
                                lp.width = screenWidth
                                lp.height = (screenWidth / videoRatio).toInt()
                            } else {
                                lp.height = screenHeight
                                lp.width = (screenHeight * videoRatio).toInt()
                            }
                            videoView.layoutParams = lp
                        }
                    }
                    mp.start()
                }

                videoView.setOnCompletionListener { videoEnded = true }
                videoView.setOnErrorListener { _, _, _ -> videoEnded = true; true }

                frame
            },
            modifier = Modifier.fillMaxSize()
        )
    }
}
