package com.example.trainium2

import android.net.Uri
import android.widget.VideoView
import android.widget.FrameLayout
import android.view.Gravity
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.EaseIn
import androidx.compose.animation.core.EaseOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import kotlinx.coroutines.delay

@Composable
fun SplashVideoScreen(onVideoFinished: () -> Unit) {
    val context = LocalContext.current
    var videoEnded by remember { mutableStateOf(false) }
    var fadeOut by remember { mutableStateOf(false) }
    var fadeIn by remember { mutableStateOf(false) }
    var showBranding by remember { mutableStateOf(false) }
    val bgColor = Color.White  // <-- FONDO BLANCO

    val alpha by animateFloatAsState(
        targetValue = when {
            fadeOut -> 0f
            fadeIn -> 1f
            else -> 0f
        },
        animationSpec = tween(durationMillis = if (fadeOut) 700 else 500, easing = if (fadeOut) EaseIn else EaseOut),
        label = "splashFade"
    )

    val brandingAlpha by animateFloatAsState(
        targetValue = if (showBranding) 1f else 0f,
        animationSpec = tween(durationMillis = 800, easing = EaseOut),
        label = "brandingFade"
    )

    LaunchedEffect(Unit) {
        delay(80)
        fadeIn = true
        delay(2000)
        showBranding = true
        delay(13000)
        if (!videoEnded) onVideoFinished()
    }

    LaunchedEffect(videoEnded) {
        if (videoEnded) {
            showBranding = true
            delay(600)
            fadeOut = true
            delay(800)
            onVideoFinished()
        }
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
                    setBackgroundColor(android.graphics.Color.WHITE)
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

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(160.dp)
                .align(Alignment.BottomCenter)
                .alpha(brandingAlpha)
                .background(
                    Brush.verticalGradient(
                        listOf(Color.Transparent, Color.White.copy(alpha = 0.75f))
                    )
                )
        )

        CircularProgressIndicator(
            color = Color.Black,
            strokeWidth = 4.dp,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 48.dp)
                .alpha(brandingAlpha)
        )
    }
}