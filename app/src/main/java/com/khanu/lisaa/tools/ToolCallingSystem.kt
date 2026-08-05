package com.khanu.lisaa.tools

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.provider.AlarmClock
import android.media.MediaPlayer
import android.app.AlarmManager
import android.app.PendingIntent
import java.io.File

class ToolCallingSystem(private val context: Context) {

    private val tools = mutableMapOf<String, (Map<String, Any>) -> String?>()

    init {
        registerTools()
    }

    private fun registerTools() {
        tools["open_app"] = { params -> openApp(params) }
        tools["close_app"] = { params -> closeApp(params) }
        tools["install_app"] = { params -> installApp(params) }
        tools["uninstall_app"] = { params -> uninstallApp(params) }
        tools["send_whatsapp"] = { params -> sendWhatsApp(params) }
        tools["send_sms"] = { params -> sendSms(params) }
        tools["make_call"] = { params -> makeCall(params) }
        tools["read_notifications"] = { params -> readNotifications(params) }
        tools["send_email"] = { params -> sendEmail(params) }
        tools["toggle_wifi"] = { params -> toggleWifi(params) }
        tools["toggle_bluetooth"] = { params -> toggleBluetooth(params) }
        tools["toggle_dnd"] = { params -> toggleDnd(params) }
        tools["set_volume"] = { params -> setVolume(params) }
        tools["set_brightness"] = { params -> setBrightness(params) }
        tools["list_files"] = { params -> listFiles(params) }
        tools["read_file"] = { params -> readFile(params) }
        tools["write_file"] = { params -> writeFile(params) }
        tools["delete_file"] = { params -> deleteFile(params) }
        tools["move_file"] = { params -> moveFile(params) }
        tools["copy_file"] = { params -> copyFile(params) }
        tools["play_music"] = { params -> playMusic(params) }
        tools["pause_music"] = { params -> pauseMusic(params) }
        tools["next_track"] = { params -> nextTrack(params) }
        tools["previous_track"] = { params -> previousTrack(params) }
        tools["take_photo"] = { params -> takePhoto(params) }
        tools["record_video"] = { params -> recordVideo(params) }
        tools["web_search"] = { params -> webSearch(params) }
        tools["scrape_website"] = { params -> scrapeWebsite(params) }
        tools["download_file"] = { params -> downloadFile(params) }
        tools["get_weather"] = { params -> getWeather(params) }
        tools["get_news"] = { params -> getNews(params) }
        tools["get_location"] = { params -> getLocation(params) }
        tools["get_battery"] = { params -> getBattery(params) }
        tools["set_alarm"] = { params -> setAlarm(params) }
        tools["set_timer"] = { params -> setTimer(params) }
        tools["set_reminder"] = { params -> setReminder(params) }
        tools["get_system_info"] = { params -> getSystemInfo(params) }
        tools["get_device_info"] = { params -> getDeviceInfo(params) }
        tools["run_shell_command"] = { params -> runShellCommand(params) }
        tools["open_url"] = { params -> openUrl(params) }
        tools["click_element"] = { params -> clickElement(params) }
        tools["type_text"] = { params -> typeText(params) }
        tools["scroll_screen"] = { params -> scrollScreen(params) }
        tools["take_screenshot"] = { params -> takeScreenshot(params) }
        tools["screen_recording"] = { params -> screenRecording(params) }
    }

    fun executeTool(toolName: String, params: Map<String, Any>): String? {
        val tool = tools[toolName]
        return tool?.invoke(params) ?: "Tool not found: $toolName"
    }

    fun listAllTools(): List<String> = tools.keys.toList()

    // -------------------- IMPLEMENTATIONS --------------------

