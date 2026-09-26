package com.example.waterreminder

import android.Manifest
import android.app.AlarmManager
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.example.waterreminder.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    private val notificationPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
            if (granted) enableReminders() else refreshUi()
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.scheduleText.text = ReminderScheduler.buildSlots()
            .joinToString("\n") { (h, m) -> "%02d:%02d".format(h, m) }

        binding.toggleButton.setOnClickListener {
            if (ReminderScheduler.isEnabled(this)) {
                ReminderScheduler.setEnabled(this, false)
                ReminderScheduler.cancelAll(this)
                refreshUi()
            } else {
                requestPermissionsThenEnable()
            }
        }

        refreshUi()
    }

    override fun onResume() {
        super.onResume()
        refreshUi()
    }

    private fun requestPermissionsThenEnable() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
            != PackageManager.PERMISSION_GRANTED
        ) {
            notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            return
        }
        enableReminders()
    }

    private fun enableReminders() {
        if (!ReminderScheduler.canScheduleExact(this)) {
            // Send the user to the system screen where exact-alarm permission is granted.
            val intent = Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM).apply {
                data = Uri.parse("package:$packageName")
            }
            startActivity(intent)
            refreshUi()
            return
        }
        ReminderScheduler.setEnabled(this, true)
        ReminderScheduler.scheduleAll(this)
        refreshUi()
    }

    private fun refreshUi() {
        val enabled = ReminderScheduler.isEnabled(this)
        binding.statusText.text = if (enabled) "Trạng thái: đang bật ✓" else "Trạng thái: đang tắt"
        binding.toggleButton.text = if (enabled) "Tắt nhắc nhở" else "Bật nhắc nhở"

        binding.permissionHint.text = when {
            !ReminderScheduler.canScheduleExact(this) ->
                "Cần cấp quyền \"Báo thức & lời nhắc\" trong Cài đặt để thông báo đúng giờ."
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
                ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                != PackageManager.PERMISSION_GRANTED ->
                "Cần cấp quyền thông báo để nhận nhắc nhở."
            else -> ""
        }
    }
}
