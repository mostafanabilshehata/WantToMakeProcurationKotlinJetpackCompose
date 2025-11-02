package com.informatique.tawsekmisr.utils

import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import com.scottyab.rootbeer.RootBeer
import java.io.BufferedReader
import java.io.File
import java.io.InputStreamReader
import java.net.Socket

/**
 * Utility class for detecting rooted devices, emulators, and Frida injection
 */
class RootUtil {

    fun isDeviceRooted(context: Context): Boolean {
        val rootBeer = RootBeer(context)
        return isRooted() ||
                checkForSuBinary() ||
                checkForDangerousProps() ||
                canExecuteSu() ||
                isRootedBySystemProps() ||
                isSuperUserAppInstalled(context) ||
                rootBeer.isRootedWithoutBusyBoxCheck ||
                rootBeer.isRooted ||
                isEmulator() ||
                isFridaServerRunning() ||
                detectFridaProcesses()
    }

    // ------------------- Root checks -------------------

    private fun isRooted(): Boolean {
        val paths = arrayOf(
            "/system/app/Superuser.apk",
            "/sbin/su",
            "/system/bin/su",
            "/system/xbin/su",
            "/data/local/xbin/su",
            "/data/local/bin/su",
            "/system/sd/xbin/su",
            "/system/bin/failsafe/su",
            "/data/local/su"
        )
        return paths.any { File(it).exists() }
    }

    private fun checkForSuBinary(): Boolean {
        val paths = arrayOf("/system/xbin/which", "/system/bin/which", "/sbin/which")
        for (path in paths) {
            try {
                val process = ProcessBuilder(path, "su")
                    .redirectErrorStream(true)
                    .start()
                val reader = BufferedReader(InputStreamReader(process.inputStream))
                val result = reader.readLine()
                if (result != null && result.contains("su")) {
                    return true
                }
            } catch (e: Exception) {
                // ignored
            }
        }
        return false
    }

    private fun checkForDangerousProps(): Boolean {
        val dangerousProps = arrayOf("ro.debuggable=1", "ro.secure=0")
        try {
            val process = Runtime.getRuntime().exec("getprop")
            val reader = BufferedReader(InputStreamReader(process.inputStream))
            var line: String?
            while (reader.readLine().also { line = it } != null) {
                for (prop in dangerousProps) {
                    if (line?.contains(prop) == true) {
                        return true
                    }
                }
            }
        } catch (e: Exception) {
            // ignored
        }
        return false
    }

    private fun canExecuteSu(): Boolean {
        return try {
            val process = Runtime.getRuntime().exec("su")
            process.outputStream.write("exit\n".toByteArray())
            process.waitFor()
            process.exitValue() == 0
        } catch (e: Exception) {
            false
        }
    }

    private fun isRootedBySystemProps(): Boolean {
        val debuggable = Build.TYPE
        val secure = Build.TAGS
        return debuggable.contains("eng") || secure == "test-keys"
    }

    private fun isSuperUserAppInstalled(context: Context): Boolean {
        val suApps = arrayOf("eu.chainfire.supersu", "com.topjohnwu.magisk")
        for (app in suApps) {
            try {
                val info = context.packageManager.getApplicationInfo(app, 0)
                if (info != null) {
                    return true
                }
            } catch (e: PackageManager.NameNotFoundException) {
                // ignored
            }
        }
        return false
    }

    private fun isEmulator(): Boolean {
        val fingerprint = Build.FINGERPRINT
        return fingerprint.startsWith("google/sdk_gphone") ||
                fingerprint.contains("generic") ||
                fingerprint.contains("emulator") ||
                fingerprint.contains("sdk")
    }

    /** Detect open Frida server ports (default 27042, 27043) */
    private fun isFridaServerRunning(): Boolean {
        val ports = arrayOf("27042", "27043")
        for (port in ports) {
            try {
                val socket = Socket("127.0.0.1", port.toInt())
                socket.close()
                return true // Frida server detected
            } catch (e: Exception) {
                // ignored
            }
        }
        return false
    }

    /** Detect suspicious processes (frida, objection, gadget) */
    private fun detectFridaProcesses(): Boolean {
        val indicators = arrayOf("frida", "objection", "gadget", "re.frida.server")
        try {
            val process = Runtime.getRuntime().exec("ps")
            val reader = BufferedReader(InputStreamReader(process.inputStream))
            var line: String?
            while (reader.readLine().also { line = it } != null) {
                for (keyword in indicators) {
                    if (line?.lowercase()?.contains(keyword) == true) {
                        return true
                    }
                }
            }
        } catch (e: Exception) {
            // ignored
        }
        return false
    }
}

