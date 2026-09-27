package com.example.stylussound

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.media.AudioAttributes
import android.media.SoundPool
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import kotlin.math.sqrt

class StylusSoundService : Service() {

    private lateinit var soundPool: SoundPool
    private var penDownSoundId = 0
    private var penMoveSoundId = 0
    private var lastX = 0f
    private var lastY = 0f
    private var lastPlayTime = 0L

    private var baseRate = 1.0f
    private val maxRate = 2.0f
    private val minRate = 0.5f

    override fun onCreate() {
        super.onCreate()
        initSoundPool()
        startForegroundNotification()
    }

    private fun initSoundPool() {
        val audioAttributes = AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_ASSISTANCE_SONIFICATION)
            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
            .build()

        soundPool = SoundPool.Builder()
            .setMaxStreams(10)
            .setAudioAttributes(audioAttributes)
            .build()

        penDownSoundId = soundPool.load(this, R.raw.pen_down, 1)
        penMoveSoundId = soundPool.load(this, R.raw.pen_move, 1)
    }

    private fun startForegroundNotification() {
        val channelId = "stylus_sound_channel"
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "触控笔音效",
                NotificationManager.IMPORTANCE_LOW
            )
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }

        val notification: Notification = NotificationCompat.Builder(this, channelId)
            .setContentTitle("触控笔音效已开启")
            .setContentText("正在监听书写动作...")
            .setSmallIcon(android.R.drawable.ic_media_play)
            .setOngoing(true)
            .build()

        startForeground(1, notification)
    }

    fun handleStylusEvent(event: android.view.MotionEvent) {
        val action = event.actionMasked
        val x = event.x
        val y = event.y

        when (action) {
            android.view.MotionEvent.ACTION_DOWN -> {
                lastX = x
                lastY = y
                playSound(penDownSoundId, 1.0f)
            }
            android.view.MotionEvent.ACTION_MOVE -> {
                val dx = x - lastX
                val dy = y - lastY
                val distance = sqrt(dx * dx + dy * dy)
                val currentTime = System.currentTimeMillis()
                val timeDelta = currentTime - lastPlayTime

                if (distance > 10f && timeDelta > 30) {
                    val speed = distance / (timeDelta / 1000f)
                    val rate = (baseRate + (speed / 100f)).coerceIn(minRate, maxRate)
                    playSound(penMoveSoundId, rate)
                    lastPlayTime = currentTime
                }
                lastX = x
                lastY = y
            }
        }
    }

    private fun playSound(soundId: Int, rate: Float) {
        soundPool.play(
            soundId,
            1.0f,
            1.0f,
            1,
            0,
            rate
        )
    }

    override fun onDestroy() {
        soundPool.release()
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null
}
