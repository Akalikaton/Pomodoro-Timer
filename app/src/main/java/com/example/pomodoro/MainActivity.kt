package com.example.pomodoro

import android.content.Context
import android.os.*
import android.view.View
import android.view.WindowInsets
import android.view.WindowInsetsController
import android.view.animation.AlphaAnimation
import android.view.animation.Animation
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.edit
import androidx.core.graphics.toColorInt
import java.util.*

class MainActivity : AppCompatActivity() {

    // --- 调试开关 ---
    // 将 DEBUG_MODE 设置为 true 来启用调试场景
    companion object {
        private const val DEBUG_MODE = false // 改为 true 开启调试
        // 1: 专注16:20 | 2: 等待休息7:12 | 3: 休息3:52 | 4: 统计界面
        private const val DEBUG_SCENARIO = 1
    }

    enum class State {
        IDLE, FOCUS_RUNNING, FOCUS_PAUSED, WAITING_FOR_REST, REST_RUNNING, REST_SHOWING_STATS, WAITING_FOR_FOCUS
    }

    private var currentState = State.IDLE
    private lateinit var rootLayout: ConstraintLayout
    private lateinit var timerText: TextView
    private lateinit var statsText: TextView
    private lateinit var actionBtn: Button
    private lateinit var stopBtn: Button

    private var countDownTimer: CountDownTimer? = null
    private var upTimerHandler = Handler(Looper.getMainLooper())
    private var upTimerRunnable: Runnable? = null

    private var timeRemainingInMillis: Long = 0
    private var waitTimeSeconds: Int = 0

    private val focusTime = 25 * 60 * 1000L
    private val restTime = 5 * 60 * 1000L

    private val colorFocusGreen = "#4CAF50".toColorInt()
    private val colorRestOrange = "#FF9800".toColorInt()
    private val colorWaitYellow = "#FBC02D".toColorInt()

    private var lastTimestamp: Long = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestedOrientation = android.content.pm.ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
        setContentView(R.layout.activity_main)
        hideSystemUI()

        rootLayout = findViewById(R.id.rootLayout)
        timerText = findViewById(R.id.timerText)
        statsText = findViewById(R.id.statsText)
        actionBtn = findViewById(R.id.actionBtn)
        stopBtn = findViewById(R.id.stopBtn)

        actionBtn.setOnClickListener { handleActionClick() }
        stopBtn.setOnClickListener { resetToIdle() }

