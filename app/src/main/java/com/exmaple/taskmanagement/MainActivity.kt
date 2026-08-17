package com.exmaple.taskmanagement

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.exmaple.taskmanagement.navigation.NavGraph
import com.exmaple.taskmanagement.ui.theme.TaskManagementTheme
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentChange
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.media.AudioAttributes
import android.media.RingtoneManager
import androidx.core.app.NotificationCompat
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        // Permission granted or denied, handled by OS
    }

    private var notificationListener: com.google.firebase.firestore.ListenerRegistration? = null
    private val notifiedIds = mutableSetOf<String>()

    override fun onCreate(savedInstanceState: Bundle?) {
        val splashScreen = installSplashScreen()
        super.onCreate(savedInstanceState)

        // Keep the splash screen on-screen for a bit longer to show animation
        var keepSplashScreen = true
        splashScreen.setKeepOnScreenCondition { keepSplashScreen }
        
        // Remove after 1.5 seconds (animation duration + buffer)
        lifecycleScope.launch {
            kotlinx.coroutines.delay(1500)
            keepSplashScreen = false
        }

        enableEdgeToEdge()

        askNotificationPermission()
        
        // Observe auth state to start/stop listener
        FirebaseAuth.getInstance().addAuthStateListener { auth ->
            if (auth.currentUser != null) {
                if (notificationListener == null) {
                    startNotificationListener()
                }
            } else {
                notificationListener?.remove()
                notificationListener = null
            }
        }

        setContent {
            TaskManagementTheme {
                NavGraph()
            }
        }
    }

    private fun startNotificationListener() {
        val currentUid = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val db = FirebaseFirestore.getInstance()

        notificationListener = db.collection("notifications")
            .whereEqualTo("userId", currentUid)
            .addSnapshotListener { snapshots, e ->
                if (e != null) {
                    android.util.Log.e("NotificationListener", "Listen failed.", e)
                    return@addSnapshotListener
                }

                // We only care about NEW documents added while the app is alive.
                // We handle both ADDED (server-side first) and MODIFIED (local-first confirm).
                if (snapshots != null) {
                    for (dc in snapshots.documentChanges) {
                        if (dc.type == DocumentChange.Type.ADDED || dc.type == DocumentChange.Type.MODIFIED) {
                            val docId = dc.document.id
                            if (notifiedIds.contains(docId)) continue

                            val title = dc.document.getString("title") ?: "New Notification"
                            val message = dc.document.getString("message") ?: ""
                            
                            // Use ESTIMATE behavior so pending local writes get a usable timestamp instead of null.
                            val createdAt = dc.document.getTimestamp("createdAt", DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
                            val now = System.currentTimeMillis()
                            
                            // Only notify if within the recency window.
                            if (createdAt != null && (now - createdAt.toDate().time) < 15000) {
                                notifiedIds.add(docId)
                                android.util.Log.d("NotificationListener", "Triggering alert for: $title ($docId)")
                                showSystemNotification(title, message)
                            }
                        }
                    }
                }
            }
    }

    private fun showSystemNotification(title: String, message: String) {
        val channelId = "task_notifications_v2"
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val audioAttributes = AudioAttributes.Builder()
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .setUsage(AudioAttributes.USAGE_NOTIFICATION)
                .build()

            val channel = NotificationChannel(channelId, "Task Updates", NotificationManager.IMPORTANCE_HIGH).apply {
                setSound(defaultSoundUri, audioAttributes)
                enableVibration(true)
            }
            notificationManager.createNotificationChannel(channel)
        }

        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_IMMUTABLE)

        val builder = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(android.R.drawable.stat_notify_chat)
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setDefaults(NotificationCompat.DEFAULT_ALL)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setSound(defaultSoundUri)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)

        notificationManager.notify(System.currentTimeMillis().toInt(), builder.build())
    }

    private fun askNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) !=
                PackageManager.PERMISSION_GRANTED
            ) {
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }
}
