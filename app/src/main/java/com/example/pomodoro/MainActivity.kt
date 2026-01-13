package com.example.pomodoro

import android.content.Context
import android.graphics.drawable.ClipDrawable
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.LayerDrawable
import android.os.*
import android.view.Gravity
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
    companion object {
        private const val DEBUG_MODE = false
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

    // 正式时间 (如果需要调试，可自行修改这里)
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

        // 【需求1】全屏点击触发主按钮逻辑
        // 使用 performClick 可以同时触发按钮的视觉效果（如水波纹）
        rootLayout.setOnClickListener {
            if (actionBtn.visibility == View.VISIBLE) {
                actionBtn.performClick()
            }
        }

        actionBtn.setOnClickListener { handleActionClick() }
        stopBtn.setOnClickListener { resetToIdle() }

        if (DEBUG_MODE) {
            setupDebugScenario()
        } else {
            updateInterfaceForState()
        }
    }

    private fun setupDebugScenario() {
        // 调试代码保持不变...
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
                getPreferences(MODE_PRIVATE).edit {
                    putLong("FOCUS_TOTAL", 100 * 60 * 1000L)
                    putLong("REST_TOTAL", 20 * 60 * 1000L)
                }
                timeRemainingInMillis = (16 * 60 + 20) * 1000L
                updateInterfaceForState()
            }
        }
    }

    private fun handleActionClick() {
        if(DEBUG_MODE) return

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
        updateInterfaceForState() // 先更新界面，初始化背景图层
        startCountDown(duration, isFocus = true)
    }

    private fun startRestTimer(duration: Long) {
        currentState = State.REST_RUNNING
        updateInterfaceForState() // 先更新界面，初始化背景图层
        startCountDown(duration, isFocus = false)
    }

    private fun startCountDown(duration: Long, isFocus: Boolean) {
        countDownTimer?.cancel()

        // 记录总时长，用于计算百分比
        val totalDurationForProgress = if (isFocus) focusTime else restTime

        countDownTimer = object : CountDownTimer(duration, 50) { // 提高刷新率到50ms以获得更平滑的进度条动画
            override fun onTick(millisUntilFinished: Long) {
                timeRemainingInMillis = millisUntilFinished

                // 只有整秒时才更新文字，避免频繁刷新TextView
                if (millisUntilFinished % 1000 < 100) {
                    updateTimerDisplay(millisUntilFinished / 1000 + 1) // +1 是为了视觉上不显示0秒
                }

                // 【需求2】实时更新背景进度条
                updateProgressBackground(millisUntilFinished, totalDurationForProgress, isFocus)
            }

            override fun onFinish() {
                timeRemainingInMillis = 0
                updateTimerDisplay(0)
                updateProgressBackground(0, totalDurationForProgress, isFocus) // 确保最后变为纯色

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

    /**
     * 【需求2核心逻辑】更新背景进度条
     * 原理：使用 LayerDrawable，底层为目标色，顶层为当前色（ClipDrawable）。
     * ClipDrawable 根据 Gravity.START (左侧) 和 level (0-10000) 进行裁剪。
     */
    private fun updateProgressBackground(remaining: Long, total: Long, isFocus: Boolean) {
        // 计算进度 (0 到 10000)
        val progress = ((remaining.toDouble() / total.toDouble()) * 10000).toInt()

        val backgroundDrawable = rootLayout.background as? LayerDrawable ?: return

        // 获取顶层颜色 (index 1)，它是一个 ClipDrawable
        val clipDrawable = backgroundDrawable.getDrawable(1) as? ClipDrawable

        // 设置裁剪级别：
        // 满级(10000) = 整个屏幕是顶层颜色
        // 0 = 顶层颜色完全消失，显示底层颜色
        // 因为是从右向左变，即右边的颜色（底层色）逐渐露出来，左边的颜色（顶层色）逐渐收缩
        clipDrawable?.level = progress
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
                actionBtn.text = "Start"
                updateTimerDisplay(focusTime / 1000)
            }
            State.FOCUS_RUNNING -> {
                actionBtn.text = "Break"
                // 初始化专注倒计时的背景：底层橙色，顶层绿色
                initProgressDrawable(bgColor = colorRestOrange, fgColor = colorFocusGreen)
            }
            State.FOCUS_PAUSED -> {
                rootLayout.setBackgroundColor(colorRestOrange) // 暂停时显示纯色
                actionBtn.text = "Resume"
                stopBtn.visibility = View.VISIBLE
                timerText.visibility = View.GONE
                statsText.visibility = View.VISIBLE
                updateStatsText()
            }
            State.WAITING_FOR_REST -> {
                rootLayout.setBackgroundColor(colorWaitYellow)
                actionBtn.text = "Rest Start"
            }
            State.REST_RUNNING -> {
                actionBtn.text = "View Stats"
                // 初始化休息倒计时的背景：底层绿色，顶层橙色
                initProgressDrawable(bgColor = colorFocusGreen, fgColor = colorRestOrange)
            }
            State.REST_SHOWING_STATS -> {
                rootLayout.setBackgroundColor(colorRestOrange) // 查看统计保持橙色
                actionBtn.text = "Back"
                stopBtn.visibility = View.VISIBLE
                timerText.visibility = View.GONE
                statsText.visibility = View.VISIBLE
                updateStatsText()
            }
            State.WAITING_FOR_FOCUS -> {
                rootLayout.setBackgroundColor(colorWaitYellow)
                actionBtn.text = "Continue Focus"
            }
        }
    }

    /**
     * 创建 LayerDrawable 用于进度条显示
     * @param bgColor 进度条走完后显示的颜色（底层，右侧显露出的颜色）
     * @param fgColor 当前的进度条颜色（顶层，被裁剪的颜色）
     */
    private fun initProgressDrawable(bgColor: Int, fgColor: Int) {
        val bottomLayer = ColorDrawable(bgColor)
        val topLayer = ClipDrawable(ColorDrawable(fgColor), Gravity.START, ClipDrawable.HORIZONTAL)

        // 设置初始 level 为 10000 (全屏显示 topLayer)
        topLayer.level = 10000

        val layerDrawable = LayerDrawable(arrayOf(bottomLayer, topLayer))
        // 必须设置 id 方便后续获取，虽然这里简单只用 index 也可以
        layerDrawable.setId(0, android.R.id.background)
        layerDrawable.setId(1, android.R.id.progress)

        rootLayout.background = layerDrawable
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

        if (currentState == State.FOCUS_PAUSED) {
            val elapsedFocusTime = focusTime - timeRemainingInMillis
            focusTotal += elapsedFocusTime
        }

        statsText.text = "Today's total focus time: \n${formatDuration(focusTotal)}\n\nToday's total break time:\n ${formatDuration(restTotal)}"
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