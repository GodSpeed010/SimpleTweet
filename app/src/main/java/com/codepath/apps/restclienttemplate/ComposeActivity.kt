package com.codepath.apps.restclienttemplate

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.core.widget.addTextChangedListener
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.codepath.apps.restclienttemplate.databinding.ActivityComposeBinding
import com.codepath.apps.restclienttemplate.models.Tweet
import com.codepath.asynchttpclient.callback.JsonHttpResponseHandler
import okhttp3.Headers

class ComposeActivity : AppCompatActivity() {

    lateinit var client: TwitterClient

    lateinit var binding: ActivityComposeBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityComposeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.lifecycleOwner = this

        val characterCount: MutableLiveData<String> = MutableLiveData("$MAX_TWEET_LENGTH")

        binding.characterCount = characterCount

        client = TwitterApplication.getRestClient(this)

        binding.btnTweet.setOnClickListener {

            //Grab content of edittext
            val tweetContent = binding.etTweetCompose.text.toString()

            // 1. Make sure tweet isn't empty
            if (tweetContent.isEmpty()) {
                Toast.makeText(this, "Empty tweets are not allowed", Toast.LENGTH_SHORT).show()
            } else {
                // 2. Make sure tweet is under character count
                if (tweetContent.length > MAX_TWEET_LENGTH) {
                    Toast.makeText(
                        this,
                        "Tweet is too long! Limit is $MAX_TWEET_LENGTH characters",
                        Toast.LENGTH_SHORT
                    ).show()
                } else {
                    client.publishTweet(
                        tweetContent,
                        object : JsonHttpResponseHandler() {

                            override fun onSuccess(
                                statusCode: Int,
                                headers: Headers?,
                                json: JSON
                            ) {
                                Log.i(TAG, "Successfully published tweet!")

                                val tweet = Tweet.fromJson(json.jsonObject)

                                val intent = Intent()
                                intent.putExtra("tweet", tweet)
                                setResult(RESULT_OK, intent)
                                finish()
                            }

                            override fun onFailure(
                                statusCode: Int,
                                headers: Headers?,
                                response: String?,
                                throwable: Throwable?
                            ) {
                                Log.e(TAG, "Failed to publish tweet", throwable)
                            }
                        }
                    )
                }
            }
        }

        binding.etTweetCompose.addTextChangedListener {
            //update LiveData
            characterCount.value = (MAX_TWEET_LENGTH - it.toString().length).toString()
        }
    }

    companion object {
        const val TAG = "ComposeActivity"
        const val MAX_TWEET_LENGTH = 280
    }
}