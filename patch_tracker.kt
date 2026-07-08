    private fun getAppLogcat(): String {
        return try {
            val pid = android.os.Process.myPid()
            val process = Runtime.getRuntime().exec("logcat -d -v threadtime -t 2000")
            val reader = java.io.BufferedReader(java.io.InputStreamReader(process.inputStream))
            val log = java.lang.StringBuilder()
            var line: String?
            val pidStr = pid.toString()
            while (reader.readLine().also { line = it } != null) {
                if (line!!.contains(pidStr)) {
                    log.append(line).append("\n")
                }
            }
            log.toString()
        } catch (e: Exception) {
            "Failed to get logcat: ${e.message}"
        }
    }
