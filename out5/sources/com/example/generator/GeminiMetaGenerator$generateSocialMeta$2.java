package com.example.generator;

import android.content.Context;
import kotlin.Metadata;
import kotlin.Unit;
import kotlin.coroutines.Continuation;
import kotlin.coroutines.jvm.internal.DebugMetadata;
import kotlin.coroutines.jvm.internal.SuspendLambda;
import kotlin.jvm.functions.Function2;
import kotlinx.coroutines.CoroutineScope;
/* compiled from: GeminiMetaGenerator.kt */
@Metadata(d1 = {"\u0000\n\n\u0000\n\u0002\u0018\u0002\n\u0002\u0018\u0002\u0010\u0000\u001a\u0004\u0018\u00010\u0001*\u00020\u0002H\n"}, d2 = {"<anonymous>", "Lcom/example/generator/GeneratedMetaResult;", "Lkotlinx/coroutines/CoroutineScope;"}, k = 3, mv = {2, 2, 0}, xi = 48)
@DebugMetadata(c = "com.example.generator.GeminiMetaGenerator$generateSocialMeta$2", f = "GeminiMetaGenerator.kt", i = {0, 1, 1, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5}, l = {358, 359, 447, 497, 503, 511}, m = "invokeSuspend", n = {"settingsManager", "settingsManager", "apiKey", "settingsManager", "apiKey", "geminiModel", "prompt", "jsonRequest", "requestBody", "url", "request", "response", "responseStr", "rootJson", "errMsg", "attempt", "maxAttempts", "settingsManager", "apiKey", "geminiModel", "prompt", "jsonRequest", "requestBody", "url", "request", "response", "attempt", "maxAttempts", "settingsManager", "apiKey", "geminiModel", "prompt", "jsonRequest", "requestBody", "url", "request", "response", "attempt", "maxAttempts", "settingsManager", "apiKey", "geminiModel", "prompt", "jsonRequest", "requestBody", "url", "request", "e", "attempt", "maxAttempts"}, s = {"L$0", "L$0", "L$1", "L$0", "L$1", "L$2", "L$3", "L$4", "L$5", "L$6", "L$7", "L$8", "L$9", "L$10", "L$11", "I$0", "I$1", "L$0", "L$1", "L$2", "L$3", "L$4", "L$5", "L$6", "L$7", "L$8", "I$0", "I$1", "L$0", "L$1", "L$2", "L$3", "L$4", "L$5", "L$6", "L$7", "L$8", "I$0", "I$1", "L$0", "L$1", "L$2", "L$3", "L$4", "L$5", "L$6", "L$7", "L$8", "I$0", "I$1"})
/* loaded from: /app/applet/classes5.dex */
final class GeminiMetaGenerator$generateSocialMeta$2 extends SuspendLambda implements Function2<CoroutineScope, Continuation<? super GeneratedMetaResult>, Object> {
    final /* synthetic */ Context $context;
    final /* synthetic */ int $endAyah;
    final /* synthetic */ boolean $isFacebook;
    final /* synthetic */ boolean $isInstagram;
    final /* synthetic */ boolean $isTiktok;
    final /* synthetic */ boolean $isYoutube;
    final /* synthetic */ String $reciterName;
    final /* synthetic */ int $startAyah;
    final /* synthetic */ String $surahName;
    int I$0;
    int I$1;
    Object L$0;
    Object L$1;
    Object L$10;
    Object L$11;
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
    public GeminiMetaGenerator$generateSocialMeta$2(Context context, String str, int i, int i2, String str2, boolean z, boolean z2, boolean z3, boolean z4, GeminiMetaGenerator geminiMetaGenerator, Continuation<? super GeminiMetaGenerator$generateSocialMeta$2> continuation) {
        super(2, continuation);
        this.$context = context;
        this.$surahName = str;
        this.$startAyah = i;
        this.$endAyah = i2;
        this.$reciterName = str2;
        this.$isTiktok = z;
        this.$isInstagram = z2;
        this.$isFacebook = z3;
        this.$isYoutube = z4;
        this.this$0 = geminiMetaGenerator;
    }

    public final Continuation<Unit> create(Object obj, Continuation<?> continuation) {
        return new GeminiMetaGenerator$generateSocialMeta$2(this.$context, this.$surahName, this.$startAyah, this.$endAyah, this.$reciterName, this.$isTiktok, this.$isInstagram, this.$isFacebook, this.$isYoutube, this.this$0, continuation);
    }

    public final Object invoke(CoroutineScope coroutineScope, Continuation<? super GeneratedMetaResult> continuation) {
        return create(coroutineScope, continuation).invokeSuspend(Unit.INSTANCE);
    }

