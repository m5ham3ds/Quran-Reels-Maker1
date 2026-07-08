package com.example.generator;

import android.content.Context;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import kotlin.Metadata;
import kotlin.coroutines.Continuation;
import kotlin.jvm.internal.DefaultConstructorMarker;
import kotlin.jvm.internal.Intrinsics;
import kotlinx.coroutines.BuildersKt;
import kotlinx.coroutines.Dispatchers;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Response;
/* compiled from: GeminiMetaGenerator.kt */
@Metadata(d1 = {"\u0000<\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u000e\n\u0000\n\u0002\u0010\u000b\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0010\b\n\u0002\b\t\b\u0007\u0018\u0000 \u001b2\u00020\u0001:\u0001\u001bB\u0007¢\u0006\u0004\b\u0002\u0010\u0003J*\u0010\u0006\u001a\u0004\u0018\u00010\u00072\u0006\u0010\b\u001a\u00020\t2\u0006\u0010\n\u001a\u00020\u000b2\b\b\u0002\u0010\f\u001a\u00020\rH\u0086@¢\u0006\u0002\u0010\u000eJX\u0010\u000f\u001a\u0004\u0018\u00010\u00102\u0006\u0010\b\u001a\u00020\t2\u0006\u0010\u0011\u001a\u00020\u000b2\u0006\u0010\u0012\u001a\u00020\u00132\u0006\u0010\u0014\u001a\u00020\u00132\u0006\u0010\u0015\u001a\u00020\u000b2\u0006\u0010\u0016\u001a\u00020\r2\u0006\u0010\u0017\u001a\u00020\r2\u0006\u0010\u0018\u001a\u00020\r2\u0006\u0010\u0019\u001a\u00020\rH\u0086@¢\u0006\u0002\u0010\u001aR\u000e\u0010\u0004\u001a\u00020\u0005X\u0082\u0004¢\u0006\u0002\n\u0000¨\u0006\u001c"}, d2 = {"Lcom/example/generator/GeminiMetaGenerator;", "", "<init>", "()V", "client", "Lokhttp3/OkHttpClient;", "analyzeClipUrl", "Lcom/example/generator/ClipAnalysisResult;", "context", "Landroid/content/Context;", "url", "", "skipWhisperIfCached", "", "(Landroid/content/Context;Ljava/lang/String;ZLkotlin/coroutines/Continuation;)Ljava/lang/Object;", "generateSocialMeta", "Lcom/example/generator/GeneratedMetaResult;", "surahName", "startAyah", "", "endAyah", "reciterName", "isTiktok", "isInstagram", "isFacebook", "isYoutube", "(Landroid/content/Context;Ljava/lang/String;IILjava/lang/String;ZZZZLkotlin/coroutines/Continuation;)Ljava/lang/Object;", "Companion", "app"}, k = 1, mv = {2, 2, 0}, xi = 48)
/* loaded from: /app/applet/classes5.dex */
public final class GeminiMetaGenerator {
    private final OkHttpClient client = new OkHttpClient.Builder().connectTimeout(60, TimeUnit.SECONDS).readTimeout(60, TimeUnit.SECONDS).writeTimeout(60, TimeUnit.SECONDS).addInterceptor(new Interceptor() { // from class: com.example.generator.GeminiMetaGenerator$special$$inlined$-addInterceptor$1
        public final Response intercept(Interceptor.Chain chain) {
            Intrinsics.checkNotNullParameter(chain, "chain");
            return chain.proceed(chain.request().newBuilder().header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36").header("Accept", "*/*").build());
        }
    }).build();
    public static final Companion Companion = new Companion(null);
    public static final int $stable = 8;
    private static final Map<String, String> whisperXCache = new LinkedHashMap();

    /* compiled from: GeminiMetaGenerator.kt */
    @Metadata(d1 = {"\u0000\u0018\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0002\b\u0003\n\u0002\u0010%\n\u0002\u0010\u000e\n\u0002\b\u0003\b\u0086\u0003\u0018\u00002\u00020\u0001B\t\b\u0002¢\u0006\u0004\b\u0002\u0010\u0003R\u001d\u0010\u0004\u001a\u000e\u0012\u0004\u0012\u00020\u0006\u0012\u0004\u0012\u00020\u00060\u0005¢\u0006\b\n\u0000\u001a\u0004\b\u0007\u0010\b¨\u0006\t"}, d2 = {"Lcom/example/generator/GeminiMetaGenerator$Companion;", "", "<init>", "()V", "whisperXCache", "", "", "getWhisperXCache", "()Ljava/util/Map;", "app"}, k = 1, mv = {2, 2, 0}, xi = 48)
    /* loaded from: /app/applet/classes5.dex */
    public static final class Companion {
        public /* synthetic */ Companion(DefaultConstructorMarker defaultConstructorMarker) {
            this();
        }

        private Companion() {
        }

        public final Map<String, String> getWhisperXCache() {
            return GeminiMetaGenerator.whisperXCache;
        }
    }

    public static /* synthetic */ Object analyzeClipUrl$default(GeminiMetaGenerator geminiMetaGenerator, Context context, String str, boolean z, Continuation continuation, int i, Object obj) {
        if ((i & 4) != 0) {
            z = false;
        }
        return geminiMetaGenerator.analyzeClipUrl(context, str, z, continuation);
    }

    public final Object analyzeClipUrl(Context context, String url, boolean skipWhisperIfCached, Continuation<? super ClipAnalysisResult> continuation) {
        return BuildersKt.withContext(Dispatchers.getIO(), new GeminiMetaGenerator$analyzeClipUrl$2(context, url, skipWhisperIfCached, this, null), continuation);
    }

    public final Object generateSocialMeta(Context context, String surahName, int startAyah, int endAyah, String reciterName, boolean isTiktok, boolean isInstagram, boolean isFacebook, boolean isYoutube, Continuation<? super GeneratedMetaResult> continuation) {
        return BuildersKt.withContext(Dispatchers.getIO(), new GeminiMetaGenerator$generateSocialMeta$2(context, surahName, startAyah, endAyah, reciterName, isTiktok, isInstagram, isFacebook, isYoutube, this, null), continuation);
    }
}
