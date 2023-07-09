package com.example.ai_assistant

import android.accessibilityservice.AccessibilityService
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import android.os.Build
import android.util.Log
import android.provider.Settings
import android.widget.Toast
import io.flutter.embedding.android.FlutterActivity
import io.flutter.embedding.engine.FlutterEngine
import io.flutter.plugin.common.MethodChannel

class MainActivity : FlutterActivity(), AccessibilityServiceCallback {
    private val channelName = "samples.flutter.dev/assistant"
    private var methodChannel: MethodChannel? = null

    override fun onServiceConnected() {
        // 无障碍服务已开启回调
        Log.d("开关", "无障碍服务已开启")
        // 发送事件通知给 Flutter
        methodChannel?.invokeMethod("onAccessibilityServiceUpdate", true)
    }

    override fun onServiceDisconnected() {
        // 无障碍服务已关闭回调
        Log.d("开关", "无障碍服务已关闭")
        // 发送事件通知给 Flutter
        methodChannel?.invokeMethod("onAccessibilityServiceUpdate", false)
    }

    override fun configureFlutterEngine(flutterEngine: FlutterEngine) {
        super.configureFlutterEngine(flutterEngine)
        AccessibilitySampleService.setCallback(this)
        methodChannel = MethodChannel(flutterEngine.dartExecutor.binaryMessenger, channelName)
        // This method is invoked on the main thread.
        methodChannel!!.setMethodCallHandler { call, result ->
            if (call.method == "getBatteryLevel") {
                val batteryLevel = getBatteryLevel()
                if (batteryLevel != -1) {
                    result.success(batteryLevel)
                } else {
                    result.error("UNAVAILABLE", "Battery level not available.", null)
                }
            } else if (call.method == "getAssistantService") {
                val service = getAssistantService()
                result.success(service.toString())
            } else if (call.method == "handlerOpen") {
                showToast("设置状态为 ${call.arguments}")
                if(call.arguments as Boolean){
                    this.startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS));
                } else {
                    getAssistantService()?.disableSelf()
                }
                result.success(true)
            } else if (call.method == "handlerService") {
                val handleResult = when (call.arguments) {
                    "back" -> performGlobalAction(AccessibilityService.GLOBAL_ACTION_BACK)
                    "home" -> performGlobalAction(AccessibilityService.GLOBAL_ACTION_HOME)
                    "recent" -> performGlobalAction(AccessibilityService.GLOBAL_ACTION_RECENTS)
                    else -> {
                        println("未知命令${call.arguments}")
                        false
                    }
                }
                result.success(handleResult)
            } else {
                result.notImplemented()
            }
        }
    }

    //获取设备电量
    private fun getBatteryLevel(): Int {
        val batteryLevel: Int = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            val batteryManager = getSystemService(Context.BATTERY_SERVICE) as BatteryManager
            batteryManager.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY)
        } else {
            val intent = ContextWrapper(applicationContext).registerReceiver(
                null,
                IntentFilter(Intent.ACTION_BATTERY_CHANGED)
            )
            intent!!.getIntExtra(BatteryManager.EXTRA_LEVEL, -1) * 100 / intent.getIntExtra(
                BatteryManager.EXTRA_SCALE,
                -1
            )
        }
        return batteryLevel
    }

    private fun getAssistantService(): AccessibilitySampleService? {
        val service = AccessibilitySampleService.getInstance()
        if (service == null) {
            //todo:没有开启无障碍时跳转过去
            println("无障碍未开启")
        }
        return service
    }

    private fun performGlobalAction(action: Int): Boolean {
        Log.d("执行了", action.toString())
        return getAssistantService()?.performGlobalAction(action) ?: false
    }

    private fun showToast(msg: String) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
    }
}
