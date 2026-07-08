package com.example.generator;

import android.content.Context;
import java.io.File;
import java.nio.charset.Charset;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import kotlin.Metadata;
import kotlin.collections.ArraysKt;
import kotlin.io.FilesKt;
import kotlin.jvm.functions.Function1;
import kotlin.jvm.internal.Intrinsics;
import kotlin.text.Charsets;
import kotlin.text.StringsKt;
import org.json.JSONArray;
import org.json.JSONObject;
/* compiled from: AlignmentCacheManager.kt */
@Metadata(d1 = {"\u0000D\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0002\b\u0003\n\u0002\u0010\t\n\u0000\n\u0002\u0010\u000e\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0004\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u0002\n\u0000\n\u0002\u0010 \n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0004\bÇ\u0002\u0018\u00002\u00020\u0001:\u0001\u001aB\t\b\u0002¢\u0006\u0004\b\u0002\u0010\u0003J\u0010\u0010\b\u001a\u00020\t2\u0006\u0010\n\u001a\u00020\u000bH\u0002J\u001a\u0010\f\u001a\u00020\u00072\b\u0010\r\u001a\u0004\u0018\u00010\u00072\u0006\u0010\u000e\u001a\u00020\u0007H\u0002J\"\u0010\u000f\u001a\u0004\u0018\u00010\u00102\u0006\u0010\n\u001a\u00020\u000b2\b\u0010\r\u001a\u0004\u0018\u00010\u00072\u0006\u0010\u000e\u001a\u00020\u0007JF\u0010\u0011\u001a\u00020\u00122\u0006\u0010\n\u001a\u00020\u000b2\b\u0010\r\u001a\u0004\u0018\u00010\u00072\u0006\u0010\u000e\u001a\u00020\u00072\f\u0010\u0013\u001a\b\u0012\u0004\u0012\u00020\u00150\u00142\f\u0010\u0016\u001a\b\u0012\u0004\u0012\u00020\u00170\u00142\b\u0010\u0018\u001a\u0004\u0018\u00010\tJ\u000e\u0010\u0019\u001a\u00020\u00122\u0006\u0010\n\u001a\u00020\u000bR\u000e\u0010\u0004\u001a\u00020\u0005X\u0082T¢\u0006\u0002\n\u0000R\u000e\u0010\u0006\u001a\u00020\u0007X\u0082T¢\u0006\u0002\n\u0000¨\u0006\u001b"}, d2 = {"Lcom/example/generator/AlignmentCacheManager;", "", "<init>", "()V", "CACHE_EXPIRY_MS", "", "CACHE_DIR_NAME", "", "getCacheDir", "Ljava/io/File;", "context", "Landroid/content/Context;", "generateKey", "mediaUrl", "text", "getCachedAlignment", "Lcom/example/generator/AlignmentCacheManager$CachedAlignment;", "putCachedAlignment", "", "wordSegments", "", "Lcom/example/generator/WordSegment;", "smartChunks", "Lcom/example/generator/SmartChunk;", "audioFile", "clearCache", "CachedAlignment", "app"}, k = 1, mv = {2, 2, 0}, xi = 48)
/* loaded from: /app/applet/classes5.dex */
public final class AlignmentCacheManager {
    public static final int $stable = 0;
    private static final String CACHE_DIR_NAME = "alignment_cache";
    private static final long CACHE_EXPIRY_MS = 3600000;
    public static final AlignmentCacheManager INSTANCE = new AlignmentCacheManager();

    private AlignmentCacheManager() {
    }