        if (DEBUG_MODE) {
            setupDebugScenario()
        } else {
            updateInterfaceForState()
        }
    }

    private fun setupDebugScenario() {
        when (DEBUG_SCENARIO) {
            1 -> {
                currentState = State.FOCUS_RUNNING
                updateTimerDisplay((16 * 60 + 20).toLong())
                updateInterfaceForState()
            }
            2 -> {
                currentState = State.WAITING_FOR_REST
                updateTimerDisplay((7 * 60 + 12).toLong())
                updateInterfaceForState()
            }
            3 -> {
                currentState = State.REST_RUNNING
                updateTimerDisplay((3 * 60 + 52).toLong())
                updateInterfaceForState()
            }
            4 -> {
                currentState = State.FOCUS_PAUSED
                // 伪造一些数据用于显示
                getPreferences(MODE_PRIVATE).edit {
                    putLong("FOCUS_TOTAL", 100 * 60 * 1000L) // 100分钟
                    putLong("REST_TOTAL", 20 * 60 * 1000L)  // 20分钟
                }
                timeRemainingInMillis = (16 * 60 + 20) * 1000L // 模拟从专注16:20时暂停
                updateInterfaceForState()
            }
        }
    }

    private fun handleActionClick() {
        if(DEBUG_MODE) return // 调试模式下禁用点击

        stopFlashing()
        val now = System.currentTimeMillis()

        when (currentState) {
            State.IDLE -> startFocusTimer(focusTime)
            State.FOCUS_RUNNING -> {
                countDownTimer?.cancel()
                currentState = State.FOCUS_PAUSED
                lastTimestamp = now
                updateInterfaceForState()
            }
            State.FOCUS_PAUSED -> {
                addTime(isFocus = false, duration = now - lastTimestamp)
                startFocusTimer(timeRemainingInMillis)
            }
            State.WAITING_FOR_REST -> {
                stopUpTimer()
                addTime(isFocus = true, duration = now - lastTimestamp)
                startRestTimer(restTime)
            }
            State.REST_RUNNING -> {
                currentState = State.REST_SHOWING_STATS
                updateInterfaceForState()
            }
            State.REST_SHOWING_STATS -> {
                currentState = State.REST_RUNNING
                updateInterfaceForState()
            }
            State.WAITING_FOR_FOCUS -> {
                stopUpTimer()
                addTime(isFocus = false, duration = now - lastTimestamp)
                startFocusTimer(focusTime)
            }
        }
    }

    private fun startFocusTimer(duration: Long) {
        currentState = State.FOCUS_RUNNING
        updateInterfaceForState()
        startCountDown(duration)
    }

    private fun startRestTimer(duration: Long) {
        currentState = State.REST_RUNNING
        updateInterfaceForState()
        startCountDown(duration)
    }

    private fun startCountDown(duration: Long) {
        countDownTimer?.cancel()
        countDownTimer = object : CountDownTimer(duration, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                timeRemainingInMillis = millisUntilFinished
                updateTimerDisplay(millisUntilFinished / 1000)
            }
            override fun onFinish() {
                triggerVibrateAndFlash()
                lastTimestamp = System.currentTimeMillis()
                if (currentState == State.FOCUS_RUNNING) {
                    addTime(isFocus = true, duration = focusTime)
                    currentState = State.WAITING_FOR_REST
                } else {
                    addTime(isFocus = false, duration = restTime)
                    currentState = State.WAITING_FOR_FOCUS
                }
                updateInterfaceForState()
                startUpTimer()
            }
        }.start()
    }

    private fun startUpTimer() {
        waitTimeSeconds = 0
        upTimerRunnable = object : Runnable {
            override fun run() {
                waitTimeSeconds++
                updateTimerDisplay(waitTimeSeconds.toLong())
                upTimerHandler.postDelayed(this, 1000)
            }
        }
        upTimerHandler.post(upTimerRunnable!!)
    }

    private fun stopUpTimer() {
        upTimerRunnable?.let { upTimerHandler.removeCallbacks(it) }
    }

    private fun updateInterfaceForState() {
        stopBtn.visibility = View.GONE
        timerText.visibility = View.VISIBLE
        statsText.visibility = View.GONE

        when (currentState) {
            State.IDLE -> {
                rootLayout.setBackgroundColor(colorFocusGreen)
                actionBtn.text = "开始"
                updateTimerDisplay(focusTime / 1000)
            }
            State.FOCUS_RUNNING -> {
                rootLayout.setBackgroundColor(colorFocusGreen)
                actionBtn.text = "暂停"
            }
            State.FOCUS_PAUSED -> {
                rootLayout.setBackgroundColor(colorRestOrange)
                actionBtn.text = "继续"
                stopBtn.visibility = View.VISIBLE
                timerText.visibility = View.GONE
                statsText.visibility = View.VISIBLE
                updateStatsText()
            }
            State.WAITING_FOR_REST -> {
                rootLayout.setBackgroundColor(colorWaitYellow)
                actionBtn.text = "开始休息"
            }
            State.REST_RUNNING -> {
                rootLayout.setBackgroundColor(colorRestOrange)
                actionBtn.text = "统计"
            }
            State.REST_SHOWING_STATS -> {
                rootLayout.setBackgroundColor(colorRestOrange)
                actionBtn.text = "返回"
                stopBtn.visibility = View.VISIBLE
                timerText.visibility = View.GONE
                statsText.visibility = View.VISIBLE
                updateStatsText()
            }
            State.WAITING_FOR_FOCUS -> {
                rootLayout.setBackgroundColor(colorWaitYellow)
                actionBtn.text = "继续工作"
            }
        }
    }

    private fun resetToIdle() {
        if(DEBUG_MODE) return
        countDownTimer?.cancel()
        stopUpTimer()
        currentState = State.IDLE
        updateInterfaceForState()
    }

    private fun updateTimerDisplay(secondsTotal: Long) {
        val minutes = secondsTotal / 60
        val seconds = secondsTotal % 60
        timerText.text = String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds)
    }

    private fun formatDuration(totalMillis: Long): String {
        if (totalMillis < 0) return "0s"
        val totalSeconds = totalMillis / 1000
        val h = totalSeconds / 3600
        val m = (totalSeconds % 3600) / 60
        val s = totalSeconds % 60

        return when {
            h > 0 -> "${h}h ${m}min ${s}s"
            m > 0 -> "${m}min ${s}s"
            else -> "${s}s"
        }
    }

    private fun updateStatsText() {
        val sharedPref = getPreferences(MODE_PRIVATE)
        var focusTotal = sharedPref.getLong("FOCUS_TOTAL", 0)
        val restTotal = sharedPref.getLong("REST_TOTAL", 0)

        // 如果是从专注状态暂停，将已专注的时间临时加入总数进行显示
        if (currentState == State.FOCUS_PAUSED) {
            val elapsedFocusTime = focusTime - timeRemainingInMillis
            focusTotal += elapsedFocusTime
        }

        statsText.text = "今日累计专注: ${formatDuration(focusTotal)}\n今日累计休息: ${formatDuration(restTotal)}"
    }

    private fun addTime(isFocus: Boolean, duration: Long) {
        val sharedPref = getPreferences(MODE_PRIVATE)
        val todayKey = getTodayKey()
        val lastSavedKey = sharedPref.getString("LAST_SAVED_DATE", "")

        if (lastSavedKey != todayKey) {
            sharedPref.edit {
                putLong("FOCUS_TOTAL", 0)
                putLong("REST_TOTAL", 0)
                putString("LAST_SAVED_DATE", todayKey)
            }
        }

        val key = if (isFocus) "FOCUS_TOTAL" else "REST_TOTAL"
        val current = sharedPref.getLong(key, 0)
        sharedPref.edit { putLong(key, current + duration) }
    }

    private fun getTodayKey(): String {
        val cal = Calendar.getInstance()
        if (cal.get(Calendar.HOUR_OF_DAY) < 5) cal.add(Calendar.DAY_OF_YEAR, -1)
        return "${cal.get(Calendar.YEAR)}-${cal.get(Calendar.MONTH)}-${cal.get(Calendar.DAY_OF_MONTH)}"
    }

    private fun triggerVibrateAndFlash() {
        val vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val vibratorManager = getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
            vibratorManager.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            getSystemService(VIBRATOR_SERVICE) as Vibrator
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator.vibrate(VibrationEffect.createWaveform(longArrayOf(0, 500, 200, 500), -1))
        } else {
            @Suppress("DEPRECATION")
            vibrator.vibrate(longArrayOf(0, 500, 200, 500), -1)
        }
        val anim = AlphaAnimation(0.3f, 1.0f)
        anim.duration = 300
        anim.repeatMode = Animation.REVERSE
        anim.repeatCount = 3
        rootLayout.startAnimation(anim)
    }

    private fun stopFlashing() = rootLayout.clearAnimation()

    private fun hideSystemUI() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            window.setDecorFitsSystemWindows(false)
            window.insetsController?.let {
                it.hide(WindowInsets.Type.statusBars() or WindowInsets.Type.navigationBars())
                it.systemBarsBehavior = WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
            }
        } else {
            @Suppress("DEPRECATION")
            window.decorView.systemUiVisibility = (View.SYSTEM_UI_FLAG_FULLSCREEN
                    or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                    or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY)
        }
    }
}
