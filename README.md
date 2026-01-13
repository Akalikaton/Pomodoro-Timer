# Pomodoro Timer 

**Language: [English](README.md) | [中文](README_cn.md)**

A Pomodoro timer app designed for efficient work, featuring a minimalist UI to help users better manage focus and break time.  


## ✨ Features  

- **Minimalist Interface**: Clean UI with minimal distractions, high observability, and ergonomically sized buttons.  

- **Smart Timing**: Standard cycle of 25-minute focus followed by 5-minute rest.  

- **Visual Feedback**: Different states are indicated by distinct background colors for intuitive status recognition.  

- **Progress Bar**: Background color gradient as visual progress indicator
  
- **Data Tracking**: Real-time display of cumulative focus and rest durations.  

- **Intelligent Alerts**: Dual reminders via vibration and screen flashing.  

- **Deep Focus Mode**: Alerts vibrate only twice, then switch to silent color-coded state.

- **Full-Screen Control**: Tap anywhere on screen as main interaction button

## 📱 Functional Description  

### Focus Countdown (25 min, green background)  

- 25-minute focus countdown with green background indicating active focus mode.  

- Large font displays remaining time for easy viewing from a distance.

- Progress Bar: Background color gradient as visual progress indicator

![Screenshot_20260113_121735_com example pomodoro](https://github.com/user-attachments/assets/5b94ae22-7e4f-4f92-a318-d3ba9a8ab9f2)

### Focus Paused (orange background)  

- Background turns orange when paused.  

- Displays cumulative focus and rest time statistics.  

- Pause duration is counted toward total rest time.

![Screenshot_20260113_121730_com example pomodoro](https://github.com/user-attachments/assets/b4c019f0-9ad4-4339-ac24-960e29e38287)

### Focus Completed (yellow background – awaiting rest)  

- Screen flashes twice and vibrates upon completion of focus session.  

- Enters forward-counting mode with yellow background.  

- Waits for user action to begin rest period.  

![Screenshot_20260113_121850_com example pomodoro](https://github.com/user-attachments/assets/62ee229e-106a-4557-a9e2-b8f122af7a06)

### Rest Countdown (5 min, orange background)  

- 5-minute rest countdown.  

- Allows viewing cumulative focus/rest stats without interrupting the countdown.  

- Rest countdown cannot be paused.

- Progress Bar: Background color gradient as visual progress indicator

![Screenshot_20260113_121856_com example pomodoro](https://github.com/user-attachments/assets/6d43152c-9e6f-46f1-a0c6-c759b43dfd5d)

### Rest View Stats (orange background)  

- Background turns orange when break.  

- Displays cumulative focus and rest time statistics.  

- View Stats doesn't stop rest countdown.

![Screenshot_20260113_121859_com example pomodoro](https://github.com/user-attachments/assets/40869d66-de9d-4a92-a340-b8bb52fe6f2d)

### Rest Completed (yellow background – awaiting focus)  

- Vibrates upon completion of rest period.  

- Enters waiting state, ready to start next focus session.  

- Time in this state is counted toward total rest duration.  

![Screenshot_20260113_121905_com example pomodoro](https://github.com/user-attachments/assets/e2912bda-eeef-4cdc-8745-f78ca237f2e3)

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

### Force Stop

- Force stop Focus or Break time, change to another

### Better Reminder

- More time when Pause time, maybe every 1min or too push

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
