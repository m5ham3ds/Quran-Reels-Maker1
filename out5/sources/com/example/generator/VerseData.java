package com.example.generator;

import java.util.List;
import kotlin.Metadata;
import kotlin.Pair;
import kotlin.jvm.internal.Intrinsics;
/* compiled from: VideoGenerator.kt */
@Metadata(d1 = {"\u0000D\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0000\n\u0002\u0010\u000e\n\u0002\b\u0004\n\u0002\u0010\t\n\u0000\n\u0002\u0010 \n\u0002\u0018\u0002\n\u0002\u0010\u0007\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0017\n\u0002\u0010\u000b\n\u0002\b\u0002\n\u0002\u0010\b\n\u0002\b\u0002\b\u0087\b\u0018\u00002\u00020\u0001Bk\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u0012\u0006\u0010\u0004\u001a\u00020\u0003\u0012\b\u0010\u0005\u001a\u0004\u0018\u00010\u0003\u0012\u0006\u0010\u0006\u001a\u00020\u0003\u0012\u0006\u0010\u0007\u001a\u00020\b\u0012\u0018\u0010\t\u001a\u0014\u0012\u0010\u0012\u000e\u0012\u0004\u0012\u00020\b\u0012\u0004\u0012\u00020\f0\u000b0\n\u0012\u000e\b\u0002\u0010\r\u001a\b\u0012\u0004\u0012\u00020\u000e0\n\u0012\u000e\b\u0002\u0010\u000f\u001a\b\u0012\u0004\u0012\u00020\u00100\n¢\u0006\u0004\b\u0011\u0010\u0012J\t\u0010\u001e\u001a\u00020\u0003HÆ\u0003J\t\u0010\u001f\u001a\u00020\u0003HÆ\u0003J\u000b\u0010 \u001a\u0004\u0018\u00010\u0003HÆ\u0003J\t\u0010!\u001a\u00020\u0003HÆ\u0003J\t\u0010\"\u001a\u00020\bHÆ\u0003J\u001b\u0010#\u001a\u0014\u0012\u0010\u0012\u000e\u0012\u0004\u0012\u00020\b\u0012\u0004\u0012\u00020\f0\u000b0\nHÆ\u0003J\u000f\u0010$\u001a\b\u0012\u0004\u0012\u00020\u000e0\nHÆ\u0003J\u000f\u0010%\u001a\b\u0012\u0004\u0012\u00020\u00100\nHÆ\u0003Jy\u0010&\u001a\u00020\u00002\b\b\u0002\u0010\u0002\u001a\u00020\u00032\b\b\u0002\u0010\u0004\u001a\u00020\u00032\n\b\u0002\u0010\u0005\u001a\u0004\u0018\u00010\u00032\b\b\u0002\u0010\u0006\u001a\u00020\u00032\b\b\u0002\u0010\u0007\u001a\u00020\b2\u001a\b\u0002\u0010\t\u001a\u0014\u0012\u0010\u0012\u000e\u0012\u0004\u0012\u00020\b\u0012\u0004\u0012\u00020\f0\u000b0\n2\u000e\b\u0002\u0010\r\u001a\b\u0012\u0004\u0012\u00020\u000e0\n2\u000e\b\u0002\u0010\u000f\u001a\b\u0012\u0004\u0012\u00020\u00100\nHÆ\u0001J\u0013\u0010'\u001a\u00020(2\b\u0010)\u001a\u0004\u0018\u00010\u0001HÖ\u0003J\t\u0010*\u001a\u00020+HÖ\u0001J\t\u0010,\u001a\u00020\u0003HÖ\u0001R\u0011\u0010\u0002\u001a\u00020\u0003¢\u0006\b\n\u0000\u001a\u0004\b\u0013\u0010\u0014R\u0011\u0010\u0004\u001a\u00020\u0003¢\u0006\b\n\u0000\u001a\u0004\b\u0015\u0010\u0014R\u0013\u0010\u0005\u001a\u0004\u0018\u00010\u0003¢\u0006\b\n\u0000\u001a\u0004\b\u0016\u0010\u0014R\u0011\u0010\u0006\u001a\u00020\u0003¢\u0006\b\n\u0000\u001a\u0004\b\u0017\u0010\u0014R\u0011\u0010\u0007\u001a\u00020\b¢\u0006\b\n\u0000\u001a\u0004\b\u0018\u0010\u0019R#\u0010\t\u001a\u0014\u0012\u0010\u0012\u000e\u0012\u0004\u0012\u00020\b\u0012\u0004\u0012\u00020\f0\u000b0\n¢\u0006\b\n\u0000\u001a\u0004\b\u001a\u0010\u001bR\u0017\u0010\r\u001a\b\u0012\u0004\u0012\u00020\u000e0\n¢\u0006\b\n\u0000\u001a\u0004\b\u001c\u0010\u001bR\u0017\u0010\u000f\u001a\b\u0012\u0004\u0012\u00020\u00100\n¢\u0006\b\n\u0000\u001a\u0004\b\u001d\u0010\u001b¨\u0006-"}, d2 = {"Lcom/example/generator/VerseData;", "", "surahName", "", "text", "translation", "audioPath", "durationUs", "", "energyTimeline", "", "Lkotlin/Pair;", "", "wordSegments", "Lcom/example/generator/WordSegment;", "chunks", "Lcom/example/generator/SmartChunk;", "<init>", "(Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;JLjava/util/List;Ljava/util/List;Ljava/util/List;)V", "getSurahName", "()Ljava/lang/String;", "getText", "getTranslation", "getAudioPath", "getDurationUs", "()J", "getEnergyTimeline", "()Ljava/util/List;", "getWordSegments", "getChunks", "component1", "component2", "component3", "component4", "component5", "component6", "component7", "component8", "copy", "equals", "", "other", "hashCode", "", "toString", "app"}, k = 1, mv = {2, 2, 0}, xi = 48)
/* loaded from: /app/applet/classes5.dex */
public final class VerseData {
    public static final int $stable = 8;
    private final String audioPath;
    private final List<SmartChunk> chunks;
    private final long durationUs;
    private final List<Pair<Long, Float>> energyTimeline;
    private final String surahName;
    private final String text;
    private final String translation;
    private final List<WordSegment> wordSegments;

