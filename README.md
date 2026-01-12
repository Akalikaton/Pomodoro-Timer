创作一个符合自己使用的番茄钟

# 特点
UI界面简洁干扰少，可观测性强，按钮适合操作

# 功能描述
25min专注倒计时，绿色背景
![Screenshot_20260112_222019_com example pomodoro](https://github.com/user-attachments/assets/02fc910b-c822-4062-822d-f4e7d5946199)

专注时暂停，背景变成橙色，显示累计专注和累计休息信息，并且暂停时间计入休息时间
![Screenshot_20260112_222956_com example pomodoro](https://github.com/user-attachments/assets/4b603c5c-8778-4e72-a8a6-c2eb7eccb08e)

25min专注倒计时结束后，闪烁两下屏幕并震动提醒。进入可开始休息的正计时，背景变成黄色，时间计入专注时间，等待操作进入休息时间
![Screenshot_20260112_223004_com example pomodoro](https://github.com/user-attachments/assets/b114b510-909d-44bd-8fe3-df0fe4cb02c6)

5min休息倒计时，橙色背景，可操作显示累计专注和累计休息信息，并且不会暂停休息时间倒计时
![Screenshot_20260112_223007_com example pomodoro](https://github.com/user-attachments/assets/541355a3-38c8-4188-b3ed-270f940fbc68)

5min休息倒计时结束后，闪烁两下屏幕并震动提醒。进入可继续工作的正计时，背景变成黄色，时间计入休息时间，等待操作进入工作时间
![Screenshot_20260112_223014_com example pomodoro](https://github.com/user-attachments/assets/2ec7b902-6ee6-4fd8-b8be-7023527f9d08)

考虑到连夜工作，每日记时开端定为凌晨五点

# 后续功能

## 历史记录
储存每日工作时间和休息时间，可在主页面查看历史记录
暂定为双色条形图，绿色为工作时间在下，橙色为休息时间在上，可直观对比工作时长和总时长

## 动态休息时间
正常番茄钟是每2h一个25min大休息，可在每次点击开始休息按钮时，计算过去2h内的总休息时间是否超过阈值（如30min）。如果超过了，则进入正常5min休息时间，如果没超过，则认为需要进入大休息25min。
## 专注时间调节
主页面可滚轮调节25min的专注时间
