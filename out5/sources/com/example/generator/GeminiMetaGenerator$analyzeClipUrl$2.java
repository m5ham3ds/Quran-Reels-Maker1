package com.example.generator;

import android.content.Context;
import kotlin.Metadata;
import kotlin.Unit;
import kotlin.coroutines.Continuation;
import kotlin.coroutines.jvm.internal.DebugMetadata;
import kotlin.coroutines.jvm.internal.SuspendLambda;
import kotlin.jvm.functions.Function2;
import kotlinx.coroutines.CoroutineScope;
/* JADX INFO: Access modifiers changed from: package-private */
/* compiled from: GeminiMetaGenerator.kt */
@Metadata(d1 = {"\u0000\n\n\u0000\n\u0002\u0018\u0002\n\u0002\u0018\u0002\u0010\u0000\u001a\u0004\u0018\u00010\u0001*\u00020\u0002H\n"}, d2 = {"<anonymous>", "Lcom/example/generator/ClipAnalysisResult;", "Lkotlinx/coroutines/CoroutineScope;"}, k = 3, mv = {2, 2, 0}, xi = 48)
@DebugMetadata(c = "com.example.generator.GeminiMetaGenerator$analyzeClipUrl$2", f = "GeminiMetaGenerator.kt", i = {0, 1, 1, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 4, 4, 4, 4, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 9, 9, 9, 9, 9, 9, 9, 9, 9, 9, 9, 9, 9}, l = {59, 60, 140, 144, 187, 233, 303, 313, 325, 335}, m = "invokeSuspend", n = {"settingsManager", "settingsManager", "apiKey", "settingsManager", "apiKey", "geminiModel", "whisperText", "alignPayload", "jsonMediaType", "alignRequest", "alignResponse", "alignResponseBody", "eventIdJson", "eventId", "eventRequest", "completedData", "eventResponse", "attempt", "settingsManager", "apiKey", "geminiModel", "whisperText", "alignPayload", "jsonMediaType", "alignRequest", "alignResponse", "alignResponseBody", "eventIdJson", "eventId", "eventRequest", "completedData", "e", "attempt", "settingsManager", "apiKey", "geminiModel", "whisperText", "settingsManager", "apiKey", "geminiModel", "whisperText", "savedPrompt", "prompt", "jsonRequest", "requestBody", "apiUrl", "request", "response", "responseStr", "rootJson", "errMsg", "attempt", "maxAttempts", "settingsManager", "apiKey", "geminiModel", "whisperText", "savedPrompt", "prompt", "jsonRequest", "requestBody", "apiUrl", "request", "response", "attempt", "maxAttempts", "settingsManager", "apiKey", "geminiModel", "whisperText", "savedPrompt", "prompt", "jsonRequest", "requestBody", "apiUrl", "request", "response", "attempt", "maxAttempts", "settingsManager", "apiKey", "geminiModel", "whisperText", "savedPrompt", "prompt", "jsonRequest", "requestBody", "apiUrl", "request", "e", "attempt", "maxAttempts", "settingsManager", "apiKey", "geminiModel", "whisperText", "savedPrompt", "prompt", "jsonRequest", "requestBody", "apiUrl", "request", "e", "attempt", "maxAttempts"}, s = {"L$0", "L$0", "L$1", "L$0", "L$1", "L$2", "L$3", "L$4", "L$5", "L$6", "L$7", "L$8", "L$9", "L$10", "L$11", "L$12", "L$13", "I$0", "L$0", "L$1", "L$2", "L$3", "L$4", "L$5", "L$6", "L$7", "L$8", "L$9", "L$10", "L$11", "L$12", "L$13", "I$0", "L$0", "L$1", "L$2", "L$3", "L$0", "L$1", "L$2", "L$3", "L$4", "L$5", "L$6", "L$7", "L$8", "L$9", "L$10", "L$11", "L$12", "L$13", "I$0", "I$1", "L$0", "L$1", "L$2", "L$3", "L$4", "L$5", "L$6", "L$7", "L$8", "L$9", "L$10", "I$0", "I$1", "L$0", "L$1", "L$2", "L$3", "L$4", "L$5", "L$6", "L$7", "L$8", "L$9", "L$10", "I$0", "I$1", "L$0", "L$1", "L$2", "L$3", "L$4", "L$5", "L$6", "L$7", "L$8", "L$9", "L$10", "I$0", "I$1", "L$0", "L$1", "L$2", "L$3", "L$4", "L$5", "L$6", "L$7", "L$8", "L$9", "L$10", "I$0", "I$1"})
/* loaded from: /app/applet/classes5.dex */
public final class GeminiMetaGenerator$analyzeClipUrl$2 extends SuspendLambda implements Function2<CoroutineScope, Continuation<? super ClipAnalysisResult>, Object> {
    final /* synthetic */ Context $context;
    final /* synthetic */ boolean $skipWhisperIfCached;
    final /* synthetic */ String $url;
    int I$0;
    int I$1;
    Object L$0;
    Object L$1;
    Object L$10;
    Object L$11;
    Object L$12;
    Object L$13;
    Object L$2;
    Object L$3;
    Object L$4;
    Object L$5;
    Object L$6;
    Object L$7;
    Object L$8;
    Object L$9;
    int label;
    final /* synthetic */ GeminiMetaGenerator this$0;

    /* JADX INFO: Access modifiers changed from: package-private */
    /* JADX WARN: 'super' call moved to the top of the method (can break code semantics) */
    public GeminiMetaGenerator$analyzeClipUrl$2(Context context, String str, boolean z, GeminiMetaGenerator geminiMetaGenerator, Continuation<? super GeminiMetaGenerator$analyzeClipUrl$2> continuation) {
        super(2, continuation);
        this.$context = context;
        this.$url = str;
        this.$skipWhisperIfCached = z;
        this.this$0 = geminiMetaGenerator;
    }

    public final Continuation<Unit> create(Object obj, Continuation<?> continuation) {
        return new GeminiMetaGenerator$analyzeClipUrl$2(this.$context, this.$url, this.$skipWhisperIfCached, this.this$0, continuation);
    }

    public final Object invoke(CoroutineScope coroutineScope, Continuation<? super ClipAnalysisResult> continuation) {
        return create(coroutineScope, continuation).invokeSuspend(Unit.INSTANCE);
    }

    /*  JADX ERROR: JadxRuntimeException in pass: BlockProcessor
        jadx.core.utils.exceptions.JadxRuntimeException: Unreachable block: B:448:0x1118
        	at jadx.core.dex.visitors.blocks.BlockProcessor.checkForUnreachableBlocks(BlockProcessor.java:81)
        	at jadx.core.dex.visitors.blocks.BlockProcessor.processBlocksTree(BlockProcessor.java:47)
        	at jadx.core.dex.visitors.blocks.BlockProcessor.visit(BlockProcessor.java:39)
        */
    public final java.lang.Object invokeSuspend(java.lang.Object r63) {
        /*
            Method dump skipped, instructions count: 5742
            To view this dump add '--comments-level debug' option
        */
        throw new UnsupportedOperationException("Method not decompiled: com.example.generator.GeminiMetaGenerator$analyzeClipUrl$2.invokeSuspend(java.lang.Object):java.lang.Object");
    }
}
