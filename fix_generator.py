with open('app/src/main/java/com/example/generator/VideoGenerator.kt', 'r') as f:
    text = f.read()

text = text.replace("""    }

        val url = "https://api.alquran.cloud/v1/ayah/$surah:$ayah/$edition\"""", """    }

    private fun fetchVerseInfo(surah: Int, ayah: Int, edition: String): Pair<String, Int> {
        val url = "https://api.alquran.cloud/v1/ayah/$surah:$ayah/$edition\"""")

with open('app/src/main/java/com/example/generator/VideoGenerator.kt', 'w') as f:
    f.write(text)
