package com.example.dicegame

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.dicegame.ui.theme.DiceGameTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()  // Enables edge-to-edge display for full screen experience

        setContent {
            DiceGameTheme {
                // State to track the current screen (menu or game)
                var currentScreen by rememberSaveable { mutableStateOf("menu") } // State for current screen

                // Conditional rendering based on the current screen
                when (currentScreen) {
                    "menu" -> MainMenu(onStartGame = { currentScreen = "game" })
                    "game" -> GameScreen(onBackToMenu = { currentScreen = "menu" })
                }
            }
        }
    }
}

@Composable
fun MainMenu(onStartGame: () -> Unit) {
    var showAboutDialog by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Button(onClick = onStartGame) { Text("New Game") }
        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = { showAboutDialog = true }) { Text("About") }
    }

    if (showAboutDialog) {
        ShowAboutDialog(onDismiss = { showAboutDialog = false })
    }
}

// A dialog to show information about the game and student details
@Composable
fun ShowAboutDialog(onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("About") },
        text = {
            Text(
                "Student Name: Krisha Ravichandran\nStudent ID: w2052723 / 20220849\n\n" +
                        "I confirm that I understand what plagiarism is and have read " +
                        "and understood the section on Assessment Offences in the Essential " +
                        "Information for Students. The work that I have submitted is entirely " +
                        "my own. Any work from other authors is duly referenced and acknowledged."
            )
        },
        confirmButton = {
            Button(onClick = onDismiss) { Text("OK") }
        }
    )
}
