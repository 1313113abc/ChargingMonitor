@echo off
chcp 65001 >nul
echo === ChargingMonitor 一键编译脚本 (Windows) ===
echo.

:: 检查 Java
java -version >nul 2>&1
if errorlevel 1 (
    echo ❌ 错误：未找到 Java，请先安装 JDK 17
    echo 下载地址：https://adoptium.net/zh-CN/temurin/releases/?version=17
    pause
    exit /b 1
)

echo ✅ Java 已安装

:: 检查 Android SDK
if "%ANDROID_SDK_ROOT%"=="" (
    if "%ANDROID_HOME%"=="" (
        echo ⚠️ 未设置 ANDROID_SDK_ROOT，尝试自动查找...

        if exist "%LOCALAPPDATA%\Android\Sdk" (
            set "ANDROID_SDK_ROOT=%LOCALAPPDATA%\Android\Sdk"
            echo ✅ 自动找到 SDK：%ANDROID_SDK_ROOT%
        ) else if exist "%USERPROFILE%\Android\Sdk" (
            set "ANDROID_SDK_ROOT=%USERPROFILE%\Android\Sdk"
            echo ✅ 自动找到 SDK：%ANDROID_SDK_ROOT%
        ) else (
            echo ❌ 未找到 Android SDK
            echo 请安装 Android Studio 或手动设置 ANDROID_SDK_ROOT 环境变量
            pause
            exit /b 1
        )
    ) else (
        set "ANDROID_SDK_ROOT=%ANDROID_HOME%"
    )
)

echo ✅ SDK 路径：%ANDROID_SDK_ROOT%

:: 检查并安装 SDK 组件
set "SDKMANAGER=%ANDROID_SDK_ROOT%\cmdline-tools\latestin\sdkmanager.bat"
if not exist "%SDKMANAGER%" (
    set "SDKMANAGER=%ANDROID_SDK_ROOT%\cmdline-toolsin\sdkmanager.bat"
)

if exist "%SDKMANAGER%" (
    echo === 检查 SDK 组件 ===
    call "%SDKMANAGER%" --list | findstr "platforms;android-34" >nul
    if errorlevel 1 (
        echo 正在安装 Android 34 平台...
        call "%SDKMANAGER%" --sdk_root="%ANDROID_SDK_ROOT%" "platforms;android-34" "build-tools;34.0.0" "platform-tools"
    ) else (
        echo ✅ SDK 组件已就绪
    )
)

:: 编译
echo.
echo === 开始编译 ===
call gradlew.bat assembleDebug

if errorlevel 1 (
    echo.
    echo ❌ 编译失败，请查看上方错误信息
    pause
    exit /b 1
)

echo.
echo ✅ 编译成功！
echo APK 路径：appuild\outputspk\debugpp-debug.apk
echo.
echo 安装到手机：
echo   1. 连接手机并开启 USB 调试
echo   2. 运行：%ANDROID_SDK_ROOT%\platform-toolsdb.exe install appuild\outputspk\debugpp-debug.apk
echo.
pause
