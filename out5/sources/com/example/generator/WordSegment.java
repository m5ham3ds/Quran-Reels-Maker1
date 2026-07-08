package com.example.generator;

import kotlin.Metadata;
import kotlin.jvm.internal.Intrinsics;
/* compiled from: VideoGenerator.kt */
@Metadata(d1 = {"\u0000(\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0000\n\u0002\u0010\b\n\u0000\n\u0002\u0010\t\n\u0002\b\u0002\n\u0002\u0010\u000e\n\u0002\b\u000f\n\u0002\u0010\u000b\n\u0002\b\u0004\b\u0087\b\u0018\u00002\u00020\u0001B)\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u0012\u0006\u0010\u0004\u001a\u00020\u0005\u0012\u0006\u0010\u0006\u001a\u00020\u0005\u0012\b\b\u0002\u0010\u0007\u001a\u00020\b¢\u0006\u0004\b\t\u0010\nJ\t\u0010\u0012\u001a\u00020\u0003HÆ\u0003J\t\u0010\u0013\u001a\u00020\u0005HÆ\u0003J\t\u0010\u0014\u001a\u00020\u0005HÆ\u0003J\t\u0010\u0015\u001a\u00020\bHÆ\u0003J1\u0010\u0016\u001a\u00020\u00002\b\b\u0002\u0010\u0002\u001a\u00020\u00032\b\b\u0002\u0010\u0004\u001a\u00020\u00052\b\b\u0002\u0010\u0006\u001a\u00020\u00052\b\b\u0002\u0010\u0007\u001a\u00020\bHÆ\u0001J\u0013\u0010\u0017\u001a\u00020\u00182\b\u0010\u0019\u001a\u0004\u0018\u00010\u0001HÖ\u0003J\t\u0010\u001a\u001a\u00020\u0003HÖ\u0001J\t\u0010\u001b\u001a\u00020\bHÖ\u0001R\u0011\u0010\u0002\u001a\u00020\u0003¢\u0006\b\n\u0000\u001a\u0004\b\u000b\u0010\fR\u0011\u0010\u0004\u001a\u00020\u0005¢\u0006\b\n\u0000\u001a\u0004\b\r\u0010\u000eR\u0011\u0010\u0006\u001a\u00020\u0005¢\u0006\b\n\u0000\u001a\u0004\b\u000f\u0010\u000eR\u0011\u0010\u0007\u001a\u00020\b¢\u0006\b\n\u0000\u001a\u0004\b\u0010\u0010\u0011¨\u0006\u001c"}, d2 = {"Lcom/example/generator/WordSegment;", "", "wordIndex", "", "startTimeMs", "", "endTimeMs", "word", "", "<init>", "(IJJLjava/lang/String;)V", "getWordIndex", "()I", "getStartTimeMs", "()J", "getEndTimeMs", "getWord", "()Ljava/lang/String;", "component1", "component2", "component3", "component4", "copy", "equals", "", "other", "hashCode", "toString", "app"}, k = 1, mv = {2, 2, 0}, xi = 48)
/* loaded from: /app/applet/classes5.dex */
public final class WordSegment {
    public static final int $stable = 0;
    private final long endTimeMs;
    private final long startTimeMs;
    private final String word;
    private final int wordIndex;

    public static /* synthetic */ WordSegment copy$default(WordSegment wordSegment, int i, long j, long j2, String str, int i2, Object obj) {
        if ((i2 & 1) != 0) {
            i = wordSegment.wordIndex;
        }
        if ((i2 & 2) != 0) {
            j = wordSegment.startTimeMs;
        }
        if ((i2 & 4) != 0) {
            j2 = wordSegment.endTimeMs;
        }
        if ((i2 & 8) != 0) {
            str = wordSegment.word;
        }
        String str2 = str;
        return wordSegment.copy(i, j, j2, str2);
    }

    public final int component1() {
        return this.wordIndex;
    }

    public final long component2() {
        return this.startTimeMs;
    }

    public final long component3() {
        return this.endTimeMs;
    }

    public final String component4() {
        return this.word;
    }

    public final WordSegment copy(int i, long j, long j2, String str) {
        Intrinsics.checkNotNullParameter(str, "word");
        return new WordSegment(i, j, j2, str);
    }

    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj instanceof WordSegment) {
            WordSegment wordSegment = (WordSegment) obj;
            return this.wordIndex == wordSegment.wordIndex && this.startTimeMs == wordSegment.startTimeMs && this.endTimeMs == wordSegment.endTimeMs && Intrinsics.areEqual(this.word, wordSegment.word);
        }
        return false;
    }

    public int hashCode() {
        return (((((Integer.hashCode(this.wordIndex) * 31) + Long.hashCode(this.startTimeMs)) * 31) + Long.hashCode(this.endTimeMs)) * 31) + this.word.hashCode();
    }

    public String toString() {
        int i = this.wordIndex;
        long j = this.startTimeMs;
        long j2 = this.endTimeMs;
        return "WordSegment(wordIndex=" + i + ", startTimeMs=" + j + ", endTimeMs=" + j2 + ", word=" + this.word + ")";
    }

    public WordSegment(int wordIndex, long startTimeMs, long endTimeMs, String word) {
        Intrinsics.checkNotNullParameter(word, "word");
        this.wordIndex = wordIndex;
        this.startTimeMs = startTimeMs;
        this.endTimeMs = endTimeMs;
        this.word = word;
    }

    /* JADX WARN: Illegal instructions before constructor call */
    /*
        Code decompiled incorrectly, please refer to instructions dump.
        To view partially-correct add '--show-bad-code' argument
    */
    public /* synthetic */ WordSegment(int r8, long r9, long r11, java.lang.String r13, int r14, kotlin.jvm.internal.DefaultConstructorMarker r15) {
        /*
            r7 = this;
            r14 = r14 & 8
            if (r14 == 0) goto L8
            java.lang.String r13 = ""
            r6 = r13
            goto L9
        L8:
            r6 = r13
        L9:
            r0 = r7
            r1 = r8
            r2 = r9
            r4 = r11
            r0.<init>(r1, r2, r4, r6)
            return
        */
        throw new UnsupportedOperationException("Method not decompiled: com.example.generator.WordSegment.<init>(int, long, long, java.lang.String, int, kotlin.jvm.internal.DefaultConstructorMarker):void");
    }

    public final int getWordIndex() {
        return this.wordIndex;
    }

    public final long getStartTimeMs() {
        return this.startTimeMs;
    }

    public final long getEndTimeMs() {
        return this.endTimeMs;
    }

    public final String getWord() {
        return this.word;
    }
}