    /* compiled from: AlignmentCacheManager.kt */
    @Metadata(d1 = {"\u00002\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0000\n\u0002\u0010 \n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u000e\n\u0002\b\f\n\u0002\u0010\u000b\n\u0002\b\u0002\n\u0002\u0010\b\n\u0002\b\u0002\b\u0087\b\u0018\u00002\u00020\u0001B-\u0012\f\u0010\u0002\u001a\b\u0012\u0004\u0012\u00020\u00040\u0003\u0012\f\u0010\u0005\u001a\b\u0012\u0004\u0012\u00020\u00060\u0003\u0012\b\u0010\u0007\u001a\u0004\u0018\u00010\b¢\u0006\u0004\b\t\u0010\nJ\u000f\u0010\u0010\u001a\b\u0012\u0004\u0012\u00020\u00040\u0003HÆ\u0003J\u000f\u0010\u0011\u001a\b\u0012\u0004\u0012\u00020\u00060\u0003HÆ\u0003J\u000b\u0010\u0012\u001a\u0004\u0018\u00010\bHÆ\u0003J5\u0010\u0013\u001a\u00020\u00002\u000e\b\u0002\u0010\u0002\u001a\b\u0012\u0004\u0012\u00020\u00040\u00032\u000e\b\u0002\u0010\u0005\u001a\b\u0012\u0004\u0012\u00020\u00060\u00032\n\b\u0002\u0010\u0007\u001a\u0004\u0018\u00010\bHÆ\u0001J\u0013\u0010\u0014\u001a\u00020\u00152\b\u0010\u0016\u001a\u0004\u0018\u00010\u0001HÖ\u0003J\t\u0010\u0017\u001a\u00020\u0018HÖ\u0001J\t\u0010\u0019\u001a\u00020\bHÖ\u0001R\u0017\u0010\u0002\u001a\b\u0012\u0004\u0012\u00020\u00040\u0003¢\u0006\b\n\u0000\u001a\u0004\b\u000b\u0010\fR\u0017\u0010\u0005\u001a\b\u0012\u0004\u0012\u00020\u00060\u0003¢\u0006\b\n\u0000\u001a\u0004\b\r\u0010\fR\u0013\u0010\u0007\u001a\u0004\u0018\u00010\b¢\u0006\b\n\u0000\u001a\u0004\b\u000e\u0010\u000f¨\u0006\u001a"}, d2 = {"Lcom/example/generator/AlignmentCacheManager$CachedAlignment;", "", "wordSegments", "", "Lcom/example/generator/WordSegment;", "smartChunks", "Lcom/example/generator/SmartChunk;", "audioPath", "", "<init>", "(Ljava/util/List;Ljava/util/List;Ljava/lang/String;)V", "getWordSegments", "()Ljava/util/List;", "getSmartChunks", "getAudioPath", "()Ljava/lang/String;", "component1", "component2", "component3", "copy", "equals", "", "other", "hashCode", "", "toString", "app"}, k = 1, mv = {2, 2, 0}, xi = 48)
    /* loaded from: /app/applet/classes5.dex */
    public static final class CachedAlignment {
        public static final int $stable = 8;
        private final String audioPath;
        private final List<SmartChunk> smartChunks;
        private final List<WordSegment> wordSegments;

        /* JADX WARN: Multi-variable type inference failed */
        public static /* synthetic */ CachedAlignment copy$default(CachedAlignment cachedAlignment, List list, List list2, String str, int i, Object obj) {
            if ((i & 1) != 0) {
                list = cachedAlignment.wordSegments;
            }
            if ((i & 2) != 0) {
                list2 = cachedAlignment.smartChunks;
            }
            if ((i & 4) != 0) {
                str = cachedAlignment.audioPath;
            }
            return cachedAlignment.copy(list, list2, str);
        }

        public final List<WordSegment> component1() {
            return this.wordSegments;
        }

        public final List<SmartChunk> component2() {
            return this.smartChunks;
        }

        public final String component3() {
            return this.audioPath;
        }

        public final CachedAlignment copy(List<WordSegment> list, List<SmartChunk> list2, String str) {
            Intrinsics.checkNotNullParameter(list, "wordSegments");
            Intrinsics.checkNotNullParameter(list2, "smartChunks");
            return new CachedAlignment(list, list2, str);
        }

        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (obj instanceof CachedAlignment) {
                CachedAlignment cachedAlignment = (CachedAlignment) obj;
                return Intrinsics.areEqual(this.wordSegments, cachedAlignment.wordSegments) && Intrinsics.areEqual(this.smartChunks, cachedAlignment.smartChunks) && Intrinsics.areEqual(this.audioPath, cachedAlignment.audioPath);
            }
            return false;
        }

