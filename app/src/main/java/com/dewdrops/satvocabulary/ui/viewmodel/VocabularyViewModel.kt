package com.dewdrops.satvocabulary.ui.viewmodel

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import com.dewdrops.satvocabulary.data.VocabularyWord
import com.dewdrops.satvocabulary.data.VocabularyRepository
import com.dewdrops.satvocabulary.ui.theme.ThemeMode
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.update

data class QuizQuestion(
    val word: VocabularyWord,
    val questionType: QuizType,
    val questionText: String,
    val options: List<String>,
    val correctAnswer: String
)

enum class QuizType {
    DEFINITION, SYNONYM, ANTONYM
}

class VocabularyViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = VocabularyRepository(application)
    private val prefs = application.getSharedPreferences("sat_vocab_prefs", Context.MODE_PRIVATE)

    private val _themeMode = MutableStateFlow(
        try {
            ThemeMode.valueOf(prefs.getString("theme_mode", "SYSTEM") ?: "SYSTEM")
        } catch (e: Exception) {
            ThemeMode.SYSTEM
        }
    )
    val themeMode: StateFlow<ThemeMode> = _themeMode.asStateFlow()

    fun setThemeMode(mode: ThemeMode) {
        _themeMode.value = mode
        prefs.edit().putString("theme_mode", mode.name).apply()
    }

    private val _targetLanguage = MutableStateFlow(prefs.getString("target_language", "Spanish") ?: "Spanish")
    val targetLanguage: StateFlow<String> = _targetLanguage.asStateFlow()

    fun setTargetLanguage(language: String) {
        _targetLanguage.value = language
        prefs.edit().putString("target_language", language).apply()
    }

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _filterTab = MutableStateFlow("All") // "All" or "Bookmarked"
    val filterTab: StateFlow<String> = _filterTab.asStateFlow()

    val availableLanguages = listOf("Spanish", "French", "Mandarin", "Hindi", "Arabic", "German", "Bangla")

    val words: StateFlow<List<VocabularyWord>> = combine(
        repository.words,
        _searchQuery,
        _filterTab
    ) { wordList, query, tab ->
        wordList.filter { word ->
            val matchesSearch = query.isBlank() || 
                word.word.contains(query, ignoreCase = true) || 
                word.definition.contains(query, ignoreCase = true)
            val matchesTab = when (tab) {
                "Bookmarked" -> word.isBookmarked
                else -> true
            }
            matchesSearch && matchesTab
        }
    }.stateIn(
        scope = kotlinx.coroutines.CoroutineScope(kotlinx.coroutines.Dispatchers.Default),
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    private val _currentFlashcardIndex = MutableStateFlow(0)
    val currentFlashcardIndex: StateFlow<Int> = _currentFlashcardIndex.asStateFlow()

    fun toggleBookmark(wordId: Int) {
        repository.toggleBookmark(wordId)
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun setFilterTab(tab: String) {
        _filterTab.value = tab
        _currentFlashcardIndex.value = 0
    }

    fun nextFlashcard(totalSize: Int) {
        if (totalSize > 0) {
            _currentFlashcardIndex.update { (it + 1) % totalSize }
        }
    }

    fun prevFlashcard(totalSize: Int) {
        if (totalSize > 0) {
            _currentFlashcardIndex.update { if (it - 1 < 0) totalSize - 1 else it - 1 }
        }
    }

    // Quiz State
    private val _quizQuestions = MutableStateFlow<List<QuizQuestion>>(emptyList())
    val quizQuestions: StateFlow<List<QuizQuestion>> = _quizQuestions.asStateFlow()

    private val _currentQuizIndex = MutableStateFlow(0)
    val currentQuizIndex: StateFlow<Int> = _currentQuizIndex.asStateFlow()

    private val _quizScore = MutableStateFlow(0)
    val quizScore: StateFlow<Int> = _quizScore.asStateFlow()

    private val _isQuizFinished = MutableStateFlow(false)
    val isQuizFinished: StateFlow<Boolean> = _isQuizFinished.asStateFlow()

    init {
        generateQuiz()
    }

    fun generateQuiz() {
        val allWords = repository.words.value
        if (allWords.isEmpty()) return

        val shuffled = allWords.shuffled().take(5)
        val questions = shuffled.map { word ->
            val type = listOf(QuizType.DEFINITION, QuizType.SYNONYM, QuizType.ANTONYM).random()
            when (type) {
                QuizType.DEFINITION -> {
                    val wrongOptions = allWords.filter { it.id != word.id }.shuffled().take(3).map { it.definition }
                    val options = (wrongOptions + word.definition).shuffled()
                    QuizQuestion(
                        word = word,
                        questionType = type,
                        questionText = "What is the definition of '${word.word}'?",
                        options = options,
                        correctAnswer = word.definition
                    )
                }
                QuizType.SYNONYM -> {
                    val correctSynonym = word.synonyms.firstOrNull() ?: "None"
                    val wrongOptions = allWords.filter { it.id != word.id }.mapNotNull { it.synonyms.firstOrNull() }.shuffled().take(3)
                    val options = (wrongOptions + correctSynonym).shuffled()
                    QuizQuestion(
                        word = word,
                        questionType = type,
                        questionText = "Which of the following is a synonym for '${word.word}'?",
                        options = options,
                        correctAnswer = correctSynonym
                    )
                }
                QuizType.ANTONYM -> {
                    val correctAntonym = word.antonyms.firstOrNull() ?: "None"
                    val wrongOptions = allWords.filter { it.id != word.id }.mapNotNull { it.antonyms.firstOrNull() }.shuffled().take(3)
                    val options = (wrongOptions + correctAntonym).shuffled()
                    QuizQuestion(
                        word = word,
                        questionType = type,
                        questionText = "Which of the following is an antonym for '${word.word}'?",
                        options = options,
                        correctAnswer = correctAntonym
                    )
                }
            }
        }
        _quizQuestions.value = questions
        _currentQuizIndex.value = 0
        _quizScore.value = 0
        _isQuizFinished.value = false
    }

    fun answerQuizQuestion(selectedOption: String) {
        val questions = _quizQuestions.value
        val currentIndex = _currentQuizIndex.value
        if (currentIndex < questions.size) {
            if (questions[currentIndex].correctAnswer == selectedOption) {
                _quizScore.update { it + 1 }
            }
            if (currentIndex + 1 < questions.size) {
                _currentQuizIndex.update { it + 1 }
            } else {
                _isQuizFinished.value = true
            }
        }
    }
}