    /* JADX WARN: Can't wrap try/catch for region: R(21:73|74|(3:76|77|78)(3:188|189|(1:191)(1:192))|79|80|(4:82|83|84|(24:86|87|88|89|90|91|92|93|94|95|97|98|(4:100|101|102|(17:104|105|106|107|108|109|110|112|113|(4:115|116|117|(13:119|120|121|122|123|124|125|127|128|(4:130|131|132|(3:134|135|136))(1:142)|138|135|136)(1:152))|156|127|128|(0)(0)|138|135|136)(1:165))|169|112|113|(0)|156|127|128|(0)(0)|138|135|136)(1:182))(1:187)|183|97|98|(0)|169|112|113|(0)|156|127|128|(0)(0)|138|135|136) */
    /* JADX WARN: Can't wrap try/catch for region: R(9:(1:198)|199|200|201|202|203|204|205|(2:213|(1:215)(3:216|217|(2:10|11)(0)))) */
    /* JADX WARN: Can't wrap try/catch for region: R(9:198|199|200|201|202|203|204|205|(2:213|(1:215)(3:216|217|(2:10|11)(0)))) */
    /* JADX WARN: Code restructure failed: missing block: B:180:0x06fa, code lost:
        r0 = e;
     */
    /* JADX WARN: Code restructure failed: missing block: B:181:0x06fb, code lost:
        r6 = r23;
     */
    /* JADX WARN: Code restructure failed: missing block: B:184:0x0706, code lost:
        r0 = e;
     */
    /* JADX WARN: Code restructure failed: missing block: B:185:0x0707, code lost:
        r6 = r23;
     */
    /* JADX WARN: Code restructure failed: missing block: B:186:0x070d, code lost:
        r0 = e;
     */
    /* JADX WARN: Code restructure failed: missing block: B:187:0x070e, code lost:
        r41 = r15;
        r6 = r23;
     */
    /* JADX WARN: Code restructure failed: missing block: B:192:0x073a, code lost:
        r2 = r45;
        r15 = r14;
     */
    /* JADX WARN: Code restructure failed: missing block: B:45:0x03f8, code lost:
        if (r5 == null) goto L61;
     */
    /* JADX WARN: Code restructure failed: missing block: B:70:0x04f2, code lost:
        r0 = e;
     */
    /* JADX WARN: Code restructure failed: missing block: B:71:0x04f3, code lost:
        r2 = r15;
        r15 = r14;
        r14 = r2;
        r2 = r45;
        r38 = r6;
        r39 = r12;
        r40 = r13;
        r6 = r23;
        r37 = r26;
     */
    /* JADX WARN: Code restructure failed: missing block: B:72:0x0504, code lost:
        r0 = e;
     */
    /* JADX WARN: Code restructure failed: missing block: B:73:0x0505, code lost:
        r31 = r4;
        r34 = r7;
        r2 = r15;
        r15 = r14;
        r14 = r2;
        r2 = r45;
        r38 = r6;
        r39 = r12;
        r40 = r13;
        r6 = r23;
        r37 = r26;
     */
    /* JADX WARN: Not initialized variable reg: 14, insn: 0x012f: MOVE  (r15 I:??[OBJECT, ARRAY] A[D('url' java.lang.String)]) = (r14 I:??[OBJECT, ARRAY] A[D('request' okhttp3.Request)]), block:B:14:0x012d */
    /* JADX WARN: Not initialized variable reg: 15, insn: 0x012d: MOVE  (r19 I:??[OBJECT, ARRAY]) = (r15 I:??[OBJECT, ARRAY] A[D('url' java.lang.String)]), block:B:14:0x012d */
    /* JADX WARN: Not initialized variable reg: 23, insn: 0x013c: MOVE  (r30 I:??[OBJECT, ARRAY]) = (r23 I:??[OBJECT, ARRAY] A[D('geminiModel' java.lang.String)]), block:B:14:0x012d */
    /* JADX WARN: Not initialized variable reg: 24, insn: 0x013e: MOVE  (r19 I:??[OBJECT, ARRAY]) = (r24 I:??[OBJECT, ARRAY] A[D('apiKey' java.lang.String)]), block:B:14:0x012d */
    /* JADX WARN: Not initialized variable reg: 25, insn: 0x0140: MOVE  (r31 I:??[OBJECT, ARRAY]) = (r25 I:??[OBJECT, ARRAY] A[D('settingsManager' com.example.settings.SettingsManager)]), block:B:14:0x012d */
    /* JADX WARN: Removed duplicated region for block: B:127:0x0636  */
    /* JADX WARN: Removed duplicated region for block: B:148:0x067c  */
    /* JADX WARN: Removed duplicated region for block: B:170:0x06c7  */
    /* JADX WARN: Removed duplicated region for block: B:174:0x06ee  */
    /* JADX WARN: Removed duplicated region for block: B:229:0x0886  */
    /* JADX WARN: Removed duplicated region for block: B:237:0x08f9  */
    /* JADX WARN: Removed duplicated region for block: B:275:0x03d8 A[EXC_TOP_SPLITTER, SYNTHETIC] */
    /* JADX WARN: Removed duplicated region for block: B:27:0x022d A[RETURN] */
    /* JADX WARN: Removed duplicated region for block: B:28:0x022e  */
    /* JADX WARN: Removed duplicated region for block: B:31:0x023e  */
    /* JADX WARN: Unsupported multi-entry loop pattern (BACK_EDGE: B:206:0x07cf -> B:38:0x03d5). Please submit an issue!!! */
    /* JADX WARN: Unsupported multi-entry loop pattern (BACK_EDGE: B:216:0x0844 -> B:38:0x03d5). Please submit an issue!!! */
    /* JADX WARN: Unsupported multi-entry loop pattern (BACK_EDGE: B:234:0x08d9 -> B:235:0x08e2). Please submit an issue!!! */
    /* JADX WARN: Unsupported multi-entry loop pattern (BACK_EDGE: B:69:0x04e4 -> B:38:0x03d5). Please submit an issue!!! */
    /*
        Code decompiled incorrectly, please refer to instructions dump.
        To view partially-correct add '--show-bad-code' argument
    */
    public final java.lang.Object invokeSuspend(java.lang.Object r45) {
        /*
            Method dump skipped, instructions count: 2336
            To view this dump add '--comments-level debug' option
        */
        throw new UnsupportedOperationException("Method not decompiled: com.example.generator.GeminiMetaGenerator$generateSocialMeta$2.invokeSuspend(java.lang.Object):java.lang.Object");
    }
}
