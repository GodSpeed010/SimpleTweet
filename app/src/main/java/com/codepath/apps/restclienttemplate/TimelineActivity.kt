package com.codepath.apps.restclienttemplate

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.codepath.apps.restclienttemplate.models.Tweet
import com.codepath.asynchttpclient.RequestParams
import com.codepath.asynchttpclient.callback.JsonHttpResponseHandler
import com.google.android.material.floatingactionbutton.FloatingActionButton
import okhttp3.Headers
import org.json.JSONException

class TimelineActivity : AppCompatActivity() {

    val TAG = "TimelineActivity"

    lateinit var client: TwitterClient

    lateinit var rvTweets: RecyclerView
    lateinit var adapter: TweetsAdapter
    val tweets = ArrayList<Tweet>()

    lateinit var swipeContainer: SwipeRefreshLayout
    lateinit var fabCompose: FloatingActionButton
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_timeline)

        client = TwitterApplication.getRestClient(this)

        swipeContainer = findViewById(R.id.swipeContainer)
        fabCompose = findViewById(R.id.fabCompose)
        
        swipeContainer.setOnRefreshListener {
            Log.i(TAG, "Refreshing timeline")
            populateHomeTimeline()
        }

        // Configure the refreshing colors
        swipeContainer.setColorSchemeResources(
            android.R.color.holo_blue_bright,
            android.R.color.holo_green_light,
            android.R.color.holo_orange_light,
            android.R.color.holo_red_light
        )

        rvTweets = findViewById(R.id.rvTweets)
        adapter = TweetsAdapter(tweets)

        rvTweets.layoutManager = LinearLayoutManager(this)
        rvTweets.adapter = adapter

        rvTweets.addOnScrollListener(object :
            EndlessRecyclerViewScrollListener(rvTweets.layoutManager as LinearLayoutManager) {
            override fun onLoadMore(page: Int, totalItemsCount: Int, view: RecyclerView?) {
                Log.i(TAG, "onLoadMore page $page")

                loadNextDataFromApi(page)
            }

        }
        )

        fabCompose.setOnClickListener {
            startComposeActivity()
        }
        
        populateHomeTimeline()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    //Handles clicks on menu item
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.compose -> {
                startComposeActivity()
            }
        }
        return super.onOptionsItemSelected(item)
    }

    private fun startComposeActivity() {
        val intent = Intent(this, ComposeActivity::class.java)
        startActivityForResult(intent, REQUEST_CODE)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == REQUEST_CODE) {
            // Get data from our intent
            val tweet = data?.getParcelableExtra("tweet") as Tweet

            //Update timeline
            //Modify the data source of tweets
            tweets.add(0, tweet)

            //Update adapter
            adapter.notifyItemInserted(0)
            rvTweets.smoothScrollToPosition(0)
        }
        super.onActivityResult(requestCode, resultCode, data)
    }

    fun populateHomeTimeline() {
        client.getHomeTimeline(object : JsonHttpResponseHandler() {
            override fun onSuccess(statusCode: Int, headers: Headers, json: JSON) {
                Log.i(TAG, "onSuccess: $json")

                val jsonArray = json.jsonArray

                try {
                    //clear out our currently fetched tweets
                    adapter.clear()
                    val listOfNewTweetsRetrieved = Tweet.fromJsonArray(jsonArray)
                    tweets.addAll(listOfNewTweetsRetrieved)
                    adapter.notifyDataSetChanged()

                    //Signal refreshing has finished
                    swipeContainer.isRefreshing = false
                } catch (e: JSONException) {
                    Log.e(TAG, "JSON Exception $e")
                }
            }

            override fun onFailure(
                statusCode: Int,
                headers: Headers?,
                response: String?,
                throwable: Throwable?
            ) {
                Log.i(TAG, "onFailure $statusCode $response")
            }
        }
        )
    }

    fun loadNextDataFromApi(offset: Int) {
        val params = RequestParams().also {
            it["count"] = "25"
            it["max_id"] = tweets[tweets.size - 1].id
        }
        client.getHomeTimeline(params = params, handler = object : JsonHttpResponseHandler() {
            override fun onSuccess(statusCode: Int, headers: Headers, json: JSON) {
                Log.i(TAG, "onSuccess: $json")

                val jsonArray = json.jsonArray

                try {
                    val listOfNewTweetsRetrieved = Tweet.fromJsonArray(jsonArray)
                    tweets.addAll(listOfNewTweetsRetrieved)
                    adapter.notifyItemRangeInserted(
                        tweets.size - listOfNewTweetsRetrieved.size,
                        listOfNewTweetsRetrieved.size
                    )

                } catch (e: JSONException) {
                    Log.e(TAG, "JSON Exception $e")
                }
            }

            override fun onFailure(
                statusCode: Int,
                headers: Headers?,
                response: String?,
                throwable: Throwable?
            ) {
                Log.i(TAG, "onFailure $statusCode $response")
            }
        }
        )

        // Send an API request to retrieve appropriate paginated data
        //  --> Send the request including an offset value (i.e `page`) as a query parameter.
        //  --> Deserialize and construct new model objects from the API response
        //  --> Append the new data objects to the existing set of items inside the array of items
        //  --> Notify the adapter of the new items made with `notifyItemRangeInserted()`
    }

    companion object {
        val TAG = "TimelineActivity"
        val REQUEST_CODE = 10
    }
}