    public static /* synthetic */ VerseData copy$default(VerseData verseData, String str, String str2, String str3, String str4, long j, List list, List list2, List list3, int i, Object obj) {
        if ((i & 1) != 0) {
            str = verseData.surahName;
        }
        if ((i & 2) != 0) {
            str2 = verseData.text;
        }
        if ((i & 4) != 0) {
            str3 = verseData.translation;
        }
        if ((i & 8) != 0) {
            str4 = verseData.audioPath;
        }
        if ((i & 16) != 0) {
            j = verseData.durationUs;
        }
        List<Pair<Long, Float>> list4 = list;
        if ((i & 32) != 0) {
            list4 = verseData.energyTimeline;
        }
        List<WordSegment> list5 = list2;
        if ((i & 64) != 0) {
            list5 = verseData.wordSegments;
        }
        List<SmartChunk> list6 = list3;
        if ((i & 128) != 0) {
            list6 = verseData.chunks;
        }
        List list7 = list6;
        List list8 = list4;
        long j2 = j;
        String str5 = str3;
        String str6 = str4;
        return verseData.copy(str, str2, str5, str6, j2, list8, list5, list7);
    }

    public final String component1() {
        return this.surahName;
    }

    public final String component2() {
        return this.text;
    }

    public final String component3() {
        return this.translation;
    }

    public final String component4() {
        return this.audioPath;
    }

    public final long component5() {
        return this.durationUs;
    }

    public final List<Pair<Long, Float>> component6() {
        return this.energyTimeline;
    }

    public final List<WordSegment> component7() {
        return this.wordSegments;
    }

    public final List<SmartChunk> component8() {
        return this.chunks;
    }

