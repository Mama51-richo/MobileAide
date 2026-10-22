package com.mobileaide

import org.json.JSONObject
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL

class BackendClient(private val endpoint: String) {

    suspend fun sendEvent(event: EventRecord, token: String?): Boolean {
        return try {
            val payload = JSONObject().apply {
                put("id", event.id)
                put("timestamp", event.timestamp)
                put("type", event.type)
                put("confidence", event.confidence)
                put("detail", event.detail)
            }

            val connection = URL(endpoint).openConnection() as HttpURLConnection
            connection.requestMethod = "POST"
            connection.setRequestProperty("Content-Type", "application/json")
            connection.setRequestProperty("Accept", "application/json")
            if (!token.isNullOrBlank()) {
                connection.setRequestProperty("Authorization", "Bearer $token")
            }
            connection.doOutput = true
            connection.connectTimeout = 10_000
            connection.readTimeout = 10_000

            connection.outputStream.use { outputStream ->
                OutputStreamWriter(outputStream, Charsets.UTF_8).use { writer ->
                    writer.write(payload.toString())
                    writer.flush()
                }
            }

            val responseCode = connection.responseCode
            responseCode in 200..299
        } catch (_: Exception) {
            false
        }
    }
}
