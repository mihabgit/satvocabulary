package com.dewdrops.satvocabulary.data

import android.content.Context
import com.dewdrops.satvocabulary.data.VocabularyWord
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import org.json.JSONArray
import org.json.JSONObject
import java.io.File

class VocabularyRepository(private val context: Context) {
    private val storageFile = File(context.filesDir, "sat_vocab_storage.json")
    private val _words = MutableStateFlow<List<VocabularyWord>>(emptyList())
    val words: StateFlow<List<VocabularyWord>> = _words.asStateFlow()

    init {
        loadVocabularies()
    }

    private fun loadVocabularies() {
        try {
            // Read base words from assets (always up-to-date with new fields/words)
            val inputStream = context.assets.open("sat_vocabularies.json")
            val size = inputStream.available()
            val buffer = ByteArray(size)
            inputStream.read(buffer)
            inputStream.close()
            val assetJson = String(buffer, Charsets.UTF_8)

            // Read user bookmarks cache if exists
            val bookmarkedIds = mutableSetOf<Int>()
            if (storageFile.exists()) {
                try {
                    val oldJson = storageFile.readText()
                    val oldArray = JSONArray(oldJson)
                    for (i in 0 until oldArray.length()) {
                        val obj = oldArray.getJSONObject(i)
                        if (obj.optBoolean("isBookmarked", false)) {
                            bookmarkedIds.add(obj.getInt("id"))
                        }
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }

            val jsonArray = JSONArray(assetJson)
            val list = mutableListOf<VocabularyWord>()
            for (i in 0 until jsonArray.length()) {
                val obj = jsonArray.getJSONObject(i)
                val id = obj.getInt("id")
                val word = obj.getString("word")
                val partOfSpeech = obj.getString("partOfSpeech")
                val definition = obj.getString("definition")
                val exampleSentence = obj.getString("exampleSentence")
                
                val synonymsArray = obj.getJSONArray("synonyms")
                val synonyms = mutableListOf<String>()
                for (j in 0 until synonymsArray.length()) {
                    synonyms.add(synonymsArray.getString(j))
                }

                val antonymsArray = obj.getJSONArray("antonyms")
                val antonyms = mutableListOf<String>()
                for (j in 0 until antonymsArray.length()) {
                    antonyms.add(antonymsArray.getString(j))
                }

                val translationsObj = obj.getJSONObject("translations")
                val translations = mutableMapOf<String, String>()
                val keys = translationsObj.keys()
                while (keys.hasNext()) {
                    val key = keys.next()
                    translations[key] = translationsObj.getString(key)
                }

                val difficulty = obj.optString("difficulty", "Medium")
                val pronunciationIpa = obj.optString("pronunciationIpa", "")
                val pronunciationSimple = obj.optString("pronunciationSimple", "")
                val wordRoots = obj.optString("wordRoots", "")
                val memoryTip = obj.optString("memoryTip", "")
                val isBookmarked = bookmarkedIds.contains(id) || obj.optBoolean("isBookmarked", false)

                list.add(
                    VocabularyWord(
                        id = id,
                        word = word,
                        partOfSpeech = partOfSpeech,
                        definition = definition,
                        exampleSentence = exampleSentence,
                        synonyms = synonyms,
                        antonyms = antonyms,
                        translations = translations,
                        difficulty = difficulty,
                        pronunciationIpa = pronunciationIpa,
                        pronunciationSimple = pronunciationSimple,
                        wordRoots = wordRoots,
                        memoryTip = memoryTip,
                        isBookmarked = isBookmarked
                    )
                )
            }
            _words.value = list
            saveVocabularies(list)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun toggleBookmark(wordId: Int) {
        _words.update { list ->
            val updated = list.map { word ->
                if (word.id == wordId) word.copy(isBookmarked = !word.isBookmarked) else word
            }
            saveVocabularies(updated)
            updated
        }
    }

    private fun saveVocabularies(list: List<VocabularyWord>) {
        try {
            val jsonArray = JSONArray()
            for (word in list) {
                val obj = JSONObject().apply {
                    put("id", word.id)
                    put("word", word.word)
                    put("partOfSpeech", word.partOfSpeech)
                    put("definition", word.definition)
                    put("exampleSentence", word.exampleSentence)
                    put("synonyms", JSONArray(word.synonyms))
                    put("antonyms", JSONArray(word.antonyms))
                    put("translations", JSONObject(word.translations as Map<*, *>))
                    put("difficulty", word.difficulty)
                    put("pronunciationIpa", word.pronunciationIpa)
                    put("pronunciationSimple", word.pronunciationSimple)
                    put("wordRoots", word.wordRoots)
                    put("memoryTip", word.memoryTip)
                    put("isBookmarked", word.isBookmarked)
                }
                jsonArray.put(obj)
            }
            storageFile.writeText(jsonArray.toString())
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
