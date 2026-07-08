package com.example.generator;

import android.content.Context;
import java.util.List;
import kotlin.Metadata;
import kotlin.Unit;
import kotlin.coroutines.Continuation;
import kotlin.coroutines.jvm.internal.DebugMetadata;
import kotlin.coroutines.jvm.internal.SuspendLambda;
import kotlin.jvm.functions.Function2;
import kotlinx.coroutines.CoroutineScope;
/* JADX INFO: Access modifiers changed from: package-private */
/* compiled from: VideoGenerator.kt */
@Metadata(d1 = {"\u0000\u000e\n\u0000\n\u0002\u0010!\n\u0002\u0010\u000e\n\u0002\u0018\u0002\u0010\u0000\u001a\n\u0012\u0004\u0012\u00020\u0002\u0018\u00010\u0001*\u00020\u0003H\n"}, d2 = {"<anonymous>", "", "", "Lkotlinx/coroutines/CoroutineScope;"}, k = 3, mv = {2, 2, 0}, xi = 48)
@DebugMetadata(c = "com.example.generator.VideoGenerator$alignTranslationWithGemini$2", f = "VideoGenerator.kt", i = {0, 1, 1, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2}, l = {2870, 2871, 2946}, m = "invokeSuspend", n = {"settingsManager", "settingsManager", "apiKey", "settingsManager", "apiKey", "geminiModel", "prompt", "jsonRequest", "requestBody", "url", "request", "response", "attempt", "maxAttempts"}, s = {"L$0", "L$0", "L$1", "L$0", "L$1", "L$2", "L$3", "L$4", "L$5", "L$6", "L$7", "L$8", "I$0", "I$1"})
/* loaded from: /app/applet/classes5.dex */
public final class VideoGenerator$alignTranslationWithGemini$2 extends SuspendLambda implements Function2<CoroutineScope, Continuation<? super List<String>>, Object> {
    final /* synthetic */ List<String> $arabicChunks;
    final /* synthetic */ Context $context;
    final /* synthetic */ String $fullTranslation;
    int I$0;
    int I$1;
    Object L$0;
    Object L$1;
    Object L$2;
    Object L$3;
    Object L$4;
    Object L$5;
    Object L$6;
    Object L$7;
    Object L$8;
    int label;
    final /* synthetic */ VideoGenerator this$0;

    /* JADX INFO: Access modifiers changed from: package-private */
    /* JADX WARN: 'super' call moved to the top of the method (can break code semantics) */
    public VideoGenerator$alignTranslationWithGemini$2(Context context, List<String> list, String str, VideoGenerator videoGenerator, Continuation<? super VideoGenerator$alignTranslationWithGemini$2> continuation) {
        super(2, continuation);
        this.$context = context;
        this.$arabicChunks = list;
        this.$fullTranslation = str;
        this.this$0 = videoGenerator;
    }

    public final Continuation<Unit> create(Object obj, Continuation<?> continuation) {
        return new VideoGenerator$alignTranslationWithGemini$2(this.$context, this.$arabicChunks, this.$fullTranslation, this.this$0, continuation);
    }

    public final Object invoke(CoroutineScope coroutineScope, Continuation<? super List<String>> continuation) {
        return create(coroutineScope, continuation).invokeSuspend(Unit.INSTANCE);
    }

    /* JADX WARN: Code restructure failed: missing block: B:35:0x0200, code lost:
        if (r5 == null) goto L20;
     */
    /* JADX WARN: Removed duplicated region for block: B:18:0x00b9 A[RETURN] */
    /* JADX WARN: Removed duplicated region for block: B:19:0x00ba  */
    /* JADX WARN: Removed duplicated region for block: B:22:0x00ca  */
    /* JADX WARN: Removed duplicated region for block: B:82:0x036d  */
    /* JADX WARN: Removed duplicated region for block: B:90:0x01e1 A[EXC_TOP_SPLITTER, SYNTHETIC] */
    /* JADX WARN: Unsupported multi-entry loop pattern (BACK_EDGE: B:73:0x0342 -> B:74:0x0345). Please submit an issue!!! */
    /*
        Code decompiled incorrectly, please refer to instructions dump.
        To view partially-correct add '--show-bad-code' argument
    */
    public final java.lang.Object invokeSuspend(java.lang.Object r29) {
        /*
            Method dump skipped, instructions count: 906
            To view this dump add '--comments-level debug' option
        */
        throw new UnsupportedOperationException("Method not decompiled: com.example.generator.VideoGenerator$alignTranslationWithGemini$2.invokeSuspend(java.lang.Object):java.lang.Object");
    }
}
