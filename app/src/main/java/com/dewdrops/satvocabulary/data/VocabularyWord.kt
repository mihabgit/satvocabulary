package com.dewdrops.satvocabulary.data

data class VocabularyWord(
    val id: Int,
    val word: String,
    val partOfSpeech: String,
    val definition: String,
    val exampleSentence: String,
    val synonyms: List<String>,
    val antonyms: List<String>,
    val translations: Map<String, String>, // Language Name -> Translated Definition
    val difficulty: String = "Medium",
    val pronunciationIpa: String = "",
    val pronunciationSimple: String = "",
    val wordRoots: String = "",
    val memoryTip: String = "",
    val isBookmarked: Boolean = false
)
