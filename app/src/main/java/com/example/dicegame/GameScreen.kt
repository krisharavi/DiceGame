package com.example.dicegame

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import kotlin.random.Random

@Composable
fun GameScreen(onBackToMenu: () -> Unit) {
    // State variables for tracking game progress and settings
    var targetScore by remember { mutableStateOf(101) } // Allow user to set target score
    var showScoreDialog by remember { mutableStateOf(true) }
    var humanDice by remember { mutableStateOf(List(5) { rollDice() }) }
    var selectedDice by remember { mutableStateOf(List(5) { false }) }
    var computerDice by remember { mutableStateOf(List(5) { rollDice() }) }
    var humanScore by remember { mutableStateOf(0) }
    var computerScore by remember { mutableStateOf(0) }
    var rollCount by remember { mutableStateOf(0) }
    var gameOver by remember { mutableStateOf(false) }
    var winnerMessage by remember { mutableStateOf("") }
    var winnerColor by remember { mutableStateOf(Color.Transparent) }
    var showDialog by remember { mutableStateOf(false) }
    var inTieBreaker by remember { mutableStateOf(false) } // Track if tiebreaker is active
    // New state variables for tracking total wins
    var humanWins by remember { mutableStateOf(0) }
    var computerWins by remember { mutableStateOf(0) }

    // Dialog to set the target score
    if (showScoreDialog) {
        AlertDialog(
            onDismissRequest = {},
            confirmButton = {
                Button(onClick = { if (targetScore > 0) showScoreDialog = false }) {
                    Text("OK")
                }
            },
            title = { Text("Set Target Score") },
            text = {
                TextField(
                    value = targetScore.toString(),
                    onValueChange = {
                        val newValue = it.toIntOrNull()
                        if (newValue != null && newValue > 0) targetScore = newValue
                    },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        )
    }

    // Function to determine the winner based on scores
    fun determineWinner() {
        if (humanScore > computerScore) {
            winnerMessage = "You Win!"
            humanWins++
            winnerColor = Color.Green
        } else {
            winnerMessage = "You Lose!"
            computerWins++
            winnerColor = Color.Red

        }
        showDialog = true
    }

    /**
     * Computer Strategy:
     * - If the computer is losing by more than 10 points, it re-rolls dice < 4 aggressively.
     * - If it's leading, it keeps dice ≥ 4.
     * - If the scores are close, it keeps dice ≥ 3.
     * - Maximum 2 re-rolls are allowed.
     */
    fun optimizedComputerReroll() {
        val scoreDifference = computerScore - humanScore
        val keepThreshold = when {
            scoreDifference < -10 -> 4  // Losing significantly, take more risks
            scoreDifference > 10 -> 3   // Winning comfortably, take fewer risks
            else -> 3                   // Neutral game, balance between risk and safety
        }

        repeat(2) {
            val keep = computerDice.map { it >= keepThreshold }
            computerDice = computerDice.mapIndexed { index, value ->
                if (keep[index]) value else rollDice()
            }
        }
    }

    // Function to score the current round
    fun scoreRound() {
        humanScore += humanDice.sum()
        repeat(2) {
            if (Random.nextBoolean()) {
                val keep = List(5) { Random.nextBoolean() }
                computerDice = computerDice.mapIndexed { index, value ->
                    if (keep[index]) value else rollDice() // Re-roll dice if not kept
                }
            }
        }
        optimizedComputerReroll() // Apply the computer's re-roll strategy
        computerScore += computerDice.sum()

        rollCount = 0
        selectedDice = List(5) { false }

        // Check if the game should end
        if (humanScore >= targetScore && computerScore >= targetScore) {
            if (humanScore == computerScore) {
                inTieBreaker = true
            } else {
                gameOver = true
                determineWinner()
            }
        } else if (humanScore >= targetScore) {
            gameOver = true
            winnerMessage = "You Win!"
            winnerColor = Color.Green
            humanWins++
            showDialog = true
        } else if (computerScore >= targetScore) {
            gameOver = true
            winnerMessage = "You Lose!"
            winnerColor = Color.Red
            computerWins++
            showDialog = true
        }
    }

    // Function for handling the tie-breaker round
    fun tieBreakerRound() {
        if (!inTieBreaker) return
        val humanTieBreaker = List(5) { rollDice() }.sum()
        val computerTieBreaker = List(5) { rollDice() }.sum()

        when {
            humanTieBreaker > computerTieBreaker -> {
                winnerMessage = "You Win the Tie-Breaker!"
                winnerColor = Color.Green
                gameOver = true
            }
            computerTieBreaker > humanTieBreaker -> {
                winnerMessage = "You Lose the Tie-Breaker!"
                winnerColor = Color.Red
                gameOver = true
            }
            else -> {
                tieBreakerRound() // Continue tie-breaking until resolved
            }
        }
        showDialog = true
    }

    // Main layout for the game UI
    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier.fillMaxSize().padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                Text(
                    text = "Score - You: $humanScore | Computer: $computerScore",
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.padding(16.dp)
                )
            }

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Start) {
                Text(
                    text = "H:$humanWins / C:$computerWins",
                    style = MaterialTheme.typography.bodyLarge,
                    //color = Color.Black,
                    modifier = Modifier.padding(16.dp)
                )
            }

            // Display human's dice and allow them to select dice
            Text("Your Dice (Tap to Keep):")
            Row {
                humanDice.forEachIndexed { index, diceValue ->
                    Image(
                        painter = painterResource(getDiceImage(diceValue)),
                        contentDescription = "Dice",
                        modifier = Modifier
                            .size(60.dp)
                            .padding(4.dp)
                            .background(if (selectedDice[index]) Color.Gray else Color.Transparent)
                            .clickable(enabled = rollCount > 0 && !gameOver) {
                                selectedDice = selectedDice.toMutableList().apply { this[index] = !this[index] }
                            }
                    )
                }
            }

            // Display computer's dice
            Spacer(modifier = Modifier.height(16.dp))
            Text("Computer's Dice:")
            Row {
                computerDice.forEach { diceValue ->
                    Image(
                        painter = painterResource(getDiceImage(diceValue)),
                        contentDescription = "Dice",
                        modifier = Modifier.size(60.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.height(16.dp))

            Row {
                Button(
                    onClick = {
                        humanDice = humanDice.mapIndexed { index, value ->
                            if (selectedDice[index]) value else rollDice()
                        }
                        rollCount++
                        if (rollCount == 3) scoreRound()
                    },
                    enabled = rollCount < 3 && !gameOver
                ) {
                    Text("Throw")
                }
                Spacer(modifier = Modifier.width(16.dp))
                Button(
                    onClick = { scoreRound() },
                    enabled = rollCount > 0 && !gameOver
                ) {
                    Text("Score")
                }
            }

            // Button for tie-breaker round if active
            if (inTieBreaker) {
                Button(
                    onClick = { tieBreakerRound() },
                    enabled = !gameOver
                ) {
                    Text("Tie-Breaker Roll")
                }
            }
        }
    }

    // Display the game over dialog
    if (showDialog) {
        AlertDialog(
            onDismissRequest = {},
            confirmButton = {
                Row {
                    Button(onClick = onBackToMenu) {
                        Text("Back to Menu")
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                }
            },
            title = { Text(text = "Game Over", style = MaterialTheme.typography.headlineMedium) },
            text = { Text(text = winnerMessage, style = MaterialTheme.typography.bodyLarge, color = winnerColor) }
        )
    }
}

// Function to get the appropriate dice image based on the dice value
fun getDiceImage(value: Int): Int {
    return when (value) {
        1 -> R.drawable.dice_1
        2 -> R.drawable.dice_2
        3 -> R.drawable.dice_3
        4 -> R.drawable.dice_4
        5 -> R.drawable.dice_5
        6 -> R.drawable.dice_6
        else -> R.drawable.dice_1
    }
}

// Function to roll a dice and return a value between 1 and 6
fun rollDice(): Int = Random.nextInt(1, 7)

/**
 * Detailed Computer Roll Strategy:
 *
 * The computer adapts its play style based on the score difference:
 *
 * 1. When Losing by More Than 10 Points:
 *    - The computer becomes more aggressive, re-rolling dice below 4 to try and catch up.
 *
 * 2. When Winning by More Than 10 Points:
 *    - The computer plays conservatively, keeping dice ≥ 4 and re-rolling those below.
 *
 * 3. When Scores are Close:
 *    - The computer keeps dice ≥ 3 and re-rolls others to balance risk and reward.
 *
 * Performance Justification:
 * - Adaptive: The strategy changes based on the score difference, making the computer feel like a smarter opponent.
 * - Risk Management: It takes risks when behind and plays safely when ahead, maximizing the chances of winning without unnecessary risks.
 *
 * Advantages:
 * - Dynamic: The computer adjusts its strategy based on the game state.
 * - Challenging: Keeps the player engaged by changing the computer's behavior.
 *
 * Disadvantages:
 * - Predictable Over Time: Experienced players may recognize the pattern.
 * - Simple: The strategy doesn’t simulate deep decision-making, limiting its flexibility.
 */