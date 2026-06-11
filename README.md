# 充电状态检测器

在通知栏实时显示充电状态，通过电流数据判断（阈值 90mA）。

- 🟢 绿色 = 正在充电（电流 > 90mA）
- 🔴 红色 = 未充电（电流 ≤ 90mA）

## 三种编译方式（任选一种）

### 方式一：本地一键脚本（推荐，最简单）

**Windows**：
```bash
# 双击运行
build.bat
```

**macOS / Linux**：
```bash
chmod +x build.sh
./build.sh
```

脚本会自动：
- 检查 Java 和 Android SDK
- 自动查找已安装的 SDK 路径
- 安装缺失的 SDK 组件
- 编译并输出 APK 路径

---

### 方式二：GitHub Actions 自动编译（无需本地环境）

**不需要在电脑上安装任何开发工具**，用 GitHub 云端免费编译：

1. **注册/登录** [GitHub](https://github.com)
2. 点击右上角 **+** → **New repository**，创建一个新仓库（如 `ChargingMonitor`）
3. 把本项目代码上传到这个仓库（拖拽 ZIP 解压后的文件，或用 Git）
4. 进入仓库 → **Actions** 标签页
5. 找到 **Build APK** 工作流，点击 **Run workflow**
6. 等待约 3~5 分钟编译完成
7. 进入最新的一次运行记录 → **Artifacts** → 下载 `ChargingMonitor-APK`
8. 解压下载的文件，得到 `app-debug.apk`，传到手机安装

---

### 方式三：命令行手动编译

```bash
# 确保已配置环境变量：JAVA_HOME 和 ANDROID_SDK_ROOT

# 编译
./gradlew assembleDebug    # macOS/Linux
gradlew.bat assembleDebug  # Windows

# 输出路径：app/build/outputs/apk/debug/app-debug.apk
```

---

## 安装到手机

```bash
# 连接手机，开启 USB 调试
adb install app/build/outputs/apk/debug/app-debug.apk
```

或把 APK 传到手机，直接点击安装。

---

## 使用说明

1. 打开 APP，点击 **「开始监控」**
2. 下拉通知栏，看到常驻通知
3. 充电时通知变绿色，断开充电变红色
4. 点击通知可回到 APP

---

## 常见问题

| 问题 | 解决 |
|------|------|
| 脚本提示找不到 Java | 安装 JDK 17：https://adoptium.net |
| 脚本提示找不到 SDK | 先安装 Android Studio 打开一次，或手动设置 `ANDROID_SDK_ROOT` |
| 编译失败 | 检查网络，首次编译需下载依赖 |
| 通知不显示 | 检查手机通知权限是否允许 |
| 后台被杀 | 在手机设置中关闭本应用的电池优化 |
