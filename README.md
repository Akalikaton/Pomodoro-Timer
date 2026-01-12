# Pomodoro Timer 
A Pomodoro timer app designed for efficient work, featuring a minimalist UI to help users better manage focus and break time.  

## ✨ Features  

- **Minimalist Interface**: Clean UI with minimal distractions, high observability, and ergonomically sized buttons.  

- **Smart Timing**: Standard cycle of 25-minute focus followed by 5-minute rest.  

- **Visual Feedback**: Different states are indicated by distinct background colors for intuitive status recognition.  

- **Data Tracking**: Real-time display of cumulative focus and rest durations.  

- **Intelligent Alerts**: Dual reminders via vibration and screen flashing.  

- **Deep Focus Mode**: Alerts vibrate only twice, then switch to silent color-coded state.  

## 📱 Functional Description  

### Focus Countdown (25 min, green background)  

- 25-minute focus countdown with green background indicating active focus mode.  

- Large font displays remaining time for easy viewing from a distance.  

![专注倒计时](https://github.com/user-attachments/assets/02fc910b-c822-4062-822d-f4e7d5946199)

### Focus Paused (orange background)  

- Background turns orange when paused.  

- Displays cumulative focus and rest time statistics.  

- Pause duration is counted toward total rest time.  

![Focus Paused](https://github.com/user-attachments/pomodoro/assets/4b603c5c-8778-4e72-a8a6-c2eb7eccb08e)  

### Focus Completed (yellow background – awaiting rest)  

- Screen flashes twice and vibrates upon completion of focus session.  

- Enters forward-counting mode with yellow background.  

- Waits for user action to begin rest period.  

![Focus Completed](https://github.com/user-attachments/pomodoro/assets/b114b510-909d-44bd-8fe3-df0fe4cb02c6)  

### Rest Countdown (5 min, orange background)  

- 5-minute rest countdown.  

- Allows viewing cumulative focus/rest stats without interrupting the countdown.  

- Rest countdown cannot be paused.  

![Rest Countdown](https://github.com/user-attachments/pomodoro/assets/541355a3-38c8-4188-b3ed-270f940fbc68)  

### Rest Completed (yellow background – awaiting focus)  

- Vibrates upon completion of rest period.  

- Enters waiting state, ready to start next focus session.  

- Time in this state is counted toward total rest duration.  

![Rest Completed](https://github.com/user-attachments/pomodoro/assets/2ec7b902-6ee6-4fd8-b8be-7023527f9d08)  

### Smart Date Handling  

- To accommodate overnight work sessions, each day’s timing starts at 5:00 AM.  

## 🔮 Future Feature Roadmap  

### Dynamic Time During Pause  

- While paused, cumulative rest time updates dynamically in real time.  

### 📊 History Log  

- Store daily work and rest durations.  

- Display history chart on main page.  

- Dual-color bar chart: green (work time) at bottom, orange (rest time) on top.  

- Enables intuitive comparison of work vs. total time trends.  

### 🔄 Adaptive Rest Duration  

- Implement a long-break mechanism every 2 hours.  

- Dynamically adjust rest length based on total rest time in the past 2 hours.  

- If rest time ≥ threshold (e.g., 30 min), use 5-min short break; otherwise, trigger 25-min long break.  

### ⚙️ Adjustable Focus Duration  

- Scroll wheel on main page to adjust focus duration.  

- Supports customizing the default 25-minute focus session.  

### 🎨 Progress Visualization  

- Entire background acts as a progress bar.  

- Color gradually transitions from left to right to reflect time progression.  

### 📱 UI Optimization  

- Research custom layout designs tailored to personal workflow needs.  

- Add portrait-mode support.  

- Support orientation adaptation based on gravity sensor (charging port on left or right).  

### 🌍 Multi-language Support  

- README documentation and UI text support Chinese/English toggle.  

### 🔒 Focus Lock Mode  

- Enable forced focus mode.  

- When active, device locks during focus: no notifications, no app switching.  

- Alternatively, app switching counts as rest time or increments failure count.  

## 🛠 Tech Stack  

- **Development Language**: Kotlin  

- **UI Framework**: Android XML + ConstraintLayout  

- **Timer Engine**: CountDownTimer  

- **Data Persistence**: SharedPreferences  

## 🚀 Installation & Usage  

1. Download the APK file.  

2. Install on Android device.  

3. Grant vibration permission.  

4. Start focused, efficient work!  