    private fun openApp(params: Map<String, Any>): String {
        val packageName = params["package"] as? String ?: params["name"] as? String
        if (packageName == null) return "App name required"
        try {
            val intent = context.packageManager.getLaunchIntentForPackage(packageName)
            if (intent != null) {
                context.startActivity(intent)
                return "Opening $packageName"
            } else {
                val uri = Uri.parse("market://details?id=$packageName")
                context.startActivity(Intent(Intent.ACTION_VIEW, uri))
                return "App not installed. Opening Play Store."
            }
        } catch (e: Exception) {
            return "Error: ${e.message}"
        }
    }

    private fun closeApp(params: Map<String, Any>): String = "Requires accessibility service"
    private fun installApp(params: Map<String, Any>): String = "Requires file provider"
    private fun uninstallApp(params: Map<String, Any>): String {
        val packageName = params["package"] as? String ?: return "Package required"
        try {
            context.startActivity(Intent(Intent.ACTION_DELETE, Uri.parse("package:$packageName")))
            return "Uninstalling $packageName"
        } catch (e: Exception) {
            return "Error: ${e.message}"
        }
    }

    private fun sendWhatsApp(params: Map<String, Any>): String {
        val number = params["number"] as? String ?: return "Number required"
        val text = params["text"] as? String ?: ""
        try {
            context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://api.whatsapp.com/send?phone=$number&text=${Uri.encode(text)}")))
            return "Opening WhatsApp for $number"
        } catch (e: Exception) {
            return "WhatsApp not installed"
        }
    }

    private fun sendSms(params: Map<String, Any>): String {
        val number = params["number"] as? String ?: return "Number required"
        val text = params["text"] as? String ?: ""
        try {
            val intent = Intent(Intent.ACTION_SENDTO, Uri.parse("smsto:$number"))
            intent.putExtra("sms_body", text)
            context.startActivity(intent)
            return "Opening SMS for $number"
        } catch (e: Exception) {
            return "Error: ${e.message}"
        }
    }

    private fun makeCall(params: Map<String, Any>): String {
        val number = params["number"] as? String ?: return "Number required"
        try {
            context.startActivity(Intent(Intent.ACTION_CALL, Uri.parse("tel:$number")))
            return "Calling $number"
        } catch (e: Exception) {
            return "Call permission missing"
        }
    }

    private fun readNotifications(params: Map<String, Any>): String = "Requires accessibility service"
    private fun sendEmail(params: Map<String, Any>): String {
        val to = params["to"] as? String ?: return "Recipient required"
        val subject = params["subject"] as? String ?: ""
        val body = params["body"] as? String ?: ""
        try {
            val intent = Intent(Intent.ACTION_SENDTO, Uri.parse("mailto:$to"))
            intent.putExtra(Intent.EXTRA_SUBJECT, subject)
            intent.putExtra(Intent.EXTRA_TEXT, body)
            context.startActivity(intent)
            return "Opening email"
        } catch (e: Exception) {
            return "Email app not found"
        }
    }

    private fun toggleWifi(params: Map<String, Any>): String {
        val wifiManager = context.applicationContext.getSystemService(Context.WIFI_SERVICE) as android.net.wifi.WifiManager
        wifiManager.isWifiEnabled = !wifiManager.isWifiEnabled
        return "WiFi ${if (wifiManager.isWifiEnabled) "ON" else "OFF"}"
    }

    private fun toggleBluetooth(params: Map<String, Any>): String {
        val adapter = android.bluetooth.BluetoothAdapter.getDefaultAdapter()
        if (adapter == null) return "Bluetooth not supported"
        if (adapter.isEnabled) adapter.disable() else adapter.enable()
        return "Bluetooth ${if (adapter.isEnabled) "ON" else "OFF"}"
    }

