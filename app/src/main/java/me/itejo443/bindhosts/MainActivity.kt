package me.itejo443.bindhosts

import android.content.Context
import android.content.Intent
import android.app.PendingIntent
import android.app.NotificationManager
import android.app.NotificationChannel
import android.os.Build
import android.util.Log
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import java.io.BufferedReader
import java.io.InputStreamReader


class MainActivity : AppCompatActivity() {

    private val NOTIFICATION_PERMISSION_REQUEST_CODE = 1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Always show the real permission state first. The old code only
        // updated this TextView from onRequestPermissionsResult(), so an
        // already-granted permission left the layout's default "Pending" text.
        updateNotificationStatus()
        updateRootStatus()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ContextCompat.checkSelfPermission(this, android.Manifest.permission.POST_NOTIFICATIONS)
            != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(android.Manifest.permission.POST_NOTIFICATIONS),
                NOTIFICATION_PERMISSION_REQUEST_CODE
            )
        }

        // The button previously had no click listener at all.
        findViewById<Button>(R.id.check_button).setOnClickListener {
            updateNotificationStatus()
            updateRootStatus()
        }
    }

    private fun updateNotificationStatus() {
        val notificationStatusText: TextView = findViewById(R.id.notification_status)
        val notificationsEnabled = NotificationManagerCompat.from(this).areNotificationsEnabled()

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
            notificationStatusText.text = if (notificationsEnabled) {
                "Notification Permission: Granted"
            } else {
                "Notification Permission: Disabled in system settings"
            }
            return
        }

        val granted = ContextCompat.checkSelfPermission(
            this,
            android.Manifest.permission.POST_NOTIFICATIONS
        ) == PackageManager.PERMISSION_GRANTED

        notificationStatusText.text = when {
            !granted -> "Notification Permission: Pending"
            !notificationsEnabled -> "Notification Permission: Granted (notifications disabled in system settings)"
            else -> "Notification Permission: Granted"
        }
    }

    private fun updateRootStatus() {
        val rootStatusText: TextView = findViewById(R.id.root_status)
        rootStatusText.text = if (isRootAvailable()) {
            "Root Access: Enabled"
        } else {
            "Root Access: Disabled"
        }
    }

    // Handle permission request result
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == NOTIFICATION_PERMISSION_REQUEST_CODE) {
            updateNotificationStatus()
        }
    }

    private fun isRootAvailable(): Boolean {
        return try {
            val process = Runtime.getRuntime().exec("su -c whoami")
            val reader = BufferedReader(InputStreamReader(process.inputStream))
            val line = reader.readLine()
            line != null && line.trim().equals("root", ignoreCase = true)
        } catch (e: Exception) {
            false
        }
    }
}
