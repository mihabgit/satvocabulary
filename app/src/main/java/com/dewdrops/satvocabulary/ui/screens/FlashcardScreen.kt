package com.dewdrops.satvocabulary.ui.screens

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.dewdrops.satvocabulary.ui.components.AdBannerView
import com.dewdrops.satvocabulary.ui.viewmodel.VocabularyViewModel

@Composable
fun FlashcardScreen(viewModel: VocabularyViewModel) {
    val words by viewModel.words.collectAsState()
    val currentIndex by viewModel.currentFlashcardIndex.collectAsState()
    val targetLanguage by viewModel.targetLanguage.collectAsState()

    var isFlipped by remember { mutableStateOf(false) }

    val rotation by animateFloatAsState(
        targetValue = if (isFlipped) 180f else 0f,
        animationSpec = tween(durationMillis = 400),
        label = "CardFlip"
    )

    Scaffold(
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        bottomBar = {
            //AdBannerView()
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
        ) {
            // Header
            Row(
                modifier = Modifier
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "SAT Flashcards",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "${currentIndex + 1} / ${words.size.coerceAtLeast(1)}",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.SemiBold
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                if (words.isEmpty()) {
                    Text(
                        text = "No SAT words found matching your filter.",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurface,
                        textAlign = TextAlign.Center
                    )
                } else {
                    val word = words[currentIndex.coerceIn(0, words.size - 1)]

                    Column(
                        modifier = Modifier.fillMaxSize(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        // Flashcard Card
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(460.dp)
                                .graphicsLayer {
                                    rotationY = rotation
                                    cameraDistance = 8f * density
                                }
                                .clickable { isFlipped = !isFlipped },
                            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = if (rotation <= 90f) MaterialTheme.colorScheme.surfaceVariant
                                else MaterialTheme.colorScheme.tertiaryContainer
                            )
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(24.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                if (rotation <= 90f) {
                                    // Front of Card
                                    Column(
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        verticalArrangement = Arrangement.Center
                                    ) {
                                        AssistChip(
                                            onClick = {},
                                            label = { Text("Difficulty: ${word.difficulty}", style = MaterialTheme.typography.bodyMedium) }
                                        )
                                        Spacer(modifier = Modifier.height(16.dp))
                                        Text(
                                            text = word.word,
                                            style = MaterialTheme.typography.displaySmall,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                        Spacer(modifier = Modifier.height(8.dp))
                                        Text(
                                            text = "(${word.partOfSpeech})  •  ${word.pronunciationSimple}",
                                            style = MaterialTheme.typography.titleMedium,
                                            fontStyle = FontStyle.Italic,
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            text = word.pronunciationIpa,
                                            style = MaterialTheme.typography.bodyLarge,
                                            color = MaterialTheme.colorScheme.outline
                                        )
                                        Spacer(modifier = Modifier.height(32.dp))
                                        Text(
                                            text = "Tap to flip card",
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = MaterialTheme.colorScheme.outline
                                        )
                                    }
                                } else {
                                    // Back of Card (Full Data View with Larger Text Sizes)
                                    Column(
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .graphicsLayer { rotationY = 180f }
                                            .verticalScroll(rememberScrollState()),
                                        horizontalAlignment = Alignment.Start,
                                        verticalArrangement = Arrangement.spacedBy(10.dp)
                                    ) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Column {
                                                Text(
                                                    text = word.word,
                                                    style = MaterialTheme.typography.headlineMedium,
                                                    fontWeight = FontWeight.Bold,
                                                    color = MaterialTheme.colorScheme.onTertiaryContainer
                                                )
                                                Text(
                                                    text = "(${word.partOfSpeech})  •  IPA: ${word.pronunciationIpa}  •  Simple: ${word.pronunciationSimple}",
                                                    style = MaterialTheme.typography.bodyLarge,
                                                    fontStyle = FontStyle.Italic,
                                                    color = MaterialTheme.colorScheme.onTertiaryContainer.copy(alpha = 0.8f)
                                                )
                                            }
                                            IconButton(onClick = { viewModel.toggleBookmark(word.id) }) {
                                                Icon(
                                                    imageVector = if (word.isBookmarked) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                                                    contentDescription = "Bookmark",
                                                    tint = MaterialTheme.colorScheme.error,
                                                    modifier = Modifier.size(28.dp)
                                                )
                                            }
                                        }

                                        HorizontalDivider(color = MaterialTheme.colorScheme.onTertiaryContainer.copy(alpha = 0.3f))

                                        Text(
                                            text = "📖 Definition:",
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                        Text(
                                            text = word.definition,
                                            style = MaterialTheme.typography.bodyLarge,
                                            color = MaterialTheme.colorScheme.onTertiaryContainer
                                        )

                                        Text(
                                            text = "💬 Example Sentence:",
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                        Text(
                                            text = "\"${word.exampleSentence}\"",
                                            style = MaterialTheme.typography.bodyMedium,
                                            fontStyle = FontStyle.Italic,
                                            color = MaterialTheme.colorScheme.onTertiaryContainer.copy(alpha = 0.9f)
                                        )

                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                                        ) {
                                            Column(modifier = Modifier.weight(1f)) {
                                                Text(
                                                    text = "🔗 Synonyms:",
                                                    style = MaterialTheme.typography.bodyMedium,
                                                    fontWeight = FontWeight.Bold,
                                                    color = MaterialTheme.colorScheme.primary
                                                )
                                                Text(
                                                    text = word.synonyms.joinToString(", "),
                                                    style = MaterialTheme.typography.bodyMedium,
                                                    color = MaterialTheme.colorScheme.onTertiaryContainer
                                                )
                                            }
                                            Column(modifier = Modifier.weight(1f)) {
                                                Text(
                                                    text = "🔀 Antonyms:",
                                                    style = MaterialTheme.typography.bodyMedium,
                                                    fontWeight = FontWeight.Bold,
                                                    color = MaterialTheme.colorScheme.secondary
                                                )
                                                Text(
                                                    text = word.antonyms.joinToString(", "),
                                                    style = MaterialTheme.typography.bodyMedium,
                                                    color = MaterialTheme.colorScheme.onTertiaryContainer
                                                )
                                            }
                                        }

                                        if (word.wordRoots.isNotBlank()) {
                                            Text(
                                                text = "🌳 Roots & Origin:",
                                                style = MaterialTheme.typography.titleMedium,
                                                fontWeight = FontWeight.Bold,
                                                color = MaterialTheme.colorScheme.tertiary
                                            )
                                            Text(
                                                text = word.wordRoots,
                                                style = MaterialTheme.typography.bodyMedium,
                                                color = MaterialTheme.colorScheme.onTertiaryContainer
                                            )
                                        }

                                        if (word.memoryTip.isNotBlank()) {
                                            Text(
                                                text = "💡 Mnemonic Tip:",
                                                style = MaterialTheme.typography.titleMedium,
                                                fontWeight = FontWeight.Bold,
                                                color = MaterialTheme.colorScheme.tertiary
                                            )
                                            Text(
                                                text = word.memoryTip,
                                                style = MaterialTheme.typography.bodyMedium,
                                                fontWeight = FontWeight.Medium,
                                                color = MaterialTheme.colorScheme.onTertiaryContainer
                                            )
                                        }

                                        Text(
                                            text = "🌐 Translation ($targetLanguage):",
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                        Text(
                                            text = word.translations[targetLanguage] ?: "N/A",
                                            style = MaterialTheme.typography.bodyLarge,
                                            fontWeight = FontWeight.SemiBold,
                                            color = MaterialTheme.colorScheme.onTertiaryContainer
                                        )

                                        Spacer(modifier = Modifier.height(12.dp))
                                        Text(
                                            text = "Tap to flip back",
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = MaterialTheme.colorScheme.onTertiaryContainer.copy(alpha = 0.6f),
                                            modifier = Modifier.align(Alignment.CenterHorizontally)
                                        )
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Navigation Controls
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Button(
                                onClick = {
                                    isFlipped = false
                                    viewModel.prevFlashcard(words.size)
                                },
                                modifier = Modifier.height(48.dp)
                            ) {
                                Text("Previous", style = MaterialTheme.typography.bodyLarge)
                            }
                            IconButton(onClick = { isFlipped = false }) {
                                Icon(Icons.Default.Refresh, contentDescription = "Reset Card", modifier = Modifier.size(32.dp))
                            }
                            Button(
                                onClick = {
                                    isFlipped = false
                                    viewModel.nextFlashcard(words.size)
                                },
                                modifier = Modifier.height(48.dp)
                            ) {
                                Text("Next", style = MaterialTheme.typography.bodyLarge)
                            }
                        }
                    }
                }
            }
        }
    }
}
