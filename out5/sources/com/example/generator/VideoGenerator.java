package com.example.generator;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Typeface;
import android.media.Image;
import android.net.Uri;
import com.example.service.VideoGenerationService;
import java.io.File;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.ShortBuffer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.ListIterator;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.CancellationException;
import java.util.concurrent.TimeUnit;
import kotlin.Metadata;
import kotlin.Pair;
import kotlin.TuplesKt;
import kotlin.Unit;
import kotlin.collections.CollectionsKt;
import kotlin.collections.MapsKt;
import kotlin.comparisons.ComparisonsKt;
import kotlin.coroutines.Continuation;
import kotlin.coroutines.intrinsics.IntrinsicsKt;
import kotlin.jvm.functions.Function1;
import kotlin.jvm.functions.Function2;
import kotlin.jvm.internal.Intrinsics;
import kotlin.ranges.RangesKt;
import kotlin.text.Regex;
import kotlin.text.StringsKt;
import kotlinx.coroutines.BuildersKt;
import kotlinx.coroutines.Dispatchers;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;
import org.json.JSONArray;
import org.json.JSONObject;
/* compiled from: VideoGenerator.kt */
@Metadata(d1 = {"\u0000®\u0001\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u0003\n\u0000\n\u0002\u0010%\n\u0002\u0010\u000e\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0010$\n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0010\u0002\n\u0002\b\u0002\n\u0002\u0010\b\n\u0002\b\u0006\n\u0002\u0010\u000b\n\u0002\b\u0007\n\u0002\u0018\u0002\n\u0002\u0010\u0007\n\u0000\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\b\u0004\n\u0002\u0018\u0002\n\u0002\b\u0004\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0010 \n\u0002\u0018\u0002\n\u0002\u0010\t\n\u0002\b\r\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0002\b$\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0002\b\t\n\u0002\u0018\u0002\n\u0002\b\u0010\b\u0007\u0018\u00002\u00020\u0001B\u0007¢\u0006\u0004\b\u0002\u0010\u0003J\u0018\u0010\u0010\u001a\u00020\u000b2\u0006\u0010\u0011\u001a\u00020\u00122\u0006\u0010\u0013\u001a\u00020\nH\u0002J\u0018\u0010\u0014\u001a\u00020\u000b2\u0006\u0010\u0011\u001a\u00020\u00122\u0006\u0010\u0013\u001a\u00020\nH\u0002J\u0006\u0010\u0015\u001a\u00020\u0016J\u0010\u0010\u0017\u001a\u00020\n2\u0006\u0010\u0018\u001a\u00020\u0019H\u0002J¼\u0001\u0010\u001a\u001a\u00020\u00162\u0006\u0010\u0011\u001a\u00020\u00122\u0006\u0010\u001b\u001a\u00020\u00192\u0006\u0010\u001c\u001a\u00020\u00192\u0006\u0010\u001d\u001a\u00020\u00192\u0006\u0010\u001e\u001a\u00020\n2\u0006\u0010\u001f\u001a\u00020 2\u0006\u0010!\u001a\u00020\n2\b\b\u0002\u0010\"\u001a\u00020\n2\b\b\u0002\u0010#\u001a\u00020 2\b\b\u0002\u0010$\u001a\u00020 2\b\b\u0002\u0010%\u001a\u00020 2\n\b\u0002\u0010&\u001a\u0004\u0018\u00010\n2\u0018\u0010'\u001a\u0014\u0012\u0004\u0012\u00020\n\u0012\u0004\u0012\u00020)\u0012\u0004\u0012\u00020\u00160(2\u0012\u0010*\u001a\u000e\u0012\u0004\u0012\u00020,\u0012\u0004\u0012\u00020\u00160+2\u0012\u0010-\u001a\u000e\u0012\u0004\u0012\u00020\n\u0012\u0004\u0012\u00020\u00160+H\u0086@¢\u0006\u0002\u0010.J\u0010\u0010/\u001a\u00020\n2\u0006\u0010\u001b\u001a\u00020\u0019H\u0002J2\u00100\u001a\u0014\u0012\u0004\u0012\u00020\n\u0012\u0004\u0012\u00020\u0019\u0012\u0004\u0012\u00020\n012\u0006\u0010\u001b\u001a\u00020\u00192\u0006\u0010\u0018\u001a\u00020\u00192\u0006\u00102\u001a\u00020\nH\u0002J\u0018\u00103\u001a\u00020\u00162\u0006\u00104\u001a\u00020\n2\u0006\u00105\u001a\u000206H\u0002J\b\u00107\u001a\u00020\u0016H\u0002JG\u00108\u001a\u0014\u0012\u0010\u0012\u000e\u0012\u0004\u0012\u00020;\u0012\u0004\u0012\u00020)0:092\u0006\u0010<\u001a\u00020\n2\u0006\u0010=\u001a\u00020\n2\n\b\u0002\u0010>\u001a\u0004\u0018\u00010;2\n\b\u0002\u0010?\u001a\u0004\u0018\u00010;H\u0002¢\u0006\u0002\u0010@J:\u0010A\u001a\u00020)2\u0006\u0010B\u001a\u00020\u00192\u0006\u0010C\u001a\u00020\u00192\u0006\u0010D\u001a\u00020;2\u0018\u0010E\u001a\u0014\u0012\u0010\u0012\u000e\u0012\u0004\u0012\u00020;\u0012\u0004\u0012\u00020)0:09H\u0002JP\u0010F\u001a\u00020\n2\u0006\u0010G\u001a\u00020\n2\u0006\u0010B\u001a\u00020\u00192\u0006\u0010C\u001a\u00020\u00192\u0006\u0010D\u001a\u00020;2\u0018\u0010E\u001a\u0014\u0012\u0010\u0012\u000e\u0012\u0004\u0012\u00020;\u0012\u0004\u0012\u00020)0:092\f\u0010H\u001a\b\u0012\u0004\u0012\u00020I09H\u0002J\\\u0010J\u001a\u0004\u0018\u00010\n2\b\u0010K\u001a\u0004\u0018\u00010\n2\u0006\u0010G\u001a\u00020\n2\u0006\u0010B\u001a\u00020\u00192\u0006\u0010C\u001a\u00020\u00192\u0006\u0010D\u001a\u00020;2\u0018\u0010E\u001a\u0014\u0012\u0010\u0012\u000e\u0012\u0004\u0012\u00020;\u0012\u0004\u0012\u00020)0:092\f\u0010H\u001a\b\u0012\u0004\u0012\u00020I09H\u0002JÀ\u0002\u0010L\u001a\u00020M2\u0006\u0010N\u001a\u00020\n2\u0006\u0010G\u001a\u00020\n2\b\u0010K\u001a\u0004\u0018\u00010\n2\b\u0010O\u001a\u0004\u0018\u00010M2\u0006\u0010\u0011\u001a\u00020\u00122\u0006\u0010P\u001a\u00020\n2\u0006\u0010Q\u001a\u00020\n2\u0006\u0010R\u001a\u00020\u00192\u0006\u0010S\u001a\u00020\n2\u0006\u0010T\u001a\u00020)2\u0006\u0010U\u001a\u00020 2\u0006\u0010V\u001a\u00020\n2\u0006\u0010W\u001a\u00020)2\u0006\u0010X\u001a\u00020\u00192\u0006\u0010Y\u001a\u00020\n2\u0006\u0010Z\u001a\u00020\n2\u0006\u0010[\u001a\u00020\n2\u0006\u0010\\\u001a\u00020\u00192\u0006\u0010]\u001a\u00020\n2\u0006\u0010^\u001a\u00020\n2\u0006\u0010_\u001a\u00020\n2\u0006\u0010`\u001a\u00020\u00192\u0006\u0010a\u001a\u00020\u00192\u0006\u0010b\u001a\u00020\u00192\u0006\u0010c\u001a\u00020\u00192\u0006\u0010d\u001a\u00020\u00192\u0006\u0010e\u001a\u00020\n2\u0006\u0010f\u001a\u00020)2\u0006\u0010g\u001a\u00020\u00192\u0006\u0010h\u001a\u00020\u00192\u0006\u0010i\u001a\u00020)2\u0006\u0010j\u001a\u00020\u00192\u0006\u0010k\u001a\u00020\u00192\u0006\u0010l\u001a\u00020;2\u0006\u0010m\u001a\u00020;2\u0006\u0010$\u001a\u00020 2\b\b\u0002\u0010n\u001a\u00020\u00192\b\b\u0002\u0010o\u001a\u00020\u0019H\u0002J\u0018\u0010p\u001a\u00020\u00162\u0006\u0010q\u001a\u00020r2\u0006\u0010s\u001a\u00020MH\u0002J\u0010\u0010t\u001a\u00020\u00192\u0006\u0010\u001e\u001a\u00020\nH\u0002J@\u0010u\u001a\u00020v2\u0006\u0010w\u001a\u00020v2\u0006\u0010x\u001a\u00020\u00192\u0006\u0010y\u001a\u00020\u00192\u0006\u0010z\u001a\u00020\u00192\u0006\u0010{\u001a\u00020\u00192\u0006\u0010|\u001a\u00020\u00192\u0006\u0010}\u001a\u00020\u0019H\u0002J\u0010\u0010~\u001a\u00020\n2\u0006\u0010G\u001a\u00020\nH\u0002JG\u0010\u007f\u001a\u001b\u0012\n\u0012\b\u0012\u0004\u0012\u00020I09\u0012\u000b\u0012\t\u0012\u0005\u0012\u00030\u0080\u0001090:2\u0006\u0010\u0011\u001a\u00020\u00122\t\u0010\u0081\u0001\u001a\u0004\u0018\u0001062\t\u0010\u0082\u0001\u001a\u0004\u0018\u00010\n2\u0006\u0010G\u001a\u00020\nH\u0002J8\u0010\u0083\u0001\u001a\n\u0012\u0004\u0012\u00020\n\u0018\u0001092\u0006\u0010\u0011\u001a\u00020\u00122\r\u0010\u0084\u0001\u001a\b\u0012\u0004\u0012\u00020\n092\u0007\u0010\u0085\u0001\u001a\u00020\nH\u0082@¢\u0006\u0003\u0010\u0086\u0001JZ\u0010\u0087\u0001\u001a\t\u0012\u0005\u0012\u00030\u0080\u0001092\u0006\u0010\u0011\u001a\u00020\u00122\u0007\u0010\u0088\u0001\u001a\u00020\n2\t\u0010\u0089\u0001\u001a\u0004\u0018\u00010\n2\f\u0010H\u001a\b\u0012\u0004\u0012\u00020I092\u000e\u0010\u008a\u0001\u001a\t\u0012\u0005\u0012\u00030\u0080\u0001092\u0007\u0010\u008b\u0001\u001a\u00020;H\u0082@¢\u0006\u0003\u0010\u008c\u0001J%\u0010\u008d\u0001\u001a\u0005\u0018\u00010\u0080\u00012\u000e\u0010\u008e\u0001\u001a\t\u0012\u0005\u0012\u00030\u0080\u0001092\u0007\u0010\u008f\u0001\u001a\u00020;H\u0002R\u000e\u0010\u0004\u001a\u00020\u0005X\u0082\u0004¢\u0006\u0002\n\u0000R\u0010\u0010\u0006\u001a\u0004\u0018\u00010\u0007X\u0082\u000e¢\u0006\u0002\n\u0000R\u001a\u0010\b\u001a\u000e\u0012\u0004\u0012\u00020\n\u0012\u0004\u0012\u00020\u000b0\tX\u0082\u000e¢\u0006\u0002\n\u0000R\u001a\u0010\f\u001a\u000e\u0012\u0004\u0012\u00020\n\u0012\u0004\u0012\u00020\u000b0\tX\u0082\u000e¢\u0006\u0002\n\u0000R\u001a\u0010\r\u001a\u000e\u0012\u0004\u0012\u00020\n\u0012\u0004\u0012\u00020\n0\u000eX\u0082\u0004¢\u0006\u0002\n\u0000R\u001a\u0010\u000f\u001a\u000e\u0012\u0004\u0012\u00020\n\u0012\u0004\u0012\u00020\n0\u000eX\u0082\u0004¢\u0006\u0002\n\u0000¨\u0006\u0090\u0001"}, d2 = {"Lcom/example/generator/VideoGenerator;", "", "<init>", "()V", "client", "Lokhttp3/OkHttpClient;", "threadError", "", "customArabicTypefaces", "", "", "Landroid/graphics/Typeface;", "customEnglishTypefaces", "arabicFontUrls", "", "englishFontUrls", "getArabicTypeface", "context", "Landroid/content/Context;", "fontName", "getEnglishTypeface", "cancelNetworkRequests", "", "formatAyahSymbol", "ayah", "", "generateReel", "surah", "startAyah", "endAyah", "reciterId", "showTranslation", "", "pexelsApiKey", "videoQuality", "isRetry", "isPreviewMode", "includeBasmalah", "videoQuery", "onProgress", "Lkotlin/Function2;", "", "onComplete", "Lkotlin/Function1;", "Landroid/net/Uri;", "onError", "(Landroid/content/Context;IIILjava/lang/String;ZLjava/lang/String;Ljava/lang/String;ZZZLjava/lang/String;Lkotlin/jvm/functions/Function2;Lkotlin/jvm/functions/Function1;Lkotlin/jvm/functions/Function1;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "fetchFullSurahText", "fetchVerseInfo", "Lkotlin/Triple;", "edition", "downloadAudio", "url", "destFile", "Ljava/io/File;", "checkCancellationAndPause", "transcodeMp3ToAac", "", "Lkotlin/Pair;", "", "inputPath", "outputPath", "extractStartUs", "extractEndUs", "(Ljava/lang/String;Ljava/lang/String;Ljava/lang/Long;Ljava/lang/Long;)Ljava/util/List;", "getCumulativeEnergyRatio", "currentFrame", "totalFrames", "durationUs", "timeline", "getActiveTextChunk", "text", "wordSegments", "Lcom/example/generator/WordSegment;", "getActiveTranslationChunk", "translation", "createVerseBitmap", "Landroid/graphics/Bitmap;", "surahName", "bgBitmap", "fontFamily", "fontWeight", "textFontSize", "textColorStr", "textOpacity", "showTextBg", "textBgColorStr", "textBgOpacity", "textBgRadius", "textPosition", "textAlign", "textAnimationType", "translationFontSize", "translationColorStr", "translationFontFamily", "translationFontWeight", "translationTextX", "translationTextY", "arabicTextX", "arabicTextY", "surahNameFontSize", "surahNameColorStr", "surahNameOpacity", "surahNameX", "surahNameY", "iconOpacity", "iconX", "iconY", "frameIndex", "chunkTimeMs", "videoWidth", "videoHeight", "fillImageFromBitmap", "image", "Landroid/media/Image;", "bitmap", "mapReciterIdToQuranComId", "resamplePCM", "Ljava/nio/ByteBuffer;", "inputBuf", "inputSize", "inputOffset", "srcSampleRate", "srcChannels", "dstSampleRate", "dstChannels", "cleanArabicForWhisper", "alignWithWhisperX", "Lcom/example/generator/SmartChunk;", "audioFile", "mediaUrl", "alignTranslationWithGemini", "arabicChunks", "fullTranslation", "(Landroid/content/Context;Ljava/util/List;Ljava/lang/String;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "getSmartChunks", "arabicText", "englishText", "whisperXChunks", "durationMs", "(Landroid/content/Context;Ljava/lang/String;Ljava/lang/String;Ljava/util/List;Ljava/util/List;JLkotlin/coroutines/Continuation;)Ljava/lang/Object;", "getActiveSmartChunk", "chunks", "currentTimeMs", "app"}, k = 1, mv = {2, 2, 0}, xi = 48)
/* loaded from: /app/applet/classes5.dex */
public final class VideoGenerator {
    public static final int $stable = 8;
    private volatile Throwable threadError;
    private final OkHttpClient client = new OkHttpClient.Builder().connectTimeout(1800, TimeUnit.SECONDS).readTimeout(1800, TimeUnit.SECONDS).writeTimeout(1800, TimeUnit.SECONDS).addInterceptor(new Interceptor() { // from class: com.example.generator.VideoGenerator$special$$inlined$-addInterceptor$1
        public final Response intercept(Interceptor.Chain chain) {
            Intrinsics.checkNotNullParameter(chain, "chain");
            Request request = chain.request();
            return chain.proceed(request.newBuilder().header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36").header("Accept", "*/*").method(request.method(), request.body()).build());
        }
    }).build();
    private Map<String, Typeface> customArabicTypefaces = new LinkedHashMap();
    private Map<String, Typeface> customEnglishTypefaces = new LinkedHashMap();
    private final Map<String, String> arabicFontUrls = MapsKt.mapOf(new Pair[]{TuplesKt.to("Amiri", "https://github.com/google/fonts/raw/main/ofl/amiriquran/AmiriQuran-Regular.ttf"), TuplesKt.to("Cairo", "https://github.com/google/fonts/raw/main/ofl/cairo/static/Cairo-Bold.ttf"), TuplesKt.to("Scheherazade New", "https://github.com/google/fonts/raw/main/ofl/scheherazadenew/ScheherazadeNew-Bold.ttf"), TuplesKt.to("Lateef", "https://github.com/google/fonts/raw/main/ofl/lateef/Lateef-Regular.ttf"), TuplesKt.to("Reem Kufi", "https://github.com/google/fonts/raw/main/ofl/reemkufi/static/ReemKufi-Bold.ttf")});
    private final Map<String, String> englishFontUrls = MapsKt.mapOf(new Pair[]{TuplesKt.to("Montserrat", "https://github.com/google/fonts/raw/main/ofl/montserrat/static/Montserrat-Medium.ttf"), TuplesKt.to("Roboto", "https://github.com/google/fonts/raw/main/ofl/roboto/static/Roboto-Medium.ttf"), TuplesKt.to("Playfair", "https://github.com/google/fonts/raw/main/ofl/playfairdisplay/static/PlayfairDisplay-Italic.ttf"), TuplesKt.to("Lato", "https://github.com/google/fonts/raw/main/ofl/lato/Lato-Regular.ttf")});

    private final Typeface getArabicTypeface(Context context, String fontName) {
        Typeface tf;
        Typeface typeface = this.customArabicTypefaces.get(fontName);
        if (typeface == null) {
            try {
                if (StringsKt.startsWith$default(fontName, "/", false, 2, (Object) null)) {
                    try {
                        File file = new File(fontName);
                        if (file.exists() && file.length() > 0) {
                            Typeface tf2 = Typeface.createFromFile(file);
                            this.customArabicTypefaces.put(fontName, tf2);
                            Intrinsics.checkNotNull(tf2);
                            return tf2;
                        }
                    } catch (Exception e) {
                        Typeface create = Typeface.create("serif", 1);
                        Intrinsics.checkNotNull(create);
                        return create;
                    }
                }
            } catch (Exception e2) {
            }
            try {
                String fileName = StringsKt.replace$default(fontName, " ", "", false, 4, (Object) null) + ".ttf";
                File file2 = new File(context.getCacheDir(), fileName);
                if (file2.exists() && file2.length() > 1000) {
                    tf = Typeface.createFromFile(file2);
                    this.customArabicTypefaces.put(fontName, tf);
                } else {
                    tf = Typeface.create("serif", 1);
                }
                Intrinsics.checkNotNull(tf);
                return tf;
            } catch (Exception e3) {
                Typeface create2 = Typeface.create("serif", 1);
                Intrinsics.checkNotNull(create2);
                return create2;
            }
        }
        return typeface;
    }

    private final Typeface getEnglishTypeface(Context context, String fontName) {
        Typeface tf;
        Typeface typeface = this.customEnglishTypefaces.get(fontName);
        if (typeface != null) {
            return typeface;
        }
        try {
            if (StringsKt.startsWith$default(fontName, "/", false, 2, (Object) null)) {
                try {
                    File file = new File(fontName);
                    if (file.exists() && file.length() > 0) {
                        Typeface tf2 = Typeface.createFromFile(file);
                        this.customEnglishTypefaces.put(fontName, tf2);
                        Intrinsics.checkNotNull(tf2);
                        return tf2;
                    }
                } catch (Exception e) {
                    Typeface create = Typeface.create("sans-serif-medium", 0);
                    Intrinsics.checkNotNull(create);
                    return create;
                }
            }
            try {
                String fileName = "EN_" + StringsKt.replace$default(fontName, " ", "", false, 4, (Object) null) + ".ttf";
                File file2 = new File(context.getCacheDir(), fileName);
                if (file2.exists() && file2.length() > 1000) {
                    tf = Typeface.createFromFile(file2);
                    this.customEnglishTypefaces.put(fontName, tf);
                } else {
                    tf = Typeface.create("sans-serif-medium", 0);
                }
                Intrinsics.checkNotNull(tf);
                return tf;
            } catch (Exception e2) {
                Typeface create2 = Typeface.create("sans-serif-medium", 0);
                Intrinsics.checkNotNull(create2);
                return create2;
            }
        } catch (Exception e3) {
        }
    }

    public final void cancelNetworkRequests() {
        try {
            this.client.dispatcher().cancelAll();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public final String formatAyahSymbol(int ayah) {
        String arabicStr = StringsKt.replace$default(StringsKt.replace$default(StringsKt.replace$default(StringsKt.replace$default(StringsKt.replace$default(StringsKt.replace$default(StringsKt.replace$default(StringsKt.replace$default(StringsKt.replace$default(StringsKt.replace$default(String.valueOf(ayah), "0", "٠", false, 4, (Object) null), "1", "١", false, 4, (Object) null), "2", "٢", false, 4, (Object) null), "3", "٣", false, 4, (Object) null), "4", "٤", false, 4, (Object) null), "5", "٥", false, 4, (Object) null), "6", "٦", false, 4, (Object) null), "7", "٧", false, 4, (Object) null), "8", "٨", false, 4, (Object) null), "9", "٩", false, 4, (Object) null);
        return " ﴿" + arabicStr + "﴾";
    }

    public final Object generateReel(Context context, int surah, int startAyah, int endAyah, String reciterId, boolean showTranslation, String pexelsApiKey, String videoQuality, boolean isRetry, boolean isPreviewMode, boolean includeBasmalah, String videoQuery, Function2<? super String, ? super Float, Unit> function2, Function1<? super Uri, Unit> function1, Function1<? super String, Unit> function12, Continuation<? super Unit> continuation) {
        Object withContext = BuildersKt.withContext(Dispatchers.getIO(), new VideoGenerator$generateReel$2(surah, startAyah, endAyah, reciterId, showTranslation, this, context, function2, includeBasmalah, isRetry, pexelsApiKey, videoQuery, videoQuality, isPreviewMode, function1, function12, null), continuation);
        return withContext == IntrinsicsKt.getCOROUTINE_SUSPENDED() ? withContext : Unit.INSTANCE;
    }

    private final String fetchFullSurahText(int surah) {
        String url;
        String string;
        int i = surah;
        String url2 = "https://api.alquran.cloud/v1/surah/" + i + "/quran-uthmani";
        Request request = new Request.Builder().url(url2).build();
        Response response = this.client.newCall(request).execute();
        String str = "";
        if (response.isSuccessful()) {
            ResponseBody body = response.body();
            if (body != null && (string = body.string()) != null) {
                str = string;
            }
            JSONObject json = new JSONObject(str);
            JSONArray ayahs = json.getJSONObject("data").getJSONArray("ayahs");
            StringBuilder sb = new StringBuilder();
            int i2 = 0;
            int length = ayahs.length();
            while (i2 < length) {
                String text = ayahs.getJSONObject(i2).getString("text");
                if (i == 1 || i == 9 || i2 != 0) {
                    url = url2;
                } else {
                    boolean z = false;
                    List keywords = CollectionsKt.listOf(new String[]{"بِسْمِ اللَّهِ", "بِسْمِ اللهِ", "بِسْمِ"});
                    Intrinsics.checkNotNull(text);
                    if (StringsKt.startsWith$default(text, "بِسْمِ اللَّهِ الرَّحْمَٰنِ الرَّحِيمِ", false, 2, (Object) null)) {
                        Intrinsics.checkNotNull(text);
                        String substring = text.substring("بِسْمِ اللَّهِ الرَّحْمَٰنِ الرَّحِيمِ".length());
                        Intrinsics.checkNotNullExpressionValue(substring, "substring(...)");
                        text = StringsKt.trim(substring).toString();
                        url = url2;
                    } else {
                        Iterator it = keywords.iterator();
                        while (true) {
                            if (!it.hasNext()) {
                                url = url2;
                                break;
                            }
                            String kw = (String) it.next();
                            Intrinsics.checkNotNull(text);
                            url = url2;
                            if (StringsKt.startsWith$default(text, kw, z, 2, (Object) null)) {
                                Intrinsics.checkNotNull(text);
                                int index = StringsKt.indexOf$default(text, "الرَّحِيمِ", 0, false, 6, (Object) null);
                                if (index != -1 && index < 60) {
                                    Intrinsics.checkNotNull(text);
                                    String substring2 = text.substring(index + 10);
                                    Intrinsics.checkNotNullExpressionValue(substring2, "substring(...)");
                                    text = StringsKt.trim(substring2).toString();
                                    break;
                                }
                                Intrinsics.checkNotNull(text);
                                int index2 = StringsKt.indexOf$default(text, "الرَّحِيْمِ", 0, false, 6, (Object) null);
                                if (index2 != -1 && index2 < 60) {
                                    Intrinsics.checkNotNull(text);
                                    String substring3 = text.substring(index2 + 11);
                                    Intrinsics.checkNotNullExpressionValue(substring3, "substring(...)");
                                    text = StringsKt.trim(substring3).toString();
                                    break;
                                }
                                url2 = url;
                                z = false;
                            } else {
                                url2 = url;
                                z = false;
                            }
                        }
                    }
                }
                sb.append(text).append(" ");
                i2++;
                i = surah;
                url2 = url;
            }
            String sb2 = sb.toString();
            Intrinsics.checkNotNullExpressionValue(sb2, "toString(...)");
            return StringsKt.trim(sb2).toString();
        }
        return "";
    }

    /* JADX INFO: Access modifiers changed from: private */
    /* JADX WARN: Code restructure failed: missing block: B:12:0x005e, code lost:
        if (r9 == null) goto L15;
     */
    /* JADX WARN: Code restructure failed: missing block: B:13:0x0060, code lost:
        r9 = "";
     */
    /*
        Code decompiled incorrectly, please refer to instructions dump.
        To view partially-correct add '--show-bad-code' argument
    */
    public final kotlin.Triple<java.lang.String, java.lang.Integer, java.lang.String> fetchVerseInfo(int r12, int r13, java.lang.String r14) {
        /*
            r11 = this;
            java.lang.StringBuilder r0 = new java.lang.StringBuilder
            r0.<init>()
            java.lang.String r1 = "https://api.alquran.cloud/v1/ayah/"
            java.lang.StringBuilder r0 = r0.append(r1)
            java.lang.StringBuilder r0 = r0.append(r12)
            java.lang.String r1 = ":"
            java.lang.StringBuilder r0 = r0.append(r1)
            java.lang.StringBuilder r0 = r0.append(r13)
            java.lang.String r1 = "/"
            java.lang.StringBuilder r0 = r0.append(r1)
            java.lang.StringBuilder r0 = r0.append(r14)
            java.lang.String r0 = r0.toString()
            okhttp3.Request$Builder r1 = new okhttp3.Request$Builder
            r1.<init>()
            okhttp3.Request$Builder r1 = r1.url(r0)
            okhttp3.Request r1 = r1.build()
            java.lang.String r2 = ""
            r3 = 0
        L37:
            r4 = 3
            r5 = 1
            if (r3 >= r4) goto L6d
        L3c:
            r6 = 2000(0x7d0, double:9.88E-321)
            okhttp3.OkHttpClient r8 = r11.client     // Catch: java.lang.Exception -> L64
            okhttp3.Call r8 = r8.newCall(r1)     // Catch: java.lang.Exception -> L64
            okhttp3.Response r8 = r8.execute()     // Catch: java.lang.Exception -> L64
            boolean r9 = r8.isSuccessful()     // Catch: java.lang.Exception -> L64
            if (r9 != 0) goto L54
            int r3 = r3 + 1
            java.lang.Thread.sleep(r6)     // Catch: java.lang.Exception -> L64
            goto L37
        L54:
            okhttp3.ResponseBody r9 = r8.body()     // Catch: java.lang.Exception -> L64
            if (r9 == 0) goto L60
            java.lang.String r9 = r9.string()     // Catch: java.lang.Exception -> L64
            if (r9 != 0) goto L62
        L60:
            java.lang.String r9 = ""
        L62:
            r2 = r9
            goto L6d
        L64:
            r8 = move-exception
            int r3 = r3 + r5
            if (r3 >= r4) goto L6c
            java.lang.Thread.sleep(r6)
            goto L37
        L6c:
            throw r8
        L6d:
            r4 = r2
            java.lang.CharSequence r4 = (java.lang.CharSequence) r4
            int r4 = r4.length()
            if (r4 != 0) goto L77
            goto L78
        L77:
            r5 = 0
        L78:
            if (r5 != 0) goto La7
            org.json.JSONObject r4 = new org.json.JSONObject
            r4.<init>(r2)
            java.lang.String r5 = "data"
            org.json.JSONObject r5 = r4.getJSONObject(r5)
            java.lang.String r6 = "surah"
            org.json.JSONObject r6 = r5.getJSONObject(r6)
            java.lang.String r7 = "name"
            java.lang.String r7 = r6.getString(r7)
            kotlin.Triple r8 = new kotlin.Triple
            java.lang.String r9 = "text"
            java.lang.String r9 = r5.getString(r9)
            java.lang.String r10 = "number"
            int r10 = r5.getInt(r10)
            java.lang.Integer r10 = java.lang.Integer.valueOf(r10)
            r8.<init>(r9, r10, r7)
            return r8
        La7:
            java.lang.Exception r4 = new java.lang.Exception
            java.lang.String r5 = "فشل تحميل نصوص الآيات من الخادم"
            r4.<init>(r5)
            throw r4
        */
        throw new UnsupportedOperationException("Method not decompiled: com.example.generator.VideoGenerator.fetchVerseInfo(int, int, java.lang.String):kotlin.Triple");
    }

    /* JADX INFO: Access modifiers changed from: private */
    /* JADX WARN: Code restructure failed: missing block: B:42:0x015f, code lost:
        if (r0 == null) goto L69;
     */
    /* JADX WARN: Removed duplicated region for block: B:137:0x02ba A[Catch: all -> 0x02e9, TryCatch #3 {all -> 0x02e9, blocks: (B:135:0x02b4, B:137:0x02ba, B:138:0x02bd, B:127:0x027a, B:129:0x0280, B:95:0x0223, B:97:0x0229, B:101:0x0230, B:103:0x0236, B:139:0x02be, B:140:0x02e8), top: B:156:0x027a }] */
    /* JADX WARN: Removed duplicated region for block: B:159:0x0277 A[EXC_TOP_SPLITTER, SYNTHETIC] */
    /* JADX WARN: Removed duplicated region for block: B:188:0x028d A[SYNTHETIC] */
    /*
        Code decompiled incorrectly, please refer to instructions dump.
        To view partially-correct add '--show-bad-code' argument
    */
    public final void downloadAudio(java.lang.String r31, java.io.File r32) {
        /*
            Method dump skipped, instructions count: 760
            To view this dump add '--comments-level debug' option
        */
        throw new UnsupportedOperationException("Method not decompiled: com.example.generator.VideoGenerator.downloadAudio(java.lang.String, java.io.File):void");
    }

    /* JADX INFO: Access modifiers changed from: private */
    public final void checkCancellationAndPause() {
        if (VideoGenerationService.Companion.isCancelled()) {
            throw new CancellationException("تم إلغاء عملية إنتاج الفيديو");
        }
        if (VideoGenerationService.Companion.isPaused()) {
            synchronized (VideoGenerationService.Companion.getPauseLock()) {
                while (VideoGenerationService.Companion.isPaused() && !VideoGenerationService.Companion.isCancelled()) {
                    try {
                        VideoGenerationService.Companion.getPauseLock().wait(100L);
                    } catch (Exception e) {
                    }
                }
                Unit unit = Unit.INSTANCE;
            }
            if (VideoGenerationService.Companion.isCancelled()) {
                throw new CancellationException("تم إلغاء عملية إنتاج الفيديو");
            }
        }
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public static /* synthetic */ List transcodeMp3ToAac$default(VideoGenerator videoGenerator, String str, String str2, Long l, Long l2, int i, Object obj) {
        if ((i & 4) != 0) {
            l = null;
        }
        if ((i & 8) != 0) {
            l2 = null;
        }
        return videoGenerator.transcodeMp3ToAac(str, str2, l, l2);
    }

    /* JADX WARN: Removed duplicated region for block: B:126:0x0388 A[ADDED_TO_REGION] */
    /* JADX WARN: Removed duplicated region for block: B:129:0x0393  */
    /* JADX WARN: Removed duplicated region for block: B:130:0x03ab  */
    /* JADX WARN: Removed duplicated region for block: B:134:0x03bd  */
    /* JADX WARN: Removed duplicated region for block: B:146:0x0426  */
    /*
        Code decompiled incorrectly, please refer to instructions dump.
        To view partially-correct add '--show-bad-code' argument
    */
    private final java.util.List<kotlin.Pair<java.lang.Long, java.lang.Float>> transcodeMp3ToAac(java.lang.String r48, java.lang.String r49, java.lang.Long r50, java.lang.Long r51) {
        /*
            Method dump skipped, instructions count: 1445
            To view this dump add '--comments-level debug' option
        */
        throw new UnsupportedOperationException("Method not decompiled: com.example.generator.VideoGenerator.transcodeMp3ToAac(java.lang.String, java.lang.String, java.lang.Long, java.lang.Long):java.util.List");
    }

    /* JADX INFO: Access modifiers changed from: private */
    /* JADX WARN: Removed duplicated region for block: B:41:0x00b1  */
    /* JADX WARN: Removed duplicated region for block: B:42:0x00b7  */
    /*
        Code decompiled incorrectly, please refer to instructions dump.
        To view partially-correct add '--show-bad-code' argument
    */
    public static final void transcodeMp3ToAac$drainEncoder(android.media.MediaCodec r17, android.media.MediaCodec.BufferInfo r18, long r19, kotlin.jvm.internal.Ref.IntRef r21, kotlin.jvm.internal.Ref.LongRef r22, kotlin.jvm.internal.Ref.LongRef r23, android.media.MediaMuxer r24, kotlin.jvm.internal.Ref.BooleanRef r25, kotlin.jvm.internal.Ref.BooleanRef r26, boolean r27) {
        /*
            Method dump skipped, instructions count: 225
            To view this dump add '--comments-level debug' option
        */
        throw new UnsupportedOperationException("Method not decompiled: com.example.generator.VideoGenerator.transcodeMp3ToAac$drainEncoder(android.media.MediaCodec, android.media.MediaCodec$BufferInfo, long, kotlin.jvm.internal.Ref$IntRef, kotlin.jvm.internal.Ref$LongRef, kotlin.jvm.internal.Ref$LongRef, android.media.MediaMuxer, kotlin.jvm.internal.Ref$BooleanRef, kotlin.jvm.internal.Ref$BooleanRef, boolean):void");
    }

    private final float getCumulativeEnergyRatio(int currentFrame, int totalFrames, long durationUs, List<Pair<Long, Float>> list) {
        Float valueOf;
        int i;
        Pair pair;
        if (list.isEmpty() || totalFrames <= 1) {
            float totalEnergy = currentFrame;
            return totalEnergy / totalFrames;
        }
        Iterator<T> it = list.iterator();
        if (it.hasNext()) {
            float floatValue = ((Number) ((Pair) it.next()).getSecond()).floatValue();
            while (it.hasNext()) {
                floatValue = Math.max(floatValue, ((Number) ((Pair) it.next()).getSecond()).floatValue());
            }
            valueOf = Float.valueOf(floatValue);
        } else {
            valueOf = null;
        }
        float peak = valueOf != null ? valueOf.floatValue() : 1.0f;
        float activeThreshold = 0.04f * peak;
        int i2 = 0;
        Iterator<Pair<Long, Float>> it2 = list.iterator();
        while (true) {
            i = -1;
            if (it2.hasNext()) {
                if ((((Number) it2.next().getSecond()).floatValue() > activeThreshold ? 1 : null) != null) {
                    break;
                }
                i2++;
            } else {
                i2 = -1;
                break;
            }
        }
        int firstActiveIdx = RangesKt.coerceAtLeast(i2, 0);
        ListIterator listIterator = list.listIterator(list.size());
        while (true) {
            if (!listIterator.hasPrevious()) {
                break;
            }
            if (((Number) listIterator.previous().getSecond()).floatValue() > activeThreshold) {
                pair = 1;
                continue;
            } else {
                pair = null;
                continue;
            }
            if (pair != null) {
                i = listIterator.nextIndex();
                break;
            }
        }
        int lastActiveIdx = RangesKt.coerceAtMost(RangesKt.coerceAtLeast(i, 0), list.size() - 1);
        long startSpeechUs = ((Number) list.get(firstActiveIdx).getFirst()).longValue();
        long endSpeechUs = ((Number) list.get(lastActiveIdx).getFirst()).longValue();
        long activeDurationUs = endSpeechUs - startSpeechUs;
        float currentTimeUs = (currentFrame / totalFrames) * ((float) durationUs);
        if (currentTimeUs < ((float) startSpeechUs)) {
            return 0.0f;
        }
        if (currentTimeUs > ((float) endSpeechUs)) {
            return 1.0f;
        }
        if (activeDurationUs <= 0) {
            return (currentTimeUs - ((float) startSpeechUs)) / Math.max(1.0f, (float) durationUs);
        }
        float totalEnergy2 = 0.0f;
        float cumulativeEnergy = 0.0f;
        for (Pair sample : list) {
            if (((Number) sample.getFirst()).longValue() < startSpeechUs || ((Number) sample.getFirst()).longValue() > endSpeechUs) {
                cumulativeEnergy = cumulativeEnergy;
            } else {
                float totalEnergy3 = totalEnergy2 + ((Number) sample.getSecond()).floatValue();
                float cumulativeEnergy2 = cumulativeEnergy;
                if (((float) ((Number) sample.getFirst()).longValue()) > currentTimeUs) {
                    cumulativeEnergy = cumulativeEnergy2;
                    totalEnergy2 = totalEnergy3;
                } else {
                    cumulativeEnergy = cumulativeEnergy2 + ((Number) sample.getSecond()).floatValue();
                    totalEnergy2 = totalEnergy3;
                }
            }
        }
        float cumulativeEnergy3 = cumulativeEnergy;
        if (totalEnergy2 > 0.0f) {
            return cumulativeEnergy3 / totalEnergy2;
        }
        return (currentTimeUs - ((float) startSpeechUs)) / ((float) activeDurationUs);
    }

    private final String getActiveTextChunk(String text, int currentFrame, int totalFrames, long durationUs, List<Pair<Long, Float>> list, List<WordSegment> list2) {
        Regex regex = new Regex("\\s+");
        Collection arrayList = new ArrayList();
        for (Object obj : regex.split(text, 0)) {
            if (!StringsKt.isBlank((String) obj)) {
                arrayList.add(obj);
            }
        }
        Iterable rawWords = (List) arrayList;
        Iterable<String> iterable = rawWords;
        Collection arrayList2 = new ArrayList(CollectionsKt.collectionSizeOrDefault(iterable, 10));
        for (String str : iterable) {
            arrayList2.add(StringsKt.trim(StringsKt.replace$default(StringsKt.replace$default(StringsKt.replace$default(StringsKt.replace$default(StringsKt.replace$default(StringsKt.replace$default(StringsKt.replace$default(StringsKt.replace$default(str, "ۗ", "", false, 4, (Object) null), "ۖ", "", false, 4, (Object) null), "ۚ", "", false, 4, (Object) null), "ۘ", "", false, 4, (Object) null), "ۙ", "", false, 4, (Object) null), "ۛ", "", false, 4, (Object) null), "۞", "", false, 4, (Object) null), "۩", "", false, 4, (Object) null)).toString());
        }
        Collection arrayList3 = new ArrayList();
        for (Object obj2 : (List) arrayList2) {
            if (!StringsKt.isBlank((String) obj2)) {
                arrayList3.add(obj2);
            }
        }
        List words = (List) arrayList3;
        if (words.isEmpty()) {
            return text;
        }
        int maxWordsInChunk = 5;
        List chunks = new ArrayList();
        List arrayList4 = new ArrayList();
        int size = words.size();
        for (int idx = 0; idx < size; idx++) {
            arrayList4.add(Integer.valueOf(idx));
            if (arrayList4.size() >= 5) {
                chunks.add(arrayList4);
                List currentChunk = new ArrayList();
                arrayList4 = currentChunk;
            }
        }
        if (!arrayList4.isEmpty()) {
            chunks.add(arrayList4);
        }
        List adjustedWordSegments = CollectionsKt.sortedWith(list2, new Comparator() { // from class: com.example.generator.VideoGenerator$getActiveTextChunk$$inlined$sortedBy$1
            @Override // java.util.Comparator
            public final int compare(T t, T t2) {
                return ComparisonsKt.compareValues(Long.valueOf(((WordSegment) t).getStartTimeMs()), Long.valueOf(((WordSegment) t2).getStartTimeMs()));
            }
        });
        float currentTimeMs = ((currentFrame / totalFrames) * ((float) durationUs)) / 1000.0f;
        int activeChunkIdx = 0;
        if (adjustedWordSegments.isEmpty()) {
            float ratio = currentFrame / totalFrames;
            activeChunkIdx = RangesKt.coerceIn((int) (chunks.size() * ratio), 0, chunks.size() - 1);
        } else {
            int cIdx = 0;
            int size2 = chunks.size();
            while (cIdx < size2) {
                int firstWordIdxInChunk = ((Number) CollectionsKt.first((List) chunks.get(cIdx))).intValue();
                ArrayList arrayList5 = arrayList4;
                long chunkStartMs = getActiveTextChunk$getWordStartTime(adjustedWordSegments, firstWordIdxInChunk);
                int maxWordsInChunk2 = maxWordsInChunk;
                if (currentTimeMs >= ((float) chunkStartMs)) {
                    activeChunkIdx = cIdx;
                }
                cIdx++;
                arrayList4 = arrayList5;
                maxWordsInChunk = maxWordsInChunk2;
            }
        }
        Iterable activeChunkWordIndices = (List) chunks.get(activeChunkIdx);
        Iterable<Number> iterable2 = activeChunkWordIndices;
        Collection arrayList6 = new ArrayList(CollectionsKt.collectionSizeOrDefault(iterable2, 10));
        for (Number number : iterable2) {
            arrayList6.add((String) words.get(number.intValue()));
        }
        List chunkWords = (List) arrayList6;
        return CollectionsKt.joinToString$default(chunkWords, " ", (CharSequence) null, (CharSequence) null, 0, (CharSequence) null, (Function1) null, 62, (Object) null);
    }

    private static final long getActiveTextChunk$getWordStartTime(List<WordSegment> list, int wordIdx) {
        Object obj;
        Object obj2;
        WordSegment wordSegment;
        WordSegment wordSegment2;
        Iterator<T> it = list.iterator();
        while (true) {
            if (!it.hasNext()) {
                obj = null;
                break;
            }
            obj = it.next();
            if (((WordSegment) obj).getWordIndex() == wordIdx + 1) {
                wordSegment2 = 1;
                continue;
            } else {
                wordSegment2 = null;
                continue;
            }
            if (wordSegment2 != null) {
                break;
            }
        }
        WordSegment seg = (WordSegment) obj;
        if (seg != null) {
            return seg.getStartTimeMs();
        }
        if (wordIdx == 0) {
            return 0L;
        }
        for (int prevIdx = wordIdx - 1; -1 < prevIdx; prevIdx--) {
            Iterator<T> it2 = list.iterator();
            while (true) {
                if (!it2.hasNext()) {
                    obj2 = null;
                    break;
                }
                obj2 = it2.next();
                if (((WordSegment) obj2).getWordIndex() == prevIdx + 1) {
                    wordSegment = 1;
                    continue;
                } else {
                    wordSegment = null;
                    continue;
                }
                if (wordSegment != null) {
                    break;
                }
            }
            WordSegment prevSeg = (WordSegment) obj2;
            if (prevSeg != null) {
                return prevSeg.getEndTimeMs() + (((wordIdx - prevIdx) - 1) * 350);
            }
        }
        return wordIdx * 350;
    }

    private final String getActiveTranslationChunk(String translation, String text, int currentFrame, int totalFrames, long durationUs, List<Pair<Long, Float>> list, List<WordSegment> list2) {
        if (translation == null) {
            return null;
        }
        Regex regex = new Regex("\\s+");
        Collection arrayList = new ArrayList();
        for (Object obj : regex.split(text, 0)) {
            if (!StringsKt.isBlank((String) obj)) {
                arrayList.add(obj);
            }
        }
        List rawWords = (List) arrayList;
        List<String> list3 = rawWords;
        Collection arrayList2 = new ArrayList(CollectionsKt.collectionSizeOrDefault(list3, 10));
        for (String str : list3) {
            arrayList2.add(StringsKt.trim(StringsKt.replace$default(StringsKt.replace$default(StringsKt.replace$default(StringsKt.replace$default(StringsKt.replace$default(StringsKt.replace$default(StringsKt.replace$default(StringsKt.replace$default(str, "ۗ", "", false, 4, (Object) null), "ۖ", "", false, 4, (Object) null), "ۚ", "", false, 4, (Object) null), "ۘ", "", false, 4, (Object) null), "ۙ", "", false, 4, (Object) null), "ۛ", "", false, 4, (Object) null), "۞", "", false, 4, (Object) null), "۩", "", false, 4, (Object) null)).toString());
        }
        Collection arrayList3 = new ArrayList();
        for (Object obj2 : (List) arrayList2) {
            if (!StringsKt.isBlank((String) obj2)) {
                arrayList3.add(obj2);
            }
        }
        List words = (List) arrayList3;
        if (words.isEmpty()) {
            return translation;
        }
        Regex regex2 = new Regex("\\s+");
        Collection arrayList4 = new ArrayList();
        for (Object obj3 : regex2.split(translation, 0)) {
            if (!StringsKt.isBlank((String) obj3)) {
                arrayList4.add(obj3);
            }
        }
        List transWords = (List) arrayList4;
        if (transWords.size() <= 6) {
            return translation;
        }
        int maxWordsInChunk = 5;
        int chunksCount = RangesKt.coerceAtLeast((int) Math.ceil(words.size() / 5), 1);
        int wordsPerTransChunk = RangesKt.coerceAtLeast((int) Math.ceil(transWords.size() / chunksCount), 1);
        List transChunks = new ArrayList();
        for (int tIdx = 0; tIdx < transWords.size(); tIdx += wordsPerTransChunk) {
            int end = Math.min(tIdx + wordsPerTransChunk, transWords.size());
            transChunks.add(CollectionsKt.joinToString$default(transWords.subList(tIdx, end), " ", (CharSequence) null, (CharSequence) null, 0, (CharSequence) null, (Function1) null, 62, (Object) null));
        }
        List adjustedWordSegments = CollectionsKt.sortedWith(list2, new Comparator() { // from class: com.example.generator.VideoGenerator$getActiveTranslationChunk$$inlined$sortedBy$1
            @Override // java.util.Comparator
            public final int compare(T t, T t2) {
                return ComparisonsKt.compareValues(Long.valueOf(((WordSegment) t).getStartTimeMs()), Long.valueOf(((WordSegment) t2).getStartTimeMs()));
            }
        });
        float currentTimeMs = ((currentFrame / totalFrames) * ((float) durationUs)) / 1000.0f;
        int activeChunkIdx = 0;
        if (adjustedWordSegments.isEmpty()) {
            float ratio = currentFrame / totalFrames;
            activeChunkIdx = RangesKt.coerceIn((int) (chunksCount * ratio), 0, chunksCount - 1);
        } else {
            int cIdx = 0;
            while (cIdx < chunksCount) {
                int maxWordsInChunk2 = maxWordsInChunk;
                List words2 = words;
                int firstWordIdxInChunkCoerced = RangesKt.coerceAtMost(cIdx * maxWordsInChunk2, words.size() - 1);
                int cIdx2 = cIdx;
                List rawWords2 = rawWords;
                long chunkStartMs = getActiveTranslationChunk$getWordStartTime$27(adjustedWordSegments, firstWordIdxInChunkCoerced);
                if (currentTimeMs >= ((float) chunkStartMs)) {
                    activeChunkIdx = cIdx2;
                }
                cIdx = cIdx2 + 1;
                maxWordsInChunk = maxWordsInChunk2;
                rawWords = rawWords2;
                words = words2;
            }
        }
        if (activeChunkIdx < transChunks.size()) {
            return (String) transChunks.get(activeChunkIdx);
        }
        String str2 = (String) CollectionsKt.lastOrNull(transChunks);
        return str2 == null ? translation : str2;
    }

    private static final long getActiveTranslationChunk$getWordStartTime$27(List<WordSegment> list, int wordIdx) {
        Object obj;
        Object obj2;
        WordSegment wordSegment;
        WordSegment wordSegment2;
        Iterator<T> it = list.iterator();
        while (true) {
            if (!it.hasNext()) {
                obj = null;
                break;
            }
            obj = it.next();
            if (((WordSegment) obj).getWordIndex() == wordIdx + 1) {
                wordSegment2 = 1;
                continue;
            } else {
                wordSegment2 = null;
                continue;
            }
            if (wordSegment2 != null) {
                break;
            }
        }
        WordSegment seg = (WordSegment) obj;
        if (seg != null) {
            return seg.getStartTimeMs();
        }
        if (wordIdx == 0) {
            return 0L;
        }
        for (int prevIdx = wordIdx - 1; -1 < prevIdx; prevIdx--) {
            Iterator<T> it2 = list.iterator();
            while (true) {
                if (!it2.hasNext()) {
                    obj2 = null;
                    break;
                }
                obj2 = it2.next();
                if (((WordSegment) obj2).getWordIndex() == prevIdx + 1) {
                    wordSegment = 1;
                    continue;
                } else {
                    wordSegment = null;
                    continue;
                }
                if (wordSegment != null) {
                    break;
                }
            }
            WordSegment prevSeg = (WordSegment) obj2;
            if (prevSeg != null) {
                return prevSeg.getEndTimeMs() + (((wordIdx - prevIdx) - 1) * 350);
            }
        }
        return wordIdx * 350;
    }

    static /* synthetic */ Bitmap createVerseBitmap$default(VideoGenerator videoGenerator, String str, String str2, String str3, Bitmap bitmap, Context context, String str4, String str5, int i, String str6, float f, boolean z, String str7, float f2, int i2, String str8, String str9, String str10, int i3, String str11, String str12, String str13, int i4, int i5, int i6, int i7, int i8, String str14, float f3, int i9, int i10, float f4, int i11, int i12, long j, long j2, boolean z2, int i13, int i14, int i15, int i16, Object obj) {
        int i17;
        int i18;
        if ((i16 & 16) == 0) {
            i17 = i13;
        } else {
            i17 = 720;
        }
        if ((i16 & 32) == 0) {
            i18 = i14;
        } else {
            i18 = 1280;
        }
        return videoGenerator.createVerseBitmap(str, str2, str3, bitmap, context, str4, str5, i, str6, f, z, str7, f2, i2, str8, str9, str10, i3, str11, str12, str13, i4, i5, i6, i7, i8, str14, f3, i9, i10, f4, i11, i12, j, j2, z2, i17, i18);
    }

    /* JADX INFO: Access modifiers changed from: private */
    /* JADX WARN: Can't fix incorrect switch cases order, some code will duplicate */
    /* JADX WARN: Can't wrap try/catch for region: R(35:1|(1:3)(4:108|(2:111|109)|112|113)|4|(1:6)(1:107)|7|(3:8|9|10)|11|(1:13)(1:103)|14|(1:102)(26:18|19|23|24|25|26|27|(1:29)(2:88|(1:90)(1:91))|30|31|32|33|(1:35)(1:84)|36|(1:38)(1:83)|39|(1:41)(1:82)|42|(1:44)(2:78|(1:80)(1:81))|45|(1:47)(1:77)|(3:49|(1:51)(1:75)|(4:53|(1:74)(5:(3:68|69|70)(1:56)|57|(1:59)(1:67)|60|(1:62)(1:66))|63|64))|76|(0)(0)|63|64)|101|23|24|25|26|27|(0)(0)|30|31|32|33|(0)(0)|36|(0)(0)|39|(0)(0)|42|(0)(0)|45|(0)(0)|(0)|76|(0)(0)|63|64) */
    /* JADX WARN: Can't wrap try/catch for region: R(37:1|(1:3)(4:108|(2:111|109)|112|113)|4|(1:6)(1:107)|7|8|9|10|11|(1:13)(1:103)|14|(1:102)(26:18|19|23|24|25|26|27|(1:29)(2:88|(1:90)(1:91))|30|31|32|33|(1:35)(1:84)|36|(1:38)(1:83)|39|(1:41)(1:82)|42|(1:44)(2:78|(1:80)(1:81))|45|(1:47)(1:77)|(3:49|(1:51)(1:75)|(4:53|(1:74)(5:(3:68|69|70)(1:56)|57|(1:59)(1:67)|60|(1:62)(1:66))|63|64))|76|(0)(0)|63|64)|101|23|24|25|26|27|(0)(0)|30|31|32|33|(0)(0)|36|(0)(0)|39|(0)(0)|42|(0)(0)|45|(0)(0)|(0)|76|(0)(0)|63|64) */
    /* JADX WARN: Code restructure failed: missing block: B:47:0x02e0, code lost:
        r28 = -1;
     */
    /* JADX WARN: Code restructure failed: missing block: B:59:0x0382, code lost:
        r0 = android.graphics.Color.parseColor("#E0E0E0");
     */
    /* JADX WARN: Removed duplicated region for block: B:108:0x05b4  */
    /* JADX WARN: Removed duplicated region for block: B:50:0x0344  */
    /* JADX WARN: Removed duplicated region for block: B:51:0x0347  */
    /* JADX WARN: Removed duplicated region for block: B:62:0x03b9  */
    /* JADX WARN: Removed duplicated region for block: B:63:0x03bf  */
    /* JADX WARN: Removed duplicated region for block: B:66:0x03fd  */
    /* JADX WARN: Removed duplicated region for block: B:67:0x0415  */
    /* JADX WARN: Removed duplicated region for block: B:70:0x0422  */
    /* JADX WARN: Removed duplicated region for block: B:71:0x0429  */
    /* JADX WARN: Removed duplicated region for block: B:74:0x0438  */
    /* JADX WARN: Removed duplicated region for block: B:75:0x043f  */
    /* JADX WARN: Removed duplicated region for block: B:81:0x046a  */
    /* JADX WARN: Removed duplicated region for block: B:82:0x046c  */
    /* JADX WARN: Removed duplicated region for block: B:84:0x046f  */
    /* JADX WARN: Removed duplicated region for block: B:93:0x0494  */
    /*
        Code decompiled incorrectly, please refer to instructions dump.
        To view partially-correct add '--show-bad-code' argument
    */
    public final android.graphics.Bitmap createVerseBitmap(java.lang.String r55, java.lang.String r56, java.lang.String r57, android.graphics.Bitmap r58, android.content.Context r59, java.lang.String r60, java.lang.String r61, int r62, java.lang.String r63, float r64, boolean r65, java.lang.String r66, float r67, int r68, java.lang.String r69, java.lang.String r70, java.lang.String r71, int r72, java.lang.String r73, java.lang.String r74, java.lang.String r75, int r76, int r77, int r78, int r79, int r80, java.lang.String r81, float r82, int r83, int r84, float r85, int r86, int r87, long r88, long r90, boolean r92, int r93, int r94) {
        /*
            Method dump skipped, instructions count: 1508
            To view this dump add '--comments-level debug' option
        */
        throw new UnsupportedOperationException("Method not decompiled: com.example.generator.VideoGenerator.createVerseBitmap(java.lang.String, java.lang.String, java.lang.String, android.graphics.Bitmap, android.content.Context, java.lang.String, java.lang.String, int, java.lang.String, float, boolean, java.lang.String, float, int, java.lang.String, java.lang.String, java.lang.String, int, java.lang.String, java.lang.String, java.lang.String, int, int, int, int, int, java.lang.String, float, int, int, float, int, int, long, long, boolean, int, int):android.graphics.Bitmap");
    }

    /* JADX INFO: Access modifiers changed from: private */
    public final void fillImageFromBitmap(Image image, Bitmap bitmap) {
        Bitmap scaledBitmap;
        int[] argb;
        Image.Plane yPlane;
        int imgWidth;
        Image.Plane yPlane2;
        int imgWidth2;
        int imgWidth3 = image.getWidth();
        int imgHeight = image.getHeight();
        if (bitmap.getWidth() != imgWidth3 || bitmap.getHeight() != imgHeight) {
            scaledBitmap = Bitmap.createScaledBitmap(bitmap, imgWidth3, imgHeight, true);
        } else {
            scaledBitmap = bitmap;
        }
        Intrinsics.checkNotNull(scaledBitmap);
        int[] argb2 = new int[imgWidth3 * imgHeight];
        scaledBitmap.getPixels(argb2, 0, imgWidth3, 0, 0, imgWidth3, imgHeight);
        if (!Intrinsics.areEqual(scaledBitmap, bitmap)) {
            scaledBitmap.recycle();
        }
        Image.Plane yPlane3 = image.getPlanes()[0];
        Image.Plane uPlane = image.getPlanes()[1];
        Image.Plane vPlane = image.getPlanes()[2];
        ByteBuffer yBuffer = yPlane3.getBuffer();
        ByteBuffer uBuffer = uPlane.getBuffer();
        ByteBuffer vBuffer = vPlane.getBuffer();
        int yRowStride = yPlane3.getRowStride();
        int uRowStride = uPlane.getRowStride();
        int vRowStride = vPlane.getRowStride();
        int uPixelStride = uPlane.getPixelStride();
        int vPixelStride = vPlane.getPixelStride();
        yBuffer.clear();
        uBuffer.clear();
        vBuffer.clear();
        int bCol = 0;
        byte[] yBytes = new byte[imgWidth3];
        int index = 0;
        int index2 = 0;
        while (index2 < imgHeight) {
            Bitmap scaledBitmap2 = scaledBitmap;
            int c = 0;
            int r = index2;
            int r2 = index;
            while (true) {
                if (c >= imgWidth3) {
                    argb = argb2;
                    yPlane = yPlane3;
                    imgWidth = imgWidth3;
                    break;
                }
                int c2 = c;
                int c3 = argb2.length;
                if (r2 >= c3) {
                    argb = argb2;
                    yPlane = yPlane3;
                    imgWidth = imgWidth3;
                    break;
                }
                int index3 = r2 + 1;
                int color = argb2[r2];
                int rCol = (color & 16711680) >> 16;
                int index4 = (color & 65280) >> 8;
                int[] argb3 = argb2;
                int bCol2 = (color & 255) >> 0;
                int Y = (((((rCol * 66) + (index4 * 129)) + (bCol2 * 25)) + 128) >> 8) + 16;
                yBytes[c2] = (byte) RangesKt.coerceIn(Y, bCol, 255);
                if (r % 2 == 0 && c2 % 2 == 0) {
                    int U = (((((rCol * (-38)) - (index4 * 74)) + (bCol2 * 112)) + 128) >> 8) + 128;
                    int V = (((((rCol * 112) - (index4 * 94)) - (bCol2 * 18)) + 128) >> 8) + 128;
                    yPlane2 = yPlane3;
                    int U2 = RangesKt.coerceIn(U, 0, 255);
                    int V2 = RangesKt.coerceIn(V, 0, 255);
                    int V3 = c2 / 2;
                    int uPos = ((r / 2) * uRowStride) + (V3 * uPixelStride);
                    int cHalf = ((r / 2) * vRowStride) + (V3 * vPixelStride);
                    imgWidth2 = imgWidth3;
                    if (uPos < uBuffer.capacity()) {
                        uBuffer.position(uPos);
                        uBuffer.put((byte) U2);
                    }
                    if (cHalf < vBuffer.capacity()) {
                        vBuffer.position(cHalf);
                        vBuffer.put((byte) V2);
                    }
                } else {
                    yPlane2 = yPlane3;
                    imgWidth2 = imgWidth3;
                }
                c = c2 + 1;
                yPlane3 = yPlane2;
                argb2 = argb3;
                r2 = index3;
                imgWidth3 = imgWidth2;
                bCol = 0;
            }
            int c4 = r * yRowStride;
            if (c4 + imgWidth <= yBuffer.capacity()) {
                yBuffer.position(r * yRowStride);
                yBuffer.put(yBytes);
            }
            index = r2;
            index2 = r + 1;
            yPlane3 = yPlane;
            scaledBitmap = scaledBitmap2;
            argb2 = argb;
            imgWidth3 = imgWidth;
            bCol = 0;
        }
    }

    private final int mapReciterIdToQuranComId(String reciterId) {
        String clean = reciterId.toLowerCase(Locale.ROOT);
        Intrinsics.checkNotNullExpressionValue(clean, "toLowerCase(...)");
        if (StringsKt.contains$default(clean, "alafasy", false, 2, (Object) null)) {
            return 7;
        }
        if (StringsKt.contains$default(clean, "sudais", false, 2, (Object) null)) {
            return 3;
        }
        if (StringsKt.contains$default(clean, "shuraim", false, 2, (Object) null)) {
            return 10;
        }
        if (StringsKt.contains$default(clean, "husary", false, 2, (Object) null)) {
            return 6;
        }
        if (StringsKt.contains$default(clean, "minshawi", false, 2, (Object) null)) {
            return 9;
        }
        if (StringsKt.contains$default(clean, "abdulbasit", false, 2, (Object) null)) {
            return 2;
        }
        if (StringsKt.contains$default(clean, "shatri", false, 2, (Object) null)) {
            return 4;
        }
        if (StringsKt.contains$default(clean, "rifai", false, 2, (Object) null)) {
            return 5;
        }
        return StringsKt.contains$default(clean, "tablawi", false, 2, (Object) null) ? 11 : 7;
    }

    private final ByteBuffer resamplePCM(ByteBuffer inputBuf, int inputSize, int inputOffset, int srcSampleRate, int srcChannels, int dstSampleRate, int dstChannels) {
        int inPosition = inputBuf.position();
        int inLimit = inputBuf.limit();
        inputBuf.position(inputOffset);
        inputBuf.limit(inputOffset + inputSize);
        ShortBuffer shortBuf = inputBuf.asShortBuffer();
        int totalInputShorts = shortBuf.remaining();
        short[] inputShorts = new short[totalInputShorts];
        shortBuf.get(inputShorts);
        inputBuf.position(inPosition);
        inputBuf.limit(inLimit);
        int inputFrames = totalInputShorts / srcChannels;
        if (inputFrames <= 0) {
            ByteBuffer allocate = ByteBuffer.allocate(0);
            Intrinsics.checkNotNullExpressionValue(allocate, "allocate(...)");
            return allocate;
        }
        float[] monoSamples = new float[inputFrames];
        for (int i = 0; i < inputFrames; i++) {
            if (srcChannels == 1) {
                monoSamples[i] = inputShorts[i];
            } else {
                float ch0 = inputShorts[i * srcChannels];
                float ch1 = inputShorts[(i * srcChannels) + 1];
                monoSamples[i] = (ch0 + ch1) / 2.0f;
            }
        }
        double scale = dstSampleRate / srcSampleRate;
        int outputFrames = (int) (inputFrames * scale);
        float[] resampledMono = new float[outputFrames];
        int i2 = 0;
        while (i2 < outputFrames) {
            float[] resampledMono2 = resampledMono;
            double srcIdx = i2 / scale;
            int inPosition2 = inPosition;
            int inPosition3 = (int) srcIdx;
            double frac = srcIdx - inPosition3;
            int inLimit2 = inLimit;
            if (inPosition3 >= inputFrames - 1) {
                resampledMono2[i2] = monoSamples[inputFrames - 1];
            } else {
                float s0 = monoSamples[inPosition3];
                float s1 = monoSamples[inPosition3 + 1];
                resampledMono2[i2] = (((float) frac) * (s1 - s0)) + s0;
            }
            i2++;
            resampledMono = resampledMono2;
            inPosition = inPosition2;
            inLimit = inLimit2;
        }
        float[] resampledMono3 = resampledMono;
        int totalOutputShorts = outputFrames * dstChannels;
        ByteBuffer outBytes = ByteBuffer.allocate(totalOutputShorts * 2).order(ByteOrder.nativeOrder());
        int i3 = 0;
        while (i3 < outputFrames) {
            int outputFrames2 = outputFrames;
            short sampleVal = (short) RangesKt.coerceIn(resampledMono3[i3], -32768.0f, 32767.0f);
            for (int c = 0; c < dstChannels; c++) {
                outBytes.putShort(sampleVal);
            }
            i3++;
            outputFrames = outputFrames2;
        }
        outBytes.flip();
        Intrinsics.checkNotNull(outBytes);
        return outBytes;
    }

    private final String cleanArabicForWhisper(String text) {
        String clean1 = new Regex("[ً-ٰٟۖ-ۭؐ-ؚـ]").replace(text, "");
        String clean2 = new Regex("[ٱأإآ]").replace(clean1, "ا");
        String clean3 = new Regex("[^\\p{L}\\s]").replace(clean2, "");
        return StringsKt.trim(new Regex("\\s+").replace(clean3, " ")).toString();
    }

    /* JADX INFO: Access modifiers changed from: private */
    /* JADX WARN: Code restructure failed: missing block: B:224:0x05ac, code lost:
        com.example.generator.SystemDiagnosticTracker.INSTANCE.addLog("WHISPERX_API", "اكتملت المعالجة بنجاح. جاري تحليل البيانات المسترجعة...");
        r3 = new org.json.JSONArray(r15);
     */
    /* JADX WARN: Code restructure failed: missing block: B:225:0x05be, code lost:
        if (r3.length() < 3) goto L546;
     */
    /* JADX WARN: Code restructure failed: missing block: B:226:0x05c0, code lost:
        r9 = r3.get(0);
     */
    /* JADX WARN: Code restructure failed: missing block: B:227:0x05c8, code lost:
        if ((r9 instanceof org.json.JSONObject) == false) goto L224;
     */
    /* JADX WARN: Code restructure failed: missing block: B:229:0x05d1, code lost:
        if (((org.json.JSONObject) r9).has(r8) != false) goto L222;
     */
    /* JADX WARN: Code restructure failed: missing block: B:231:0x05d4, code lost:
        r0 = "استجابة خطأ بصيغة JSON من WhisperX: " + ((org.json.JSONObject) r9).getString(r8);
        com.example.generator.SystemDiagnosticTracker.INSTANCE.addLog(r2, r0);
     */
    /* JADX WARN: Code restructure failed: missing block: B:232:0x05f8, code lost:
        throw new java.lang.Exception(r0);
     */
    /* JADX WARN: Code restructure failed: missing block: B:234:0x05fb, code lost:
        if ((r9 instanceof java.lang.String) == false) goto L231;
     */
    /* JADX WARN: Code restructure failed: missing block: B:236:0x0609, code lost:
        if (kotlin.text.StringsKt.startsWith$default((java.lang.String) r9, "❌ خطأ", false, 2, (java.lang.Object) null) != false) goto L229;
     */
    /* JADX WARN: Code restructure failed: missing block: B:238:0x060c, code lost:
        r0 = "استجابة خطأ من WhisperX: " + r9;
        com.example.generator.SystemDiagnosticTracker.INSTANCE.addLog(r2, r0);
     */
    /* JADX WARN: Code restructure failed: missing block: B:239:0x0629, code lost:
        throw new java.lang.Exception(r0);
     */
    /* JADX WARN: Code restructure failed: missing block: B:241:0x062d, code lost:
        r11 = r3.length();
     */
    /* JADX WARN: Code restructure failed: missing block: B:242:0x0631, code lost:
        r13 = 0;
        r10 = null;
        r8 = null;
     */
    /* JADX WARN: Code restructure failed: missing block: B:243:0x0634, code lost:
        r21 = r9;
        r9 = "words";
        r29 = r2;
        r2 = "start";
     */
    /* JADX WARN: Code restructure failed: missing block: B:244:0x0640, code lost:
        if (r13 >= r11) goto L296;
     */
    /* JADX WARN: Code restructure failed: missing block: B:245:0x0642, code lost:
        r33 = r3.optString(r13);
     */
    /* JADX WARN: Code restructure failed: missing block: B:246:0x0646, code lost:
        if (r33 == null) goto L292;
     */
    /* JADX WARN: Code restructure failed: missing block: B:248:0x0650, code lost:
        if (r33.length() <= 0) goto L291;
     */
    /* JADX WARN: Code restructure failed: missing block: B:249:0x0652, code lost:
        r34 = true;
     */
    /* JADX WARN: Code restructure failed: missing block: B:250:0x0655, code lost:
        r34 = false;
     */
    /* JADX WARN: Code restructure failed: missing block: B:251:0x0657, code lost:
        if (r34 == false) goto L292;
     */
    /* JADX WARN: Code restructure failed: missing block: B:252:0x0659, code lost:
        r34 = kotlin.text.StringsKt.trim(r33).toString();
        r34 = r11;
     */
    /* JADX WARN: Code restructure failed: missing block: B:253:0x066b, code lost:
        r40 = r8;
        r41 = r12;
        r39 = r13;
        r38 = r15;
     */
    /* JADX WARN: Code restructure failed: missing block: B:255:0x067c, code lost:
        if (kotlin.text.StringsKt.startsWith$default(r34, "[", false, 2, (java.lang.Object) null) == false) goto L248;
     */
    /* JADX WARN: Code restructure failed: missing block: B:256:0x067f, code lost:
        r8 = new org.json.JSONArray(r34);
     */
    /* JADX WARN: Code restructure failed: missing block: B:257:0x0688, code lost:
        if (r8.length() <= 0) goto L250;
     */
    /* JADX WARN: Code restructure failed: missing block: B:258:0x068a, code lost:
        r9 = r8.optJSONObject(0);
     */
    /* JADX WARN: Code restructure failed: missing block: B:259:0x068f, code lost:
        if (r9 == null) goto L250;
     */
    /* JADX WARN: Code restructure failed: missing block: B:261:0x0695, code lost:
        if (r9.has("word") == false) goto L277;
     */
    /* JADX WARN: Code restructure failed: missing block: B:262:0x0697, code lost:
        if (r40 != null) goto L277;
     */
    /* JADX WARN: Code restructure failed: missing block: B:265:0x06a0, code lost:
        if (r9.has("text") == false) goto L283;
     */
    /* JADX WARN: Code restructure failed: missing block: B:267:0x06a6, code lost:
        if (r9.has("start") != false) goto L281;
     */
    /* JADX WARN: Code restructure failed: missing block: B:269:0x06ac, code lost:
        if (r9.has("text") == false) goto L250;
     */
    /* JADX WARN: Code restructure failed: missing block: B:271:0x06b4, code lost:
        if (r9.has("timestamp") == false) goto L250;
     */
    /* JADX WARN: Code restructure failed: missing block: B:272:0x06b6, code lost:
        r10 = r8;
        r8 = r40;
     */
    /* JADX WARN: Code restructure failed: missing block: B:276:0x06c6, code lost:
        if (kotlin.text.StringsKt.startsWith$default(r34, "{", false, 2, (java.lang.Object) null) == false) goto L250;
     */
    /* JADX WARN: Code restructure failed: missing block: B:277:0x06c9, code lost:
        r0 = new org.json.JSONObject(r34);
     */
    /* JADX WARN: Code restructure failed: missing block: B:278:0x06d2, code lost:
        if (r0.has("words") == false) goto L259;
     */
    /* JADX WARN: Code restructure failed: missing block: B:279:0x06d4, code lost:
        if (r40 != null) goto L259;
     */
    /* JADX WARN: Code restructure failed: missing block: B:280:0x06d6, code lost:
        r8 = r0.getJSONArray("words");
     */
    /* JADX WARN: Code restructure failed: missing block: B:282:0x06e2, code lost:
        if (r0.has("segments") == false) goto L250;
     */
    /* JADX WARN: Code restructure failed: missing block: B:283:0x06e4, code lost:
        if (r40 != null) goto L250;
     */
    /* JADX WARN: Code restructure failed: missing block: B:284:0x06e6, code lost:
        r2 = r0.getJSONArray("segments");
     */
    /* JADX WARN: Code restructure failed: missing block: B:285:0x06ec, code lost:
        r8 = r2;
     */
    /* JADX WARN: Code restructure failed: missing block: B:288:0x06f0, code lost:
        r0 = e;
     */
    /* JADX WARN: Code restructure failed: missing block: B:289:0x06f1, code lost:
        r6 = r29;
     */
    /* JADX WARN: Code restructure failed: missing block: B:290:0x06f8, code lost:
        r40 = r8;
        r34 = r11;
        r41 = r12;
        r39 = r13;
        r38 = r15;
     */
    /* JADX WARN: Code restructure failed: missing block: B:293:0x0712, code lost:
        r0 = e;
     */
    /* JADX WARN: Code restructure failed: missing block: B:294:0x0713, code lost:
        r6 = r29;
     */
    /* JADX WARN: Code restructure failed: missing block: B:295:0x071c, code lost:
        r40 = r8;
        r41 = r12;
        r38 = r15;
     */
    /* JADX WARN: Code restructure failed: missing block: B:296:0x0724, code lost:
        if (r40 == null) goto L300;
     */
    /* JADX WARN: Code restructure failed: missing block: B:297:0x0726, code lost:
        com.example.generator.SystemDiagnosticTracker.INSTANCE.addLog("WHISPERX_API", "عدد الكلمات المرجعة من WhisperX: " + r40.length());
     */
    /* JADX WARN: Code restructure failed: missing block: B:298:0x0742, code lost:
        if (r6 == null) goto L307;
     */
    /* JADX WARN: Code restructure failed: missing block: B:300:0x0748, code lost:
        if (r6.exists() == false) goto L426;
     */
    /* JADX WARN: Code restructure failed: missing block: B:303:0x0750, code lost:
        if (r6.length() != 0) goto L425;
     */
    /* JADX WARN: Code restructure failed: missing block: B:305:0x0753, code lost:
        r44 = r7;
        r45 = r24;
     */
    /* JADX WARN: Code restructure failed: missing block: B:306:0x075b, code lost:
        r11 = r24;
        r12 = 6;
     */
    /* JADX WARN: Code restructure failed: missing block: B:308:0x0763, code lost:
        r18 = null;
     */
    /* JADX WARN: Code restructure failed: missing block: B:309:0x0769, code lost:
        if (r3.length() <= 6) goto L537;
     */
    /* JADX WARN: Code restructure failed: missing block: B:310:0x076b, code lost:
        r13 = r3.optJSONObject(6);
        r19 = r3.optString(6);
     */
    /* JADX WARN: Code restructure failed: missing block: B:311:0x0775, code lost:
        if (r13 == null) goto L530;
     */
    /* JADX WARN: Code restructure failed: missing block: B:313:0x077b, code lost:
        if (r13.has("url") != false) goto L436;
     */
    /* JADX WARN: Code restructure failed: missing block: B:315:0x0781, code lost:
        if (r13.has("path") == false) goto L530;
     */
    /* JADX WARN: Code restructure failed: missing block: B:316:0x0783, code lost:
        r18 = r13;
     */
    /* JADX WARN: Code restructure failed: missing block: B:317:0x0786, code lost:
        kotlin.jvm.internal.Intrinsics.checkNotNull(r19);
     */
    /* JADX WARN: Code restructure failed: missing block: B:318:0x0791, code lost:
        if (kotlin.text.StringsKt.isBlank(r19) != false) goto L536;
     */
    /* JADX WARN: Code restructure failed: missing block: B:319:0x0793, code lost:
        r19 = r11;
        r11 = r19;
     */
    /* JADX WARN: Code restructure failed: missing block: B:320:0x079b, code lost:
        if (kotlin.jvm.internal.Intrinsics.areEqual(r11, "null") != false) goto L535;
     */
    /* JADX WARN: Code restructure failed: missing block: B:322:0x07a0, code lost:
        r19 = r11;
     */
    /* JADX WARN: Code restructure failed: missing block: B:323:0x07a5, code lost:
        r19 = r11;
     */
    /* JADX WARN: Code restructure failed: missing block: B:324:0x07a7, code lost:
        r11 = r19;
     */
    /* JADX WARN: Code restructure failed: missing block: B:325:0x07a9, code lost:
        if (r18 != null) goto L529;
     */
    /* JADX WARN: Code restructure failed: missing block: B:327:0x07b2, code lost:
        if (kotlin.text.StringsKt.isBlank(r11) == false) goto L529;
     */
    /* JADX WARN: Code restructure failed: missing block: B:328:0x07b4, code lost:
        r13 = r3.length() - 1;
     */
    /* JADX WARN: Code restructure failed: missing block: B:329:0x07bc, code lost:
        r19 = r11;
     */
    /* JADX WARN: Code restructure failed: missing block: B:330:0x07bf, code lost:
        if ((-1) >= r13) goto L526;
     */
    /* JADX WARN: Code restructure failed: missing block: B:331:0x07c1, code lost:
        r11 = r3.optJSONObject(r13);
        r33 = r3.optString(r13);
     */
    /* JADX WARN: Code restructure failed: missing block: B:332:0x07cb, code lost:
        if (r11 == null) goto L524;
     */
    /* JADX WARN: Code restructure failed: missing block: B:334:0x07d1, code lost:
        if (r11.has("url") != false) goto L501;
     */
    /* JADX WARN: Code restructure failed: missing block: B:336:0x07d7, code lost:
        if (r11.has("path") == false) goto L449;
     */
    /* JADX WARN: Code restructure failed: missing block: B:338:0x07da, code lost:
        r33 = r3;
        r44 = r7;
        r39 = r13;
        r45 = r24;
        r24 = r12;
     */
    /* JADX WARN: Code restructure failed: missing block: B:339:0x07e8, code lost:
        r33 = r3;
        r3 = r24;
        r24 = r12;
     */
    /* JADX WARN: Code restructure failed: missing block: B:340:0x07ee, code lost:
        r39 = r13;
        r11 = r11.optString("url", r3) + r11.optString("path", r3);
     */
    /* JADX WARN: Code restructure failed: missing block: B:341:0x0812, code lost:
        r45 = r3;
        r44 = r7;
     */
    /* JADX WARN: Code restructure failed: missing block: B:343:0x081f, code lost:
        if (kotlin.text.StringsKt.contains$default(r11, ".mp3", false, 2, (java.lang.Object) null) != false) goto L523;
     */
    /* JADX WARN: Code restructure failed: missing block: B:345:0x0830, code lost:
        if (kotlin.text.StringsKt.contains$default(r11, ".wav", false, 2, (java.lang.Object) null) != false) goto L522;
     */
    /* JADX WARN: Code restructure failed: missing block: B:347:0x0841, code lost:
        if (kotlin.text.StringsKt.contains$default(r11, ".m4a", false, 2, (java.lang.Object) null) != false) goto L521;
     */
    /* JADX WARN: Code restructure failed: missing block: B:349:0x0852, code lost:
        if (kotlin.text.StringsKt.contains$default(r11, ".ogg", false, 2, (java.lang.Object) null) != false) goto L520;
     */
    /* JADX WARN: Code restructure failed: missing block: B:351:0x0863, code lost:
        if (kotlin.text.StringsKt.contains$default(r11, "audio", false, 2, (java.lang.Object) null) != false) goto L519;
     */
    /* JADX WARN: Code restructure failed: missing block: B:353:0x0874, code lost:
        if (kotlin.text.StringsKt.contains$default(r11, "gradio", false, 2, (java.lang.Object) null) == false) goto L518;
     */
    /* JADX WARN: Code restructure failed: missing block: B:354:0x0876, code lost:
        r3 = r11;
        r11 = r19;
     */
    /* JADX WARN: Code restructure failed: missing block: B:355:0x087e, code lost:
        r33 = r3;
        r44 = r7;
        r39 = r13;
        r45 = r24;
        r24 = r12;
     */
    /* JADX WARN: Code restructure failed: missing block: B:356:0x088a, code lost:
        kotlin.jvm.internal.Intrinsics.checkNotNull(r33);
     */
    /* JADX WARN: Code restructure failed: missing block: B:357:0x0895, code lost:
        if (kotlin.text.StringsKt.isBlank(r33) != false) goto L497;
     */
    /* JADX WARN: Code restructure failed: missing block: B:359:0x089d, code lost:
        if (kotlin.jvm.internal.Intrinsics.areEqual(r33, "null") != false) goto L495;
     */
    /* JADX WARN: Code restructure failed: missing block: B:361:0x08af, code lost:
        if (kotlin.text.StringsKt.contains$default(r33, "العنوان:", false, 2, (java.lang.Object) null) != false) goto L494;
     */
    /* JADX WARN: Code restructure failed: missing block: B:363:0x08c0, code lost:
        if (kotlin.text.StringsKt.contains$default(r33, ".mp3", false, 2, (java.lang.Object) null) != false) goto L493;
     */
    /* JADX WARN: Code restructure failed: missing block: B:365:0x08d1, code lost:
        if (kotlin.text.StringsKt.contains$default(r33, ".wav", false, 2, (java.lang.Object) null) != false) goto L492;
     */
    /* JADX WARN: Code restructure failed: missing block: B:367:0x08e2, code lost:
        if (kotlin.text.StringsKt.contains$default(r33, ".m4a", false, 2, (java.lang.Object) null) != false) goto L491;
     */
    /* JADX WARN: Code restructure failed: missing block: B:369:0x08f3, code lost:
        if (kotlin.text.StringsKt.contains$default(r33, ".ogg", false, 2, (java.lang.Object) null) != false) goto L490;
     */
    /* JADX WARN: Code restructure failed: missing block: B:371:0x0904, code lost:
        if (kotlin.text.StringsKt.contains$default(r33, "/tmp/gradio/", false, 2, (java.lang.Object) null) == false) goto L488;
     */
    /* JADX WARN: Code restructure failed: missing block: B:372:0x0906, code lost:
        r11 = r33;
        r3 = r18;
     */
    /* JADX WARN: Code restructure failed: missing block: B:374:0x090d, code lost:
        r13 = r39 - 1;
        r11 = r19;
        r12 = r24;
        r3 = r33;
        r7 = r44;
        r24 = r45;
     */
    /* JADX WARN: Code restructure failed: missing block: B:375:0x091b, code lost:
        r44 = r7;
        r45 = r24;
     */
    /* JADX WARN: Code restructure failed: missing block: B:376:0x0926, code lost:
        r44 = r7;
        r19 = r11;
        r45 = r24;
     */
    /* JADX WARN: Code restructure failed: missing block: B:377:0x0930, code lost:
        r3 = r18;
        r11 = r19;
     */
    /* JADX WARN: Code restructure failed: missing block: B:378:0x0934, code lost:
        if (r3 == null) goto L473;
     */
    /* JADX WARN: Code restructure failed: missing block: B:380:0x093a, code lost:
        if (r3.has("url") == false) goto L473;
     */
    /* JADX WARN: Code restructure failed: missing block: B:381:0x093c, code lost:
        r4 = r3.getString("url");
        com.example.generator.SystemDiagnosticTracker.INSTANCE.addLog("WHISPERX_API", "تحميل الملف الصوتي المستخرج من الرابط: " + r4);
        kotlin.jvm.internal.Intrinsics.checkNotNull(r4);
        r1.downloadAudio(r4, r6);
     */
    /* JADX WARN: Code restructure failed: missing block: B:382:0x0960, code lost:
        if (r3 == null) goto L477;
     */
    /* JADX WARN: Code restructure failed: missing block: B:384:0x0966, code lost:
        if (r3.has("path") == false) goto L477;
     */
    /* JADX WARN: Code restructure failed: missing block: B:385:0x0968, code lost:
        r4 = r3.getString("path");
        r7 = "https://qalam249-whisperx-frontend.hf.space/file=" + r4;
        com.example.generator.SystemDiagnosticTracker.INSTANCE.addLog("WHISPERX_API", "تحميل الملف الصوتي المستخرج من المسار: " + r7);
        r1.downloadAudio(r7, r6);
     */
    /* JADX WARN: Code restructure failed: missing block: B:387:0x09a3, code lost:
        if (kotlin.text.StringsKt.isBlank(r11) != false) goto L486;
     */
    /* JADX WARN: Code restructure failed: missing block: B:389:0x09a9, code lost:
        if (kotlin.jvm.internal.Intrinsics.areEqual(r11, "null") != false) goto L486;
     */
    /* JADX WARN: Code restructure failed: missing block: B:391:0x09b4, code lost:
        if (kotlin.text.StringsKt.startsWith$default(r11, "http", false, 2, (java.lang.Object) null) == false) goto L485;
     */
    /* JADX WARN: Code restructure failed: missing block: B:392:0x09b6, code lost:
        r4 = r11;
     */
    /* JADX WARN: Code restructure failed: missing block: B:393:0x09b8, code lost:
        r4 = "https://qalam249-whisperx-frontend.hf.space/file=" + r11;
     */
    /* JADX WARN: Code restructure failed: missing block: B:394:0x09cb, code lost:
        com.example.generator.SystemDiagnosticTracker.INSTANCE.addLog("WHISPERX_API", "تحميل الملف الصوتي المستخرج من السلسلة النصية: " + r4);
        r1.downloadAudio(r4, r6);
     */
    /* JADX WARN: Code restructure failed: missing block: B:396:0x09ee, code lost:
        throw new java.lang.Exception("لم يتم استرجاع الملف الصوتي من المعالج (تعذر العثور على مسار الصوت)");
     */
    /* JADX WARN: Code restructure failed: missing block: B:397:0x09ef, code lost:
        r0 = e;
     */
    /* JADX WARN: Code restructure failed: missing block: B:398:0x09f0, code lost:
        r6 = r29;
     */
    /* JADX WARN: Code restructure failed: missing block: B:399:0x09f8, code lost:
        r0 = e;
     */
    /* JADX WARN: Code restructure failed: missing block: B:400:0x09f9, code lost:
        r6 = r29;
     */
    /* JADX WARN: Code restructure failed: missing block: B:401:0x0a03, code lost:
        r44 = r7;
        r45 = r24;
     */
    /* JADX WARN: Code restructure failed: missing block: B:402:0x0a09, code lost:
        r44.clear();
     */
    /* JADX WARN: Code restructure failed: missing block: B:403:0x0a0c, code lost:
        r3 = 0;
        r4 = false;
        r7 = "end";
     */
    /* JADX WARN: Code restructure failed: missing block: B:404:0x0a14, code lost:
        if (r10 == null) goto L416;
     */
    /* JADX WARN: Code restructure failed: missing block: B:405:0x0a16, code lost:
        r8 = 0;
     */
    /* JADX WARN: Code restructure failed: missing block: B:406:0x0a17, code lost:
        r13 = r10.length();
     */
    /* JADX WARN: Code restructure failed: missing block: B:407:0x0a1b, code lost:
        if (r8 >= r13) goto L393;
     */
    /* JADX WARN: Code restructure failed: missing block: B:408:0x0a1d, code lost:
        r15 = r10.getJSONObject(r8);
     */
    /* JADX WARN: Code restructure failed: missing block: B:409:0x0a25, code lost:
        r24 = r15.optJSONArray(r9);
     */
    /* JADX WARN: Code restructure failed: missing block: B:410:0x0a27, code lost:
        if (r24 == null) goto L318;
     */
    /* JADX WARN: Code restructure failed: missing block: B:412:0x0a2d, code lost:
        if (r24.length() <= 0) goto L318;
     */
    /* JADX WARN: Code restructure failed: missing block: B:413:0x0a2f, code lost:
        r4 = true;
     */
    /* JADX WARN: Code restructure failed: missing block: B:414:0x0a32, code lost:
        r11 = r24.length();
        r12 = 0;
     */
    /* JADX WARN: Code restructure failed: missing block: B:415:0x0a38, code lost:
        if (r12 >= r11) goto L384;
     */
    /* JADX WARN: Code restructure failed: missing block: B:416:0x0a3a, code lost:
        r1 = r24;
        r22 = r1.getJSONObject(r12);
        r30 = r3;
        r24 = r4;
        r42 = r22.optDouble("start", -1.0d);
        r48 = r22.optDouble(r7, -1.0d);
        r3 = r45;
        r4 = r22.optString("word", r3);
        kotlin.jvm.internal.Intrinsics.checkNotNullExpressionValue(r4, "optString(...)");
        r4 = kotlin.text.StringsKt.trim(r4).toString();
     */
    /* JADX WARN: Code restructure failed: missing block: B:418:0x0a6d, code lost:
        if (r48 < 0.0d) goto L383;
     */
    /* JADX WARN: Code restructure failed: missing block: B:420:0x0a73, code lost:
        if (java.lang.Double.isNaN(r48) == false) goto L369;
     */
    /* JADX WARN: Code restructure failed: missing block: B:421:0x0a75, code lost:
        r48 = r42 + 1.0d;
     */
    /* JADX WARN: Code restructure failed: missing block: B:423:0x0a7b, code lost:
        if (r42 < 0.0d) goto L381;
     */
    /* JADX WARN: Code restructure failed: missing block: B:424:0x0a7e, code lost:
        r45 = r3;
        r50 = new com.example.generator.WordSegment(r30 + 1, (long) (r42 * 1000.0d), (long) (r48 * 1000.0d), r4);
     */
    /* JADX WARN: Code restructure failed: missing block: B:425:0x0a99, code lost:
        r4 = r44;
     */
    /* JADX WARN: Code restructure failed: missing block: B:426:0x0a9d, code lost:
        r4.add(r50);
     */
    /* JADX WARN: Code restructure failed: missing block: B:427:0x0aa0, code lost:
        r3 = r30 + 1;
     */
    /* JADX WARN: Code restructure failed: missing block: B:428:0x0aa3, code lost:
        r0 = e;
     */
    /* JADX WARN: Code restructure failed: missing block: B:429:0x0aa4, code lost:
        r6 = r29;
     */
    /* JADX WARN: Code restructure failed: missing block: B:430:0x0aab, code lost:
        r45 = r3;
        r4 = r44;
        r3 = r30;
     */
    /* JADX WARN: Code restructure failed: missing block: B:431:0x0ab1, code lost:
        r12 = r12 + 1;
        r44 = r4;
        r4 = r24;
        r24 = r1;
     */
    /* JADX WARN: Code restructure failed: missing block: B:432:0x0abd, code lost:
        r1 = r44;
        r4 = r4;
        r11 = r45;
     */
    /* JADX WARN: Code restructure failed: missing block: B:433:0x0acb, code lost:
        r0 = e;
     */
    /* JADX WARN: Code restructure failed: missing block: B:434:0x0acc, code lost:
        r6 = r29;
     */
    /* JADX WARN: Code restructure failed: missing block: B:435:0x0ad5, code lost:
        r0 = e;
     */
    /* JADX WARN: Code restructure failed: missing block: B:436:0x0ad6, code lost:
        r6 = r29;
     */
    /* JADX WARN: Code restructure failed: missing block: B:437:0x0ade, code lost:
        r1 = r44;
        r11 = r45;
     */
    /* JADX WARN: Code restructure failed: missing block: B:438:0x0ae4, code lost:
        r12 = r15.optString("text", r11);
        kotlin.jvm.internal.Intrinsics.checkNotNullExpressionValue(r12, "optString(...)");
        r12 = kotlin.text.StringsKt.trim(r12).toString();
        r12 = r3;
        r24 = r4;
        r42 = r15.optDouble("start", -1.0d);
        r3 = r15.optDouble(r7, -1.0d);
     */
    /* JADX WARN: Code restructure failed: missing block: B:439:0x0b0a, code lost:
        if (r42 < 0.0d) goto L349;
     */
    /* JADX WARN: Code restructure failed: missing block: B:441:0x0b0e, code lost:
        if (r3 >= 0.0d) goto L325;
     */
    /* JADX WARN: Code restructure failed: missing block: B:443:0x0b11, code lost:
        r30 = r8;
        r20 = r9;
        r34 = r10;
        r9 = r42;
        r8 = false;
        r23 = true;
     */
    /* JADX WARN: Code restructure failed: missing block: B:444:0x0b1d, code lost:
        r6 = r15.optJSONArray("timestamp");
     */
    /* JADX WARN: Code restructure failed: missing block: B:445:0x0b23, code lost:
        if (r6 == null) goto L353;
     */
    /* JADX WARN: Code restructure failed: missing block: B:446:0x0b25, code lost:
        r30 = r8;
        r20 = r9;
        r34 = r10;
        r8 = false;
     */
    /* JADX WARN: Code restructure failed: missing block: B:447:0x0b2e, code lost:
        r46 = r6.optDouble(0, r42);
        r3 = r6.optDouble(1, r3);
        r23 = true;
        r9 = r46;
     */
    /* JADX WARN: Code restructure failed: missing block: B:448:0x0b3e, code lost:
        r30 = r8;
        r20 = r9;
        r34 = r10;
        r9 = r42;
        r8 = false;
        r23 = true;
     */
    /* JADX WARN: Code restructure failed: missing block: B:450:0x0b4b, code lost:
        if (r3 < 0.0d) goto L328;
     */
    /* JADX WARN: Code restructure failed: missing block: B:452:0x0b51, code lost:
        if (java.lang.Double.isNaN(r3) == false) goto L329;
     */
    /* JADX WARN: Code restructure failed: missing block: B:454:0x0b54, code lost:
        r0 = e;
     */
    /* JADX WARN: Code restructure failed: missing block: B:455:0x0b55, code lost:
        r6 = r29;
     */
    /* JADX WARN: Code restructure failed: missing block: B:456:0x0b5b, code lost:
        r3 = r9 + 3.0d;
     */
    /* JADX WARN: Code restructure failed: missing block: B:458:0x0b61, code lost:
        if (r9 < 0.0d) goto L341;
     */
    /* JADX WARN: Code restructure failed: missing block: B:460:0x0b6b, code lost:
        if (r12.length() <= 0) goto L340;
     */
    /* JADX WARN: Code restructure failed: missing block: B:461:0x0b6d, code lost:
        r6 = r23;
     */
    /* JADX WARN: Code restructure failed: missing block: B:462:0x0b70, code lost:
        r6 = r8;
     */
    /* JADX WARN: Code restructure failed: missing block: B:463:0x0b71, code lost:
        if (r6 == false) goto L341;
     */
    /* JADX WARN: Code restructure failed: missing block: B:464:0x0b74, code lost:
        r10 = r7;
     */
    /* JADX WARN: Code restructure failed: missing block: B:465:0x0b8f, code lost:
        r7 = r41;
     */
    /* JADX WARN: Code restructure failed: missing block: B:466:0x0b93, code lost:
        r7.add(new com.example.generator.SmartChunk(r12, null, (long) (r9 * 1000.0d), (long) (r3 * 1000.0d)));
     */
    /* JADX WARN: Code restructure failed: missing block: B:467:0x0b97, code lost:
        r10 = r7;
        r7 = r41;
     */
    /* JADX WARN: Code restructure failed: missing block: B:468:0x0b9c, code lost:
        r8 = r30 + 1;
        r44 = r1;
        r41 = r7;
        r7 = r10;
        r45 = r11;
        r3 = r12;
        r9 = r20;
        r4 = r24;
        r10 = r34;
     */
    /* JADX WARN: Code restructure failed: missing block: B:469:0x0bb2, code lost:
        r0 = e;
     */
    /* JADX WARN: Code restructure failed: missing block: B:470:0x0bb3, code lost:
        r6 = r29;
     */
    /* JADX WARN: Code restructure failed: missing block: B:471:0x0bb9, code lost:
        r1 = r44;
        r11 = r45;
        r10 = r7;
        r7 = r41;
     */
    /* JADX WARN: Code restructure failed: missing block: B:472:0x0bc5, code lost:
        r1 = r44;
        r11 = r45;
        r10 = "end";
        r7 = r41;
     */
    /* JADX WARN: Code restructure failed: missing block: B:473:0x0bce, code lost:
        if (r4 != false) goto L415;
     */
    /* JADX WARN: Code restructure failed: missing block: B:474:0x0bd0, code lost:
        r0 = 0;
        r6 = r40.length();
     */
    /* JADX WARN: Code restructure failed: missing block: B:475:0x0bd5, code lost:
        if (r0 >= r6) goto L410;
     */
    /* JADX WARN: Code restructure failed: missing block: B:476:0x0bd7, code lost:
        r8 = r40;
        r9 = r8.getJSONObject(r0);
        r22 = r9.optDouble(r2, -1.0d);
        r39 = r9.optDouble(r10, -1.0d);
        r15 = r9.optString("word", r11);
        kotlin.jvm.internal.Intrinsics.checkNotNullExpressionValue(r15, "optString(...)");
        r15 = kotlin.text.StringsKt.trim(r15).toString();
     */
    /* JADX WARN: Code restructure failed: missing block: B:477:0x0bfc, code lost:
        if (r39 < 0.0d) goto L409;
     */
    /* JADX WARN: Code restructure failed: missing block: B:479:0x0c02, code lost:
        if (java.lang.Double.isNaN(r39) == false) goto L402;
     */
    /* JADX WARN: Code restructure failed: missing block: B:480:0x0c04, code lost:
        r39 = r22 + 1.0d;
     */
    /* JADX WARN: Code restructure failed: missing block: B:482:0x0c0a, code lost:
        if (r22 < 0.0d) goto L407;
     */
    /* JADX WARN: Code restructure failed: missing block: B:483:0x0c0d, code lost:
        r15 = r2;
        r20 = r3;
        r1.add(new com.example.generator.WordSegment(r0 + 1, (long) (r22 * 1000.0d), (long) (r39 * 1000.0d), r15));
     */
    /* JADX WARN: Code restructure failed: missing block: B:484:0x0c2d, code lost:
        r15 = r2;
        r20 = r3;
     */
    /* JADX WARN: Code restructure failed: missing block: B:485:0x0c30, code lost:
        r0 = r0 + 1;
        r40 = r8;
        r2 = r15;
        r3 = r20;
     */
    /* JADX WARN: Code restructure failed: missing block: B:488:0x0c46, code lost:
        r0 = e;
     */
    /* JADX WARN: Code restructure failed: missing block: B:489:0x0c47, code lost:
        r6 = r29;
     */
    /* JADX WARN: Code restructure failed: missing block: B:490:0x0c4f, code lost:
        r0 = e;
     */
    /* JADX WARN: Code restructure failed: missing block: B:491:0x0c50, code lost:
        r6 = r29;
     */
    /* JADX WARN: Code restructure failed: missing block: B:494:0x0c7f, code lost:
        com.example.generator.SystemDiagnosticTracker.INSTANCE.addLog(r29, "لم يتم العثور على مصفوفة كلمات في استجابة WhisperX. الاستجابة: " + r38);
     */
    /* JADX WARN: Code restructure failed: missing block: B:495:0x0c87, code lost:
        throw new java.lang.Exception("لم يتم العثور على مصفوفة كلمات في استجابة WhisperX");
     */
    /* JADX WARN: Code restructure failed: missing block: B:496:0x0c88, code lost:
        r0 = e;
     */
    /* JADX WARN: Code restructure failed: missing block: B:497:0x0c89, code lost:
        r6 = r29;
     */
    /* JADX WARN: Code restructure failed: missing block: B:498:0x0c8d, code lost:
        r1 = r7;
        r7 = r12;
     */
    /* JADX WARN: Code restructure failed: missing block: B:503:0x0ce5, code lost:
        com.example.generator.AlignmentCacheManager.INSTANCE.putCachedAlignment(r58, r60, r61, r1, r7, r59);
     */
    /* JADX WARN: Code restructure failed: missing block: B:504:0x0cf8, code lost:
        return new kotlin.Pair<>(r1, r7);
     */
    /* JADX WARN: Code restructure failed: missing block: B:86:0x01e7, code lost:
        if (r14.length() != 0) goto L607;
     */
    /* JADX WARN: Code restructure failed: missing block: B:87:0x01e9, code lost:
        r0 = true;
     */
    /* JADX WARN: Code restructure failed: missing block: B:88:0x01eb, code lost:
        r0 = false;
     */
    /* JADX WARN: Code restructure failed: missing block: B:89:0x01ec, code lost:
        if (r0 != false) goto L605;
     */
    /* JADX WARN: Code restructure failed: missing block: B:90:0x01ee, code lost:
        com.example.generator.SystemDiagnosticTracker.INSTANCE.addLog("WHISPERX_API", "تم الرفع بنجاح. استجابة الرفع: " + r14);
        r0 = new org.json.JSONArray(r14);
        r8 = r0.getString(0);
        r3 = new org.json.JSONObject();
        r3.put("path", r8);
        r8 = new org.json.JSONObject();
        r8.put("_type", "gradio.FileData");
        r11 = kotlin.Unit.INSTANCE;
        r3.put("meta", r8);
        r13 = r3;
     */
    /* JADX WARN: Code restructure failed: missing block: B:92:0x024d, code lost:
        throw new java.lang.Exception("Empty upload response");
     */
    /* JADX WARN: Multi-variable type inference failed */
    /* JADX WARN: Removed duplicated region for block: B:103:0x029a  */
    /* JADX WARN: Removed duplicated region for block: B:106:0x02a3  */
    /* JADX WARN: Removed duplicated region for block: B:107:0x02a6  */
    /* JADX WARN: Removed duplicated region for block: B:140:0x0360 A[Catch: Exception -> 0x0294, TRY_ENTER, TryCatch #25 {Exception -> 0x0294, blocks: (B:82:0x01cc, B:83:0x01d9, B:85:0x01e0, B:90:0x01ee, B:99:0x028e, B:140:0x0360, B:141:0x036b, B:151:0x03a6, B:153:0x03ac, B:154:0x03c8, B:155:0x03c9, B:156:0x03e1, B:228:0x05ca, B:231:0x05d4, B:232:0x05f8, B:235:0x05fd, B:238:0x060c, B:239:0x0629, B:91:0x0242, B:92:0x024d), top: B:563:0x01cc }] */
    /* JADX WARN: Removed duplicated region for block: B:145:0x0379  */
    /* JADX WARN: Removed duplicated region for block: B:146:0x037b  */
    /* JADX WARN: Removed duplicated region for block: B:148:0x037e A[Catch: Exception -> 0x0d0e, TRY_LEAVE, TryCatch #16 {Exception -> 0x0d0e, blocks: (B:96:0x025e, B:108:0x02a8, B:143:0x0370, B:148:0x037e, B:157:0x03e2, B:160:0x0430, B:222:0x0588, B:224:0x05ac, B:226:0x05c0, B:233:0x05f9, B:241:0x062d, B:104:0x029c), top: B:547:0x025e }] */
    /* JADX WARN: Removed duplicated region for block: B:500:0x0c92 A[Catch: Exception -> 0x0d0c, TRY_LEAVE, TryCatch #18 {Exception -> 0x0d0c, blocks: (B:494:0x0c7f, B:495:0x0c87, B:500:0x0c92, B:506:0x0d04, B:507:0x0d0b), top: B:551:0x037c }] */
    /* JADX WARN: Removed duplicated region for block: B:505:0x0cf9  */
    /* JADX WARN: Removed duplicated region for block: B:556:0x02ea A[EXC_TOP_SPLITTER, SYNTHETIC] */
    /* JADX WARN: Removed duplicated region for block: B:563:0x01cc A[EXC_TOP_SPLITTER, SYNTHETIC] */
    /* JADX WARN: Removed duplicated region for block: B:568:0x0144 A[EXC_TOP_SPLITTER, SYNTHETIC] */
    /* JADX WARN: Removed duplicated region for block: B:579:0x01da A[SYNTHETIC] */
    /* JADX WARN: Removed duplicated region for block: B:582:0x01d9 A[SYNTHETIC] */
    /* JADX WARN: Removed duplicated region for block: B:587:0x036c A[SYNTHETIC] */
    /* JADX WARN: Removed duplicated region for block: B:591:0x036b A[SYNTHETIC] */
    /* JADX WARN: Removed duplicated region for block: B:595:0x05ac A[EDGE_INSN: B:595:0x05ac->B:224:0x05ac ?: BREAK  , SYNTHETIC] */
    /* JADX WARN: Removed duplicated region for block: B:99:0x028e A[Catch: Exception -> 0x0294, TRY_ENTER, TRY_LEAVE, TryCatch #25 {Exception -> 0x0294, blocks: (B:82:0x01cc, B:83:0x01d9, B:85:0x01e0, B:90:0x01ee, B:99:0x028e, B:140:0x0360, B:141:0x036b, B:151:0x03a6, B:153:0x03ac, B:154:0x03c8, B:155:0x03c9, B:156:0x03e1, B:228:0x05ca, B:231:0x05d4, B:232:0x05f8, B:235:0x05fd, B:238:0x060c, B:239:0x0629, B:91:0x0242, B:92:0x024d), top: B:563:0x01cc }] */
    /* JADX WARN: Type inference failed for: r6v0, types: [java.io.File] */
    /* JADX WARN: Type inference failed for: r6v1 */
    /* JADX WARN: Type inference failed for: r6v10 */
    /* JADX WARN: Type inference failed for: r6v11 */
    /* JADX WARN: Type inference failed for: r6v12 */
    /* JADX WARN: Type inference failed for: r6v14 */
    /* JADX WARN: Type inference failed for: r6v15 */
    /* JADX WARN: Type inference failed for: r6v16 */
    /* JADX WARN: Type inference failed for: r6v17 */
    /* JADX WARN: Type inference failed for: r6v21 */
    /* JADX WARN: Type inference failed for: r6v3 */
    /* JADX WARN: Type inference failed for: r6v35 */
    /* JADX WARN: Type inference failed for: r6v36 */
    /* JADX WARN: Type inference failed for: r6v39 */
    /* JADX WARN: Type inference failed for: r6v4 */
    /* JADX WARN: Type inference failed for: r6v40 */
    /* JADX WARN: Type inference failed for: r6v8 */
    /* JADX WARN: Type inference failed for: r6v9 */
    /*
        Code decompiled incorrectly, please refer to instructions dump.
        To view partially-correct add '--show-bad-code' argument
    */
    public final kotlin.Pair<java.util.List<com.example.generator.WordSegment>, java.util.List<com.example.generator.SmartChunk>> alignWithWhisperX(android.content.Context r58, java.io.File r59, java.lang.String r60, java.lang.String r61) {
        /*
            Method dump skipped, instructions count: 3419
            To view this dump add '--comments-level debug' option
        */
        throw new UnsupportedOperationException("Method not decompiled: com.example.generator.VideoGenerator.alignWithWhisperX(android.content.Context, java.io.File, java.lang.String, java.lang.String):kotlin.Pair");
    }

    /* JADX INFO: Access modifiers changed from: private */
    public final Object alignTranslationWithGemini(Context context, List<String> list, String fullTranslation, Continuation<? super List<String>> continuation) {
        return BuildersKt.withContext(Dispatchers.getIO(), new VideoGenerator$alignTranslationWithGemini$2(context, list, fullTranslation, this, null), continuation);
    }

    /* JADX INFO: Access modifiers changed from: private */
    /* JADX WARN: Multi-variable type inference failed */
    /* JADX WARN: Removed duplicated region for block: B:10:0x002d  */
    /* JADX WARN: Removed duplicated region for block: B:116:0x03e6  */
    /* JADX WARN: Removed duplicated region for block: B:12:0x0035  */
    /* JADX WARN: Removed duplicated region for block: B:13:0x0080  */
    /* JADX WARN: Removed duplicated region for block: B:241:0x0834  */
    /* JADX WARN: Removed duplicated region for block: B:242:0x0837  */
    /* JADX WARN: Removed duplicated region for block: B:34:0x0122  */
    /* JADX WARN: Removed duplicated region for block: B:98:0x038a  */
    /*
        Code decompiled incorrectly, please refer to instructions dump.
        To view partially-correct add '--show-bad-code' argument
    */
    public final java.lang.Object getSmartChunks(android.content.Context r47, java.lang.String r48, java.lang.String r49, java.util.List<com.example.generator.WordSegment> r50, java.util.List<com.example.generator.SmartChunk> r51, long r52, kotlin.coroutines.Continuation<? super java.util.List<com.example.generator.SmartChunk>> r54) {
        /*
            Method dump skipped, instructions count: 2142
            To view this dump add '--comments-level debug' option
        */
        throw new UnsupportedOperationException("Method not decompiled: com.example.generator.VideoGenerator.getSmartChunks(android.content.Context, java.lang.String, java.lang.String, java.util.List, java.util.List, long, kotlin.coroutines.Continuation):java.lang.Object");
    }

    private static final String getSmartChunks$normalizeForMatch(String w) {
        String stripped = new Regex("[ً-ٰٟۖ-ۭؐ-ؚـ]").replace(w, "");
        String stripNonLetter = new Regex("[^\\p{L}]").replace(stripped, "");
        return new Regex("[ٱأإآ]").replace(stripNonLetter, "ا");
    }

    private static final Pair<Long, Long> getSmartChunks$getWordTimingSafe(Map<Integer, WordSegment> map, int aIdx) {
        WordSegment seg = map.get(Integer.valueOf(aIdx));
        if (seg != null) {
            return new Pair<>(Long.valueOf(seg.getStartTimeMs()), Long.valueOf(seg.getEndTimeMs()));
        }
        return null;
    }

    private static final Pair<Long, Long> getSmartChunks$getWordTiming(int totalArabic, long $durationMs, Map<Integer, WordSegment> map, int aIdx, float fallbackRatio) {
        Pair safe = getSmartChunks$getWordTimingSafe(map, aIdx);
        if (safe != null) {
            return safe;
        }
        int prevIdx = aIdx - 1;
        Pair prevTiming = null;
        while (true) {
            if (prevIdx < 0) {
                break;
            }
            Pair t = getSmartChunks$getWordTimingSafe(map, prevIdx);
            if (t != null) {
                prevTiming = t;
                break;
            }
            prevIdx--;
        }
        int nextIdx = aIdx + 1;
        Pair nextTiming = null;
        while (true) {
            if (nextIdx >= totalArabic) {
                break;
            }
            Pair t2 = getSmartChunks$getWordTimingSafe(map, nextIdx);
            if (t2 != null) {
                nextTiming = t2;
                break;
            }
            nextIdx++;
        }
        if (prevTiming != null && nextTiming != null) {
            int steps = nextIdx - prevIdx;
            int myStep = aIdx - prevIdx;
            long start = ((Number) prevTiming.getSecond()).longValue() + (((((Number) nextTiming.getFirst()).longValue() - ((Number) prevTiming.getSecond()).longValue()) * myStep) / steps);
            return new Pair<>(Long.valueOf(start), Long.valueOf(((((Number) nextTiming.getFirst()).longValue() - ((Number) prevTiming.getSecond()).longValue()) / steps) + start));
        } else if (prevTiming != null) {
            long start2 = ((Number) prevTiming.getSecond()).longValue() + 50;
            return new Pair<>(Long.valueOf(start2), Long.valueOf(200 + start2));
        } else if (nextTiming != null) {
            long end = RangesKt.coerceAtLeast(((Number) nextTiming.getFirst()).longValue() - 50, 0L);
            return new Pair<>(Long.valueOf(RangesKt.coerceAtLeast(end - 200, 0L)), Long.valueOf(end));
        } else {
            long fallbackStart = ((float) $durationMs) * fallbackRatio;
            long fallbackEnd = RangesKt.coerceAtMost(300 + fallbackStart, $durationMs);
            return new Pair<>(Long.valueOf(fallbackStart), Long.valueOf(fallbackEnd));
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public final SmartChunk getActiveSmartChunk(List<SmartChunk> list, long currentTimeMs) {
        if (!list.isEmpty() && currentTimeMs >= ((SmartChunk) CollectionsKt.first(list)).getStartTimeMs()) {
            SmartChunk activeChunk = (SmartChunk) CollectionsKt.first(list);
            for (SmartChunk chunk : list) {
                if (currentTimeMs < chunk.getStartTimeMs()) {
                    break;
                }
                activeChunk = chunk;
            }
            return activeChunk;
        }
        return null;
    }
}
