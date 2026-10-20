package com.dewdrops.satvocabulary.utils

import android.content.Context
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.dewdrops.satvocabulary.data.VocabularyRepository
import java.util.Calendar

class WordOfDayWorker(context: Context, params: WorkerParameters) : Worker(context, params) {
    override fun doWork(): Result {
        try {
            val repository = VocabularyRepository(applicationContext)
            val words = repository.words.value
            if (words.isNotEmpty()) {
                val dayOfYear = Calendar.getInstance().get(Calendar.DAY_OF_YEAR)
                val word = words[dayOfYear % words.size]
                NotificationHelper.showWordOfDayNotification(
                    applicationContext,
                    word.id,
                    word.word,
                    word.definition
                )
            }
            return Result.success()
        } catch (e: Exception) {
            e.printStackTrace()
            return Result.failure()
        }
    }
}