    public final VerseData copy(String str, String str2, String str3, String str4, long j, List<Pair<Long, Float>> list, List<WordSegment> list2, List<SmartChunk> list3) {
        Intrinsics.checkNotNullParameter(str, "surahName");
        Intrinsics.checkNotNullParameter(str2, "text");
        Intrinsics.checkNotNullParameter(str4, "audioPath");
        Intrinsics.checkNotNullParameter(list, "energyTimeline");
        Intrinsics.checkNotNullParameter(list2, "wordSegments");
        Intrinsics.checkNotNullParameter(list3, "chunks");
        return new VerseData(str, str2, str3, str4, j, list, list2, list3);
    }

    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj instanceof VerseData) {
            VerseData verseData = (VerseData) obj;
            return Intrinsics.areEqual(this.surahName, verseData.surahName) && Intrinsics.areEqual(this.text, verseData.text) && Intrinsics.areEqual(this.translation, verseData.translation) && Intrinsics.areEqual(this.audioPath, verseData.audioPath) && this.durationUs == verseData.durationUs && Intrinsics.areEqual(this.energyTimeline, verseData.energyTimeline) && Intrinsics.areEqual(this.wordSegments, verseData.wordSegments) && Intrinsics.areEqual(this.chunks, verseData.chunks);
        }
        return false;
    }

    public int hashCode() {
        return (((((((((((((this.surahName.hashCode() * 31) + this.text.hashCode()) * 31) + (this.translation == null ? 0 : this.translation.hashCode())) * 31) + this.audioPath.hashCode()) * 31) + Long.hashCode(this.durationUs)) * 31) + this.energyTimeline.hashCode()) * 31) + this.wordSegments.hashCode()) * 31) + this.chunks.hashCode();
    }

    public String toString() {
        String str = this.surahName;
        String str2 = this.text;
        String str3 = this.translation;
        String str4 = this.audioPath;
        long j = this.durationUs;
        List<Pair<Long, Float>> list = this.energyTimeline;
        List<WordSegment> list2 = this.wordSegments;
        return "VerseData(surahName=" + str + ", text=" + str2 + ", translation=" + str3 + ", audioPath=" + str4 + ", durationUs=" + j + ", energyTimeline=" + list + ", wordSegments=" + list2 + ", chunks=" + this.chunks + ")";
    }

    public VerseData(String surahName, String text, String translation, String audioPath, long durationUs, List<Pair<Long, Float>> list, List<WordSegment> list2, List<SmartChunk> list3) {
        Intrinsics.checkNotNullParameter(surahName, "surahName");
        Intrinsics.checkNotNullParameter(text, "text");
        Intrinsics.checkNotNullParameter(audioPath, "audioPath");
        Intrinsics.checkNotNullParameter(list, "energyTimeline");
        Intrinsics.checkNotNullParameter(list2, "wordSegments");
        Intrinsics.checkNotNullParameter(list3, "chunks");
        this.surahName = surahName;
        this.text = text;
        this.translation = translation;
        this.audioPath = audioPath;
        this.durationUs = durationUs;
        this.energyTimeline = list;
        this.wordSegments = list2;
        this.chunks = list3;
    }

    /* JADX WARN: Illegal instructions before constructor call */
    /*
        Code decompiled incorrectly, please refer to instructions dump.
        To view partially-correct add '--show-bad-code' argument
    */
    public /* synthetic */ VerseData(java.lang.String r13, java.lang.String r14, java.lang.String r15, java.lang.String r16, long r17, java.util.List r19, java.util.List r20, java.util.List r21, int r22, kotlin.jvm.internal.DefaultConstructorMarker r23) {
        /*
            r12 = this;
            r0 = r22
            r1 = r0 & 64
            if (r1 == 0) goto Lc
            java.util.List r1 = kotlin.collections.CollectionsKt.emptyList()
            r10 = r1
            goto Le
        Lc:
            r10 = r20
        Le:
            r0 = r0 & 128(0x80, float:1.8E-43)
            if (r0 == 0) goto L18
            java.util.List r0 = kotlin.collections.CollectionsKt.emptyList()
            r11 = r0
            goto L1a
        L18:
            r11 = r21
        L1a:
            r2 = r12
            r3 = r13
            r4 = r14
            r5 = r15
            r6 = r16
            r7 = r17
            r9 = r19
            r2.<init>(r3, r4, r5, r6, r7, r9, r10, r11)
            return
        */
        throw new UnsupportedOperationException("Method not decompiled: com.example.generator.VerseData.<init>(java.lang.String, java.lang.String, java.lang.String, java.lang.String, long, java.util.List, java.util.List, java.util.List, int, kotlin.jvm.internal.DefaultConstructorMarker):void");
    }

    public final String getSurahName() {
        return this.surahName;
    }

    public final String getText() {
        return this.text;
    }

    public final String getTranslation() {
        return this.translation;
    }

    public final String getAudioPath() {
        return this.audioPath;
    }

    public final long getDurationUs() {
        return this.durationUs;
    }

    public final List<Pair<Long, Float>> getEnergyTimeline() {
        return this.energyTimeline;
    }

    public final List<WordSegment> getWordSegments() {
        return this.wordSegments;
    }

    public final List<SmartChunk> getChunks() {
        return this.chunks;
    }
}
