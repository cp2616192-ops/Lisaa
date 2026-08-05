package com.khanu.lisaa.web

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.net.URL
import java.net.URLEncoder
import javax.net.ssl.HttpsURLConnection

class WebScraper(private val context: Context) {

    suspend fun searchWeb(query: String): String = withContext(Dispatchers.IO) {
        try {
            val encoded = URLEncoder.encode(query, "UTF-8")
            val url = URL("https://www.google.com/search?q=$encoded")
            val conn = url.openConnection() as HttpsURLConnection
            conn.connectTimeout = 5000
            conn.readTimeout = 5000
            val html = conn.inputStream.bufferedReader().use { it.readText() }
            // Extract title snippets (very simple)
            val snippets = html.split("<div class=\"BNeawe s3v9rd AP7Wnd\">")
                .drop(1)
                .map { it.substringBefore("</div>") }
                .filter { it.isNotBlank() }
                .take(3)
            return@withContext snippets.joinToString("\n")
        } catch (e: Exception) {
            return@withContext "Search failed: ${e.message}"
        }
    }

    suspend fun getWeather(city: String = "Delhi"): String = withContext(Dispatchers.IO) {
        try {
            val url = URL("https://wttr.in/$city?format=%C+%t")
            val conn = url.openConnection() as HttpsURLConnection
            conn.connectTimeout = 5000
            conn.readTimeout = 5000
            val response = conn.inputStream.bufferedReader().use { it.readText() }
            return@withContext "Weather in $city: $response"
        } catch (e: Exception) {
            return@withContext "Weather unavailable"
        }
    }

    suspend fun getNews(): String = withContext(Dispatchers.IO) {
        try {
            val url = URL("https://newsapi.org/v2/top-headlines?country=in&apiKey=demo") // Demo key
            val conn = url.openConnection() as HttpsURLConnection
            conn.connectTimeout = 5000
            conn.readTimeout = 5000
            val json = conn.inputStream.bufferedReader().use { it.readText() }
            val obj = org.json.JSONObject(json)
            val articles = obj.getJSONArray("articles")
            val builder = StringBuilder()
            for (i in 0 until minOf(3, articles.length())) {
                val title = articles.getJSONObject(i).getString("title")
                builder.append("• $title\n")
            }
            return@withContext builder.toString()
        } catch (e: Exception) {
            return@withContext "News unavailable"
        }
    }
}
