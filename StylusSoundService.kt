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

    // 用于调整音调
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
            .setMaxStreams(10) // 允许同时播放多个音效
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

    /**
     * 处理触控笔事件，由 OverlayView 或 AccessibilityService 调用
     */
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

                // 每移动一定距离或间隔一段时间播放一次
                if (distance > 10f && timeDelta > 30) {
                    // 根据速度调整音调
                    val speed = distance / (timeDelta / 1000f)
                    val rate = (baseRate + (speed / 100f)).coerceIn(minRate, maxRate)
                    playSound(penMoveSoundId, rate)
                    lastPlayTime = currentTime
                }
                lastX = x
                lastY = y
            }
            android.view.MotionEvent.ACTION_UP,
            android.view.MotionEvent.ACTION_CANCEL -> {
                // 可以在这里播放抬笔音效
            }
        }
    }

    private fun playSound(soundId: Int, rate: Float) {
        soundPool.play(
            soundId,
            1.0f, // 左音量
            1.0f, // 右音量
            1,    // 优先级
            0,    // 循环次数
            rate  // 播放速率，用于调整音调
        )
    }

    override fun onDestroy() {
        soundPool.release()
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null
}