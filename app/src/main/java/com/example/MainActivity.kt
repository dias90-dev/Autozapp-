package com.example

import android.content.Context
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.ui.AutoReplyViewModel
import com.example.ui.AutoReplyViewModelFactory
import com.example.ui.HomeScreen
import com.example.ui.openNotificationSettings
import com.example.ui.theme.MyApplicationTheme

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.draw.clip
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.border
import androidx.compose.ui.text.font.FontWeight

class MainActivity : ComponentActivity() {

    private val viewModel: AutoReplyViewModel by viewModels {
        val app = application as AutoReplyApplication
        AutoReplyViewModelFactory(app.repository, app.preferenceManager)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    HomeScreen(viewModel = viewModel)
                }
            }
        }
    }
}

fun isNotificationServiceEnabled(context: Context): Boolean {
    return try {
        val packageName = context.packageName
        val flat = Settings.Secure.getString(context.contentResolver, "enabled_notification_listeners")
        if (flat != null) {
            val names = flat.split(":")
            for (name in names) {
                val componentName = android.content.ComponentName.unflattenFromString(name)
                if (componentName != null && componentName.packageName == packageName) {
                    return true
                }
            }
        }
        false
    } catch (e: Exception) {
        Log.e("MainActivity", "Failed to check notification service status", e)
        false
    }
}

@Composable
fun PermissionBanner(onGrantClick: () -> Unit) {
    Surface(
        color = Color(0xFFFF5252).copy(alpha = 0.1f),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .clip(RoundedCornerShape(24.dp))
            .border(1.dp, Color(0xFFFF5252).copy(alpha = 0.3f), RoundedCornerShape(24.dp))
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Icon(
                Icons.Default.Warning,
                contentDescription = null,
                tint = Color(0xFFFF5252)
            )
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "ENGINE OFFLINE",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color(0xFFFF5252)
                )
                Text(
                    text = "Notification access required for automation.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Button(
                onClick = onGrantClick,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFFF5252),
                    contentColor = Color.Black
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("ENABLE", fontWeight = FontWeight.Bold)
            }
        }
    }
}