        public int hashCode() {
            return (((this.wordSegments.hashCode() * 31) + this.smartChunks.hashCode()) * 31) + (this.audioPath == null ? 0 : this.audioPath.hashCode());
        }

        public String toString() {
            List<WordSegment> list = this.wordSegments;
            List<SmartChunk> list2 = this.smartChunks;
            return "CachedAlignment(wordSegments=" + list + ", smartChunks=" + list2 + ", audioPath=" + this.audioPath + ")";
        }

        public CachedAlignment(List<WordSegment> list, List<SmartChunk> list2, String audioPath) {
            Intrinsics.checkNotNullParameter(list, "wordSegments");
            Intrinsics.checkNotNullParameter(list2, "smartChunks");
            this.wordSegments = list;
            this.smartChunks = list2;
            this.audioPath = audioPath;
        }

        public final List<WordSegment> getWordSegments() {
            return this.wordSegments;
        }

        public final List<SmartChunk> getSmartChunks() {
            return this.smartChunks;
        }

        public final String getAudioPath() {
            return this.audioPath;
        }
    }

    private final File getCacheDir(Context context) {
        File dir = new File(context.getCacheDir(), CACHE_DIR_NAME);
        if (!dir.exists()) {
            dir.mkdirs();
        }
        return dir;
    }

    private final String generateKey(String mediaUrl, String text) {
        String rawKey = (mediaUrl == null ? "" : mediaUrl) + "_" + text;
        MessageDigest messageDigest = MessageDigest.getInstance("SHA-256");
        byte[] bytes = rawKey.getBytes(Charsets.UTF_8);
        Intrinsics.checkNotNullExpressionValue(bytes, "getBytes(...)");
        byte[] bytes2 = messageDigest.digest(bytes);
        Intrinsics.checkNotNull(bytes2);
        return ArraysKt.joinToString$default(bytes2, "", (CharSequence) null, (CharSequence) null, 0, (CharSequence) null, new Function1() { // from class: com.example.generator.AlignmentCacheManager$$ExternalSyntheticLambda0
            public final Object invoke(Object obj) {
                return AlignmentCacheManager.generateKey$lambda$0(((Byte) obj).byteValue());
            }
        }, 30, (Object) null);
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public static final CharSequence generateKey$lambda$0(byte it) {
        String format = String.format("%02x", Arrays.copyOf(new Object[]{Byte.valueOf(it)}, 1));
        Intrinsics.checkNotNullExpressionValue(format, "format(...)");
        return format;
    }

    /* JADX WARN: Multi-variable type inference failed */
    public final CachedAlignment getCachedAlignment(Context context, String mediaUrl, String text) {
        CachedAlignment cachedAlignment;
        JSONObject jsonObj;
        List wordSegments;
        JSONArray wordsArray;
        int i;
        int length;
        Intrinsics.checkNotNullParameter(context, "context");
        Intrinsics.checkNotNullParameter(text, "text");
        String key = generateKey(mediaUrl, text);
        File cacheFile = new File(getCacheDir(context), key + ".json");
        CachedAlignment cachedAlignment2 = null;
        if (cacheFile.exists()) {
            if (System.currentTimeMillis() - cacheFile.lastModified() > CACHE_EXPIRY_MS) {
                cacheFile.delete();
                return null;
            }
            try {
                String jsonStr = FilesKt.readText$default(cacheFile, (Charset) null, 1, (Object) null);
                jsonObj = new JSONObject(jsonStr);
                wordSegments = new ArrayList();
                wordsArray = jsonObj.optJSONArray("wordSegments");
                if (wordsArray == null) {
                    wordsArray = new JSONArray();
                }
                i = 0;
                length = wordsArray.length();
            } catch (Exception e) {
                cachedAlignment = null;
            }
            while (true) {
                cachedAlignment = cachedAlignment2;
                if (i >= length) {
                    break;
                }
                try {
                    JSONObject obj = wordsArray.getJSONObject(i);
                    int i2 = obj.getInt("wordIndex");
                    String string = obj.getString("word");
                    Intrinsics.checkNotNullExpressionValue(string, "getString(...)");
                    wordSegments.add(new WordSegment(i2, obj.getLong("startTimeMs"), obj.getLong("endTimeMs"), string));
                    i++;
                    cachedAlignment2 = cachedAlignment;
                } catch (Exception e2) {
                }
                cacheFile.delete();
                return cachedAlignment;
            }
            List smartChunks = new ArrayList();
            JSONArray chunksArray = jsonObj.optJSONArray("smartChunks");
            if (chunksArray == null) {
                chunksArray = new JSONArray();
            }
            int i3 = 0;
            int length2 = chunksArray.length();
            while (i3 < length2) {
                JSONObject obj2 = chunksArray.getJSONObject(i3);
                JSONArray chunksArray2 = chunksArray;
                String string2 = obj2.getString("arabic");
                Intrinsics.checkNotNullExpressionValue(string2, "getString(...)");
                smartChunks.add(new SmartChunk(string2, obj2.has("english") ? obj2.getString("english") : cachedAlignment, obj2.getLong("startTimeMs"), obj2.getLong("endTimeMs")));
                i3++;
                chunksArray = chunksArray2;
            }
            String audioPath = jsonObj.has("audioPath") ? jsonObj.getString("audioPath") : cachedAlignment;
            if (audioPath != 0 && !new File(audioPath).exists()) {
                audioPath = 0;
            }
            return new CachedAlignment(wordSegments, smartChunks, audioPath);
        }
        return null;
    }

    public final void putCachedAlignment(Context context, String mediaUrl, String text, List<WordSegment> list, List<SmartChunk> list2, File audioFile) {
        Intrinsics.checkNotNullParameter(context, "context");
        Intrinsics.checkNotNullParameter(text, "text");
        Intrinsics.checkNotNullParameter(list, "wordSegments");
        Intrinsics.checkNotNullParameter(list2, "smartChunks");
        String key = generateKey(mediaUrl, text);
        File cacheFile = new File(getCacheDir(context), key + ".json");
        try {
            JSONArray wordsArray = new JSONArray();
            for (WordSegment wordSegment : list) {
                JSONObject jSONObject = new JSONObject();
                jSONObject.put("wordIndex", wordSegment.getWordIndex());
                jSONObject.put("word", wordSegment.getWord());
                jSONObject.put("startTimeMs", wordSegment.getStartTimeMs());
                jSONObject.put("endTimeMs", wordSegment.getEndTimeMs());
                wordsArray.put(jSONObject);
            }
            JSONArray chunksArray = new JSONArray();
            List<SmartChunk> list3 = list2;
            boolean z = false;
            for (SmartChunk smartChunk : list3) {
                JSONObject jSONObject2 = new JSONObject();
                Iterable iterable = list3;
                boolean z2 = z;
                jSONObject2.put("arabic", smartChunk.getArabic());
                if (smartChunk.getEnglish() != null) {
                    jSONObject2.put("english", smartChunk.getEnglish());
                }
                jSONObject2.put("startTimeMs", smartChunk.getStartTimeMs());
                jSONObject2.put("endTimeMs", smartChunk.getEndTimeMs());
                chunksArray.put(jSONObject2);
                list3 = iterable;
                z = z2;
            }
            JSONObject jsonObj = new JSONObject();
            jsonObj.put("wordSegments", wordsArray);
            jsonObj.put("smartChunks", chunksArray);
            if (audioFile != null && audioFile.exists()) {
                jsonObj.put("audioPath", audioFile.getAbsolutePath());
            }
            String jSONObject3 = jsonObj.toString();
            Intrinsics.checkNotNullExpressionValue(jSONObject3, "toString(...)");
            FilesKt.writeText$default(cacheFile, jSONObject3, (Charset) null, 2, (Object) null);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public final void clearCache(Context context) {
        Intrinsics.checkNotNullParameter(context, "context");
        try {
            File dir = getCacheDir(context);
            File[] listFiles = dir.listFiles();
            if (listFiles == null) {
                return;
            }
            for (File file : listFiles) {
                String name = file.getName();
                Intrinsics.checkNotNullExpressionValue(name, "getName(...)");
                if (StringsKt.endsWith$default(name, ".json", false, 2, (Object) null)) {
                    file.delete();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
