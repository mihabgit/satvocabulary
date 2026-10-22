package com.dewdrops.satvocabulary.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.dewdrops.satvocabulary.ui.components.AdBannerView
import com.dewdrops.satvocabulary.ui.viewmodel.VocabularyViewModel
import com.dewdrops.satvocabulary.utils.InterstitialAdHelper

@Composable
fun QuizScreen(viewModel: VocabularyViewModel) {
    val questions by viewModel.quizQuestions.collectAsState()
    val currentIndex by viewModel.currentQuizIndex.collectAsState()
    val score by viewModel.quizScore.collectAsState()
    val isFinished by viewModel.isQuizFinished.collectAsState()
    val context = LocalContext.current

    var selectedOption by remember(currentIndex) { mutableStateOf<String?>(null) }

    // Show Interstitial Ad when quiz is finished
    LaunchedEffect(isFinished) {
        if (isFinished) {
            InterstitialAdHelper.showAd(context) {
                // Ad dismissed callback
            }
        }
    }

    Scaffold(
        contentWindowInsets = WindowInsets(0, 0, 0, 0)
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .statusBarsPadding()
                .padding(16.dp)
        ) {
            // Header
            Text(
                text = "SAT Vocabulary Quiz",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(8.dp))
            AdBannerView()
            Spacer(modifier = Modifier.height(12.dp))

            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                if (questions.isEmpty()) {
                    Text("Loading quiz...", color = MaterialTheme.colorScheme.onSurface)
                } else if (isFinished) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = "Quiz Completed! 🎉",
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "Your Score: $score / ${questions.size}",
                            style = MaterialTheme.typography.titleLarge,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        val percentage = (score.toFloat() / questions.size) * 100
                        Text(
                            text = when {
                                percentage >= 80 -> "Outstanding! You're SAT ready! 🚀"
                                percentage >= 50 -> "Good job! Keep practicing! 📚"
                                else -> "Keep reviewing your flashcards! 💪"
                            },
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.SemiBold
                        )
                        Spacer(modifier = Modifier.height(24.dp))
                        Button(onClick = { viewModel.generateQuiz() }) {
                            Text("Try Again")
                        }
                    }
                } else {
                    val question = questions[currentIndex]

                    Column(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.SpaceBetween,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalAlignment = Alignment.Start
                        ) {
                            LinearProgressIndicator(
                                progress = { (currentIndex + 1).toFloat() / questions.size },
                                modifier = Modifier.fillMaxWidth()
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "Question ${currentIndex + 1} of ${questions.size}",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Text(
                                    text = "Score: $score",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.secondary
                                )
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = question.questionText,
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }

                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            question.options.forEach { option ->
                                val isSelected = selectedOption == option
                                val isCorrect = option == question.correctAnswer
                                val buttonColor = when {
                                    selectedOption != null && isCorrect -> MaterialTheme.colorScheme.primaryContainer
                                    selectedOption != null && isSelected -> MaterialTheme.colorScheme.errorContainer
                                    else -> MaterialTheme.colorScheme.surfaceVariant
                                }
                                val textColor = when {
                                    selectedOption != null && isCorrect -> MaterialTheme.colorScheme.onPrimaryContainer
                                    selectedOption != null && isSelected -> MaterialTheme.colorScheme.onErrorContainer
                                    else -> MaterialTheme.colorScheme.onSurfaceVariant
                                }

                                Button(
                                    onClick = {
                                        if (selectedOption == null) {
                                            selectedOption = option
                                            viewModel.answerQuizQuestion(option)
                                        }
                                    },
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = buttonColor,
                                        contentColor = textColor
                                    )
                                ) {
                                    Text(
                                        text = option,
                                        style = MaterialTheme.typography.bodyLarge,
                                        textAlign = TextAlign.Center,
                                        modifier = Modifier.padding(8.dp)
                                    )
                                }
                            }

                            if (selectedOption != null) {
                                Spacer(modifier = Modifier.height(8.dp))
                                Button(
                                    onClick = { selectedOption = null },
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text("Next Question")
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))
                    }
                }
            }
        }
    }
}
