#!/bin/bash
set -e

echo "=== ChargingMonitor 一键编译脚本 (macOS/Linux) ==="
echo ""

# 检查 Java
if ! command -v java &> /dev/null; then
    echo "❌ 错误：未找到 Java，请先安装 JDK 17"
    echo "下载地址：https://adoptium.net/zh-CN/temurin/releases/?version=17"
    exit 1
fi

JAVA_VER=$(java -version 2>&1 | head -n 1 | cut -d'"' -f2)
echo "✅ Java 已安装：$JAVA_VER"

# 检查 Android SDK
if [ -z "$ANDROID_SDK_ROOT" ] && [ -z "$ANDROID_HOME" ]; then
    echo "⚠️ 未设置 ANDROID_SDK_ROOT，尝试自动查找..."

    POSSIBLE_PATHS=(
        "$HOME/Android/Sdk"
        "$HOME/Library/Android/sdk"
        "/Users/$USER/Library/Android/sdk"
        "$HOME/AppData/Local/Android/Sdk"
    )

    FOUND=""
    for path in "${POSSIBLE_PATHS[@]}"; do
        if [ -d "$path" ]; then
            export ANDROID_SDK_ROOT="$path"
            FOUND="$path"
            break
        fi
    done

    if [ -z "$FOUND" ]; then
        echo "❌ 未找到 Android SDK"
        echo "请安装 Android Studio 或手动设置 ANDROID_SDK_ROOT 环境变量"
        exit 1
    else
        echo "✅ 自动找到 SDK：$FOUND"
    fi
else
    if [ -z "$ANDROID_SDK_ROOT" ]; then
        export ANDROID_SDK_ROOT="$ANDROID_HOME"
    fi
    echo "✅ SDK 路径：$ANDROID_SDK_ROOT"
fi

# 检查并安装 SDK 组件
SDK_MANAGER="$ANDROID_SDK_ROOT/cmdline-tools/latest/bin/sdkmanager"
if [ ! -f "$SDK_MANAGER" ]; then
    SDK_MANAGER="$ANDROID_SDK_ROOT/cmdline-tools/bin/sdkmanager"
fi

if [ -f "$SDK_MANAGER" ]; then
    echo ""
    echo "=== 检查 SDK 组件 ==="
    if ! $SDK_MANAGER --list 2>/dev/null | grep -q "platforms;android-34"; then
        echo "正在安装 Android 34 平台..."
        yes | $SDK_MANAGER --sdk_root="$ANDROID_SDK_ROOT" "platforms;android-34" "build-tools;34.0.0" "platform-tools" > /dev/null 2>&1
    else
        echo "✅ SDK 组件已就绪"
    fi
fi

# 编译
echo ""
echo "=== 开始编译 ==="
chmod +x gradlew
./gradlew assembleDebug

echo ""
echo "✅ 编译成功！"
echo "APK 路径：app/build/outputs/apk/debug/app-debug.apk"
echo ""
echo "安装到手机："
echo "  1. 连接手机并开启 USB 调试"
echo "  2. 运行：$ANDROID_SDK_ROOT/platform-tools/adb install app/build/outputs/apk/debug/app-debug.apk"
echo ""
