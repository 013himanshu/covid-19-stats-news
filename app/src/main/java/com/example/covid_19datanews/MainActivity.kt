package com.example.covid_19datanews

import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.browser.customtabs.CustomTabsIntent
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import kotlinx.android.synthetic.main.activity_main.*
import org.json.JSONArray
import org.json.JSONObject

class MainActivity : AppCompatActivity(), NewsItemClicked {

    private lateinit var swipeRefreshLayout: SwipeRefreshLayout
    private lateinit var mAdapter: NewsListAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        //code for swipe to refresh
        try {
            swipeRefreshLayout = findViewById(R.id.TopParent)
            swipeRefreshLayout.setProgressBackgroundColorSchemeColor(Color.WHITE)
            swipeRefreshLayout.setColorSchemeColors(
                ContextCompat.getColor(
                    this,
                    R.color.colorPrimaryDark
                )
            )
            swipeRefreshLayout.setOnRefreshListener {
                swipeRefreshLayout.isRefreshing = false
            }
        } catch (e: Exception) {
            Log.i("swipeRefreshLayoutError", e.toString())
        }

        //function call to fetch all covid data (active, confirm, recovered, deaths)
        fetchStats()

        //recycler view code...
        recyclerView.layoutManager = LinearLayoutManager(this)
        fetchNews()
        mAdapter = NewsListAdapter(this)
        recyclerView.adapter = mAdapter

    }

    //function to show statistics with volley lib.
    private fun fetchStats() {
        // Instantiate the RequestQueue.

        val statsUrl = "https://api.covid19india.org/data.json"

        // Request a string response from the provided URL.
        val jsonObjectRequest = JsonObjectRequest(
            Request.Method.GET, statsUrl, null,
            Response.Listener { response ->
                val jsonArray: JSONArray = response.getJSONArray("statewise")
                var i = 0
                while (i < jsonArray.length()) {
                    val count: JSONObject = jsonArray.getJSONObject(i)
                    //fetch state total data for if condition
                    val total: String = count.getString("state")
                    if (total == "Total") {
                        //fetch confirmed cases
                        val confirm: String = count.getString("confirmed")
                        val confirm_txt: TextView = findViewById(R.id.val_confirm)
                        confirm_txt.text = confirm.toString()
                        //fetch active cases
                        val active: String = count.getString("active")
                        val active_txt: TextView = findViewById(R.id.val_active)
                        active_txt.text = active.toString()
                        //fetch recovered cases
                        val recover: String = count.getString("recovered")
                        val recover_txt: TextView = findViewById(R.id.val_recover)
                        recover_txt.text = recover.toString()
                        //fetch death cases
                        val death: String = count.getString("deaths")
                        val deaths_txt: TextView = findViewById(R.id.val_death)
                        deaths_txt.text = death.toString()

                        Log.i(
                            "Statistics:",
                            " CONFIRM " + confirm + " ACTIVE " + active + " RECOVERED " + recover + " DEATHS " + death
                        )
                        i++
                        break
                    } else {
                        break
                    }
                }
            },
            Response.ErrorListener { Log.i("ErrorVolley", "Something Went Wrong") }
        )

        // Add the request to the RequestQueue.
        MySingleton.getInstance(this).addToRequestQueue(jsonObjectRequest)
    }

    private fun fetchNews() {
        val newsUrl =
            "https://newsapi.org/v2/top-headlines?q=covid-19&country=in&apiKey=684ecc62ba79459e80da94b713732553"
        val jsonObjectRequest = object : JsonObjectRequest(
            Request.Method.GET, newsUrl, null,
            Response.Listener {
                val newsJsonArray = it.getJSONArray("articles")
                val newsArray = ArrayList<News>()
                for (i in 0 until newsJsonArray.length()) {
                    val newsJSONObject = newsJsonArray.getJSONObject(i)
                    val news = News(
                        newsJSONObject.getString("title"),
                        newsJSONObject.getString("url"),
                        newsJSONObject.getString("urlToImage")
                    )
                    newsArray.add(news)
                }
                mAdapter.updateNews(newsArray)
            },
            Response.ErrorListener { Log.i("ErrorVolley", "Something Went Wrong") }
        ) {
            override fun getHeaders(): MutableMap<String, String> {
                val headers = HashMap<String, String>()
                headers["User-Agent"] = "Mozilla/5.0"
                return headers
            }
        }
        MySingleton.getInstance(this).addToRequestQueue(jsonObjectRequest)
    }

    override fun onItemClicked(item: News) {
        val builder = CustomTabsIntent.Builder()
        builder.setToolbarColor(ContextCompat.getColor(this, R.color.colorPrimary))
        builder.addDefaultShareMenuItem()
        val customTabsIntent = builder.build()
        customTabsIntent.launchUrl(this, Uri.parse(item.url))
    }
}