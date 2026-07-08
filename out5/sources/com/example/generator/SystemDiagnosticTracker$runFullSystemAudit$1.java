package com.example.generator;

import kotlin.Metadata;
import kotlin.coroutines.Continuation;
import kotlin.coroutines.jvm.internal.ContinuationImpl;
import kotlin.coroutines.jvm.internal.DebugMetadata;
/* JADX INFO: Access modifiers changed from: package-private */
/* compiled from: SystemDiagnosticTracker.kt */
@Metadata(k = 3, mv = {2, 2, 0}, xi = 48)
@DebugMetadata(c = "com.example.generator.SystemDiagnosticTracker", f = "SystemDiagnosticTracker.kt", i = {0, 0, 0, 0, 0, 0, 0, 0, 1, 1, 1, 1, 1}, l = {83, 108}, m = "runFullSystemAudit", n = {"context", "sb", "timestamp", "endpoints", "host", "url", "reqBuilder", "settingsManager", "context", "sb", "timestamp", "endpoints", "settingsManager"}, s = {"L$0", "L$1", "L$2", "L$3", "L$5", "L$6", "L$7", "L$8", "L$0", "L$1", "L$2", "L$3", "L$4"})
/* loaded from: /app/applet/classes5.dex */
public final class SystemDiagnosticTracker$runFullSystemAudit$1 extends ContinuationImpl {
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
    /* synthetic */ Object result;
    final /* synthetic */ SystemDiagnosticTracker this$0;

    /* JADX INFO: Access modifiers changed from: package-private */
    /* JADX WARN: 'super' call moved to the top of the method (can break code semantics) */
    public SystemDiagnosticTracker$runFullSystemAudit$1(SystemDiagnosticTracker systemDiagnosticTracker, Continuation<? super SystemDiagnosticTracker$runFullSystemAudit$1> continuation) {
        super(continuation);
        this.this$0 = systemDiagnosticTracker;
    }

    public final Object invokeSuspend(Object obj) {
        this.result = obj;
        this.label |= Integer.MIN_VALUE;
        return this.this$0.runFullSystemAudit(null, (Continuation) this);
    }
}
