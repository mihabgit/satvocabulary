package com.dewdrops.satvocabulary.utils

import android.app.Activity
import android.content.Context
import com.google.android.play.core.review.ReviewManagerFactory

object InAppReviewHelper {
    fun requestReview(context: Context) {
        val activity = context as? Activity ?: return
        val manager = ReviewManagerFactory.create(context)
        val request = manager.requestReviewFlow()
        request.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val reviewInfo = task.result
                val flow = manager.launchReviewFlow(activity, reviewInfo)
                flow.addOnCompleteListener { _ ->
                    // Review flow completed successfully
                }
            } else {
                // Fail silently without annoying user
            }
        }
    }
}
