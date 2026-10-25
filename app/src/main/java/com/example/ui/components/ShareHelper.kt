package com.example.ui.components

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.widget.Toast
import java.net.URLEncoder

object ShareHelper {

    fun formatWishForSharing(wishText: String): String {
        return "$wishText\n\n— Sent via WishYa 🎂\nApp developed by Rao Muhammad Sarfraz"
    }

    fun shareToWhatsApp(context: Context, text: String) {
        val packages = listOf("com.whatsapp", "com.whatsapp.w4b")
        var installedPackage: String? = null

        val pm = context.packageManager
        for (pkg in packages) {
            try {
                pm.getPackageInfo(pkg, PackageManager.GET_ACTIVITIES)
                installedPackage = pkg
                break
            } catch (e: Exception) {
                // Not installed
            }
        }

        if (installedPackage != null) {
            try {
                val intent = Intent(Intent.ACTION_SEND).apply {
                    type = "text/plain"
                    setPackage(installedPackage)
                    putExtra(Intent.EXTRA_TEXT, text)
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                context.startActivity(intent)
                return
            } catch (e: Exception) {
                // Fallback below
            }
        }

        // Try uri scheme: whatsapp://send?text=...
        try {
            val encodedText = URLEncoder.encode(text, "UTF-8")
            val uriIntent = Intent(Intent.ACTION_VIEW, Uri.parse("whatsapp://send?text=$encodedText")).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(uriIntent)
            return
        } catch (e: Exception) {
            // WhatsApp app is not installed, fallback to web
        }

        // WhatsApp app is not installed on this device (e.g. emulator)
        // Open WhatsApp Web API via browser so message is pre-filled
        try {
            val encodedText = URLEncoder.encode(text, "UTF-8")
            val webIntent = Intent(Intent.ACTION_VIEW, Uri.parse("https://api.whatsapp.com/send?text=$encodedText")).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(webIntent)
            Toast.makeText(context, "Opening WhatsApp Web...", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            // General share fallback
            shareGeneral(context, text, "Share via WhatsApp")
        }
    }

    fun shareToWhatsAppNumber(context: Context, phoneNumber: String, text: String) {
        val cleanPhone = phoneNumber.replace("+", "").replace("-", "").replace(" ", "").trim()
        val encodedText = try {
            URLEncoder.encode(text, "UTF-8")
        } catch (e: Exception) {
            text
        }

        // Try direct WhatsApp URI with phone number
        try {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://api.whatsapp.com/send?phone=$cleanPhone&text=$encodedText")).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
            Toast.makeText(context, "Opening WhatsApp chat...", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            shareToWhatsApp(context, text)
        }
    }

    fun shareToMessenger(context: Context, text: String) {
        val packages = listOf("com.facebook.orca", "com.facebook.mlite")
        var targetPackage: String? = null

        val pm = context.packageManager
        for (pkg in packages) {
            try {
                pm.getPackageInfo(pkg, PackageManager.GET_ACTIVITIES)
                targetPackage = pkg
                break
            } catch (e: Exception) {
                // Not installed
            }
        }

        if (targetPackage != null) {
            try {
                val intent = Intent(Intent.ACTION_SEND).apply {
                    type = "text/plain"
                    setPackage(targetPackage)
                    putExtra(Intent.EXTRA_TEXT, text)
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                context.startActivity(intent)
                return
            } catch (e: Exception) {
                // Fallback below
            }
        }

        // Messenger not installed, try default SMS/MMS messaging app
        try {
            val smsIntent = Intent(Intent.ACTION_SENDTO).apply {
                data = Uri.parse("smsto:")
                putExtra("sms_body", text)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(smsIntent)
            Toast.makeText(context, "Messenger not found. Opening Messages...", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            shareGeneral(context, text, "Share Birthday Wish")
        }
    }

    fun shareGeneral(context: Context, text: String, title: String = "Share Birthday Wish via") {
        try {
            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_TEXT, text)
                putExtra(Intent.EXTRA_SUBJECT, "Birthday Wish")
            }
            val chooser = Intent.createChooser(shareIntent, title).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(chooser)
        } catch (e: Exception) {
            Toast.makeText(context, "Could not open share sheet", Toast.LENGTH_SHORT).show()
        }
    }

    fun copyToClipboard(context: Context, text: String) {
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText("WishYa Birthday Wish", text)
        clipboard.setPrimaryClip(clip)
        Toast.makeText(context, "Copied wish to clipboard! ✨", Toast.LENGTH_SHORT).show()
    }
}
