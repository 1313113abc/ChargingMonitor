package com.example.chargingmonitor

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.example.chargingmonitor.databinding.ActivityMainBinding
import com.example.chargingmonitor.service.ChargingMonitorService

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private var isServiceRunning = false

    private val requestNotificationPermission = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            startMonitorService()
        } else {
            Toast.makeText(this, "需要通知权限才能在通知栏显示状态", Toast.LENGTH_LONG).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.startButton.setOnClickListener {
            if (isServiceRunning) {
                stopMonitorService()
            } else {
                checkAndStartService()
            }
        }

        updateUI()
    }

    private fun checkAndStartService() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            requestNotificationPermission.launch(android.Manifest.permission.POST_NOTIFICATIONS)
        } else {
            startMonitorService()
        }
    }

    private fun startMonitorService() {
        val intent = Intent(this, ChargingMonitorService::class.java)
        ContextCompat.startForegroundService(this, intent)
        isServiceRunning = true
        updateUI()
        Toast.makeText(this, "监控服务已启动", Toast.LENGTH_SHORT).show()
    }

    private fun stopMonitorService() {
        val intent = Intent(this, ChargingMonitorService::class.java)
        stopService(intent)
        isServiceRunning = false
        updateUI()
        Toast.makeText(this, "监控服务已停止", Toast.LENGTH_SHORT).show()
    }

    private fun updateUI() {
        if (isServiceRunning) {
            binding.statusText.text = "监控服务运行中"
            binding.statusText.setTextColor(ContextCompat.getColor(this, R.color.green))
            binding.startButton.text = "停止监控"
            binding.startButton.backgroundTintList = ContextCompat.getColorStateList(this, R.color.red)
        } else {
            binding.statusText.text = "充电状态检测器"
            binding.statusText.setTextColor(ContextCompat.getColor(this, R.color.white))
            binding.startButton.text = "开始监控"
            binding.startButton.backgroundTintList = ContextCompat.getColorStateList(this, R.color.green)
        }
    }
}