    private fun toggleDnd(params: Map<String, Any>): String {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as android.app.NotificationManager
            if (nm.currentInterruptionFilter == android.app.NotificationManager.INTERRUPTION_FILTER_NONE) {
                nm.setInterruptionFilter(android.app.NotificationManager.INTERRUPTION_FILTER_ALL)
                return "DND OFF"
            } else {
                nm.setInterruptionFilter(android.app.NotificationManager.INTERRUPTION_FILTER_NONE)
                return "DND ON"
            }
        }
        return "Requires Android M+"
    }

    private fun setVolume(params: Map<String, Any>): String {
        val level = (params["level"] as? Number)?.toInt() ?: return "Level required"
        val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as android.media.AudioManager
        val max = audioManager.getStreamMaxVolume(android.media.AudioManager.STREAM_MUSIC)
        audioManager.setStreamVolume(android.media.AudioManager.STREAM_MUSIC, (level * max / 100).coerceIn(0, max), 0)
        return "Volume set to $level%"
    }

    private fun setBrightness(params: Map<String, Any>): String {
        val level = (params["level"] as? Number)?.toInt() ?: return "Level required"
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            Settings.System.putInt(context.contentResolver, Settings.System.SCREEN_BRIGHTNESS, (level * 255 / 100).coerceIn(0, 255))
            return "Brightness set to $level%"
        }
        return "Requires Android M+"
    }

    private fun listFiles(params: Map<String, Any>): String {
        val path = params["path"] as? String ?: "/sdcard"
        try {
            val files = File(path).listFiles() ?: return "Empty directory"
            return files.take(20).joinToString("\n") { "${it.name} (${if (it.isDirectory) "DIR" else "${it.length()}B"})" }
        } catch (e: Exception) {
            return "Error: ${e.message}"
        }
    }

    private fun readFile(params: Map<String, Any>): String {
        val path = params["path"] as? String ?: return "Path required"
        try {
            return File(path).readText().take(500)
        } catch (e: Exception) {
            return "Error: ${e.message}"
        }
    }

    private fun writeFile(params: Map<String, Any>): String {
        val path = params["path"] as? String ?: return "Path required"
        val content = params["content"] as? String ?: return "Content required"
        try {
            File(path).writeText(content)
            return "Written to $path"
        } catch (e: Exception) {
            return "Error: ${e.message}"
        }
    }

    private fun deleteFile(params: Map<String, Any>): String {
        val path = params["path"] as? String ?: return "Path required"
        return if (File(path).delete()) "Deleted $path" else "Failed to delete"
    }

    private fun moveFile(params: Map<String, Any>): String {
        val from = params["from"] as? String ?: return "From required"
        val to = params["to"] as? String ?: return "To required"
        return if (File(from).renameTo(File(to))) "Moved to $to" else "Failed to move"
    }

    private fun copyFile(params: Map<String, Any>): String {
        val from = params["from"] as? String ?: return "From required"
        val to = params["to"] as? String ?: return "To required"
        try {
            File(from).copyTo(File(to), true)
            return "Copied to $to"
        } catch (e: Exception) {
            return "Error: ${e.message}"
        }
    }

    private fun playMusic(params: Map<String, Any>): String {
        val url = params["url"] as? String ?: return "URL required"
        try {
            MediaPlayer().apply { setDataSource(url); prepare(); start() }
            return "Playing music"
        } catch (e: Exception) {
            return "Error playing: ${e.message}"
        }
    }

    private fun pauseMusic(params: Map<String, Any>): String = "Requires media player management"
    private fun nextTrack(params: Map<String, Any>): String = "Requires media player management"
    private fun previousTrack(params: Map<String, Any>): String = "Requires media player management"
    private fun takePhoto(params: Map<String, Any>): String {
        context.startActivity(Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE))
        return "Opening camera"
    }
    private fun recordVideo(params: Map<String, Any>): String {
        context.startActivity(Intent(android.provider.MediaStore.ACTION_VIDEO_CAPTURE))
        return "Opening video camera"
    }

    private fun webSearch(params: Map<String, Any>): String {
        val query = params["query"] as? String ?: return "Query required"
        context.startActivity(Intent(Intent.ACTION_WEB_SEARCH).putExtra(android.app.SearchManager.QUERY, query))
        return "Searching for $query"
    }

    private fun scrapeWebsite(params: Map<String, Any>): String {
        val url = params["url"] as? String ?: return "URL required"
        try {
            return java.net.URL(url).readText().take(500)
        } catch (e: Exception) {
            return "Error scraping: ${e.message}"
        }
    }

    private fun downloadFile(params: Map<String, Any>): String {
        val url = params["url"] as? String ?: return "URL required"
        try {
            val data = java.net.URL(url).readBytes()
            File("/sdcard/Download/${System.currentTimeMillis()}.tmp").writeBytes(data)
            return "Downloaded file"
        } catch (e: Exception) {
            return "Error: ${e.message}"
        }
    }

    private fun getWeather(params: Map<String, Any>): String = "Weather requires API key"
    private fun getNews(params: Map<String, Any>): String = "News requires API key"
    private fun getLocation(params: Map<String, Any>): String = "Location requires permission"
    private fun getBattery(params: Map<String, Any>): String {
        val bm = context.getSystemService(Context.BATTERY_SERVICE) as android.os.BatteryManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            return "Battery: ${bm.getIntProperty(android.os.BatteryManager.BATTERY_PROPERTY_CAPACITY)}%"
        }
        return "Battery info unavailable"
    }

    private fun setAlarm(params: Map<String, Any>): String {
        val hour = (params["hour"] as? Number)?.toInt() ?: return "Hour required"
        val minute = (params["minute"] as? Number)?.toInt() ?: return "Minute required"
        context.startActivity(Intent(AlarmClock.ACTION_SET_ALARM).apply {
            putExtra(AlarmClock.EXTRA_HOUR, hour)
            putExtra(AlarmClock.EXTRA_MINUTES, minute)
        })
        return "Alarm set for $hour:$minute"
    }

    private fun setTimer(params: Map<String, Any>): String {
        val minutes = (params["minutes"] as? Number)?.toInt() ?: return "Minutes required"
        context.startActivity(Intent(AlarmClock.ACTION_SET_TIMER).putExtra(AlarmClock.EXTRA_LENGTH, minutes * 60))
        return "Timer set for $minutes minutes"
    }

    private fun setReminder(params: Map<String, Any>): String {
        val text = params["text"] as? String ?: return "Text required"
        val time = (params["time"] as? Number)?.toLong() ?: return "Time required"
        val am = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, ReminderReceiver::class.java).putExtra("text", text)
        val pi = PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
        am.setExact(AlarmManager.RTC_WAKEUP, time, pi)
        return "Reminder set: $text"
    }

    private fun getSystemInfo(params: Map<String, Any>): String {
        return "Android ${Build.VERSION.RELEASE} (${Build.VERSION.SDK_INT})\n${Build.MANUFACTURER} ${Build.MODEL}"
    }
    private fun getDeviceInfo(params: Map<String, Any>): String {
        return "CPU: ${Build.HARDWARE}\nABI: ${Build.CPU_ABI}"
    }
    private fun runShellCommand(params: Map<String, Any>): String {
        val cmd = params["command"] as? String ?: return "Command required"
        try {
            val process = Runtime.getRuntime().exec(cmd)
            return process.inputStream.bufferedReader().readText().ifEmpty { "Command executed" }
        } catch (e: Exception) {
            return "Error: ${e.message}"
        }
    }
    private fun openUrl(params: Map<String, Any>): String {
        val url = params["url"] as? String ?: return "URL required"
        context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
        return "Opening URL"
    }

    private fun clickElement(params: Map<String, Any>): String = "Requires Accessibility Service"
    private fun typeText(params: Map<String, Any>): String = "Requires Accessibility Service"
    private fun scrollScreen(params: Map<String, Any>): String = "Requires Accessibility Service"
    private fun takeScreenshot(params: Map<String, Any>): String = "Requires MediaProjection"
    private fun screenRecording(params: Map<String, Any>): String = "Requires MediaProjection"
}
