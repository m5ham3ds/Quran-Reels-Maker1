package com.example.generator;

import kotlin.Metadata;
import kotlin.coroutines.Continuation;
import kotlin.coroutines.jvm.internal.ContinuationImpl;
import kotlin.coroutines.jvm.internal.DebugMetadata;
/* JADX INFO: Access modifiers changed from: package-private */
/* compiled from: VideoGenerator.kt */
@Metadata(k = 3, mv = {2, 2, 0}, xi = 48)
@DebugMetadata(c = "com.example.generator.VideoGenerator", f = "VideoGenerator.kt", i = {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0}, l = {3002}, m = "getSmartChunks", n = {"context", "arabicText", "englishText", "wordSegments", "whisperXChunks", "englishWords", "mergedChunks", "englishChunkTexts", "arabicChunkTexts", "durationMs", "totalArabic", "totalEnglish", "i"}, s = {"L$0", "L$1", "L$2", "L$3", "L$4", "L$5", "L$6", "L$7", "L$8", "J$0", "I$0", "I$1", "I$2"})
/* loaded from: /app/applet/classes5.dex */
public final class VideoGenerator$getSmartChunks$1 extends ContinuationImpl {
    int I$0;
    int I$1;
    int I$2;
    long J$0;
    Object L$0;
    Object L$1;
    Object L$2;
    Object L$3;
    Object L$4;
    Object L$5;
    Object L$6;
    Object L$7;
    Object L$8;
    Object L$9;
    int label;
    /* synthetic */ Object result;
    final /* synthetic */ VideoGenerator this$0;

    /* JADX INFO: Access modifiers changed from: package-private */
    /* JADX WARN: 'super' call moved to the top of the method (can break code semantics) */
    public VideoGenerator$getSmartChunks$1(VideoGenerator videoGenerator, Continuation<? super VideoGenerator$getSmartChunks$1> continuation) {
        super(continuation);
        this.this$0 = videoGenerator;
    }

    public final Object invokeSuspend(Object obj) {
        Object smartChunks;
        this.result = obj;
        this.label |= Integer.MIN_VALUE;
        smartChunks = this.this$0.getSmartChunks(null, null, null, null, null, 0L, (Continuation) this);
        return smartChunks;
    }
}
