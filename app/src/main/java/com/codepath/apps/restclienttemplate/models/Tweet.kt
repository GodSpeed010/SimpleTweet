package com.codepath.apps.restclienttemplate.models

import android.text.format.DateUtils
import org.json.JSONArray
import org.json.JSONObject
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList


class Tweet {

    var body: String = ""
    var relativeTimestamp: String = ""
    var user: User? = null
    var retweets: Int = 0
    var favoriteCount: Int = 0
    var id: String = ""

    companion object {
        fun fromJson(jsonObject: JSONObject): Tweet {
            val tweet = Tweet()
            tweet.body = jsonObject.getString("text")
            tweet.relativeTimestamp = getRelativeTimeAgo(jsonObject.getString("created_at"))
            tweet.user = User.fromJson(jsonObject.getJSONObject("user"))
            tweet.retweets = jsonObject.getInt("retweet_count") //todo
            tweet.favoriteCount = jsonObject.getInt("favorite_count")
            tweet.id = jsonObject.getString("id_str")
            return tweet
        }

        fun fromJsonArray(jsonArray: JSONArray): ArrayList<Tweet> {
            val tweets = ArrayList<Tweet>()
            for (i in 0 until jsonArray.length()) {
                tweets.add(fromJson(jsonArray.getJSONObject(i)))
            }
            return tweets
        }

        private fun getRelativeTimeAgo(rawJsonDate: String): String {
            val twitterFormat = "EEE MMM dd HH:mm:ss ZZZZZ yyyy"
            val sf = SimpleDateFormat(twitterFormat, Locale.ENGLISH)
            sf.isLenient = true
            var relativeDate = ""
            try {
                val dateMillis: Long = sf.parse(rawJsonDate).time
                relativeDate = DateUtils.getRelativeTimeSpanString(
                    dateMillis,
                    System.currentTimeMillis(), DateUtils.SECOND_IN_MILLIS
                ).toString()
            } catch (e: ParseException) {
                e.printStackTrace()
            }
            return relativeDate
        }
    }
}