package com.example.generator;

import kotlin.Metadata;
import kotlin.jvm.internal.DefaultConstructorMarker;
import kotlin.jvm.internal.Intrinsics;
/* compiled from: GeminiMetaGenerator.kt */
@Metadata(d1 = {"\u0000\"\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0000\n\u0002\u0010\b\n\u0002\b\u0003\n\u0002\u0010\u000e\n\u0002\b\u0017\n\u0002\u0010\u000b\n\u0002\b\u0004\b\u0087\b\u0018\u00002\u00020\u0001BE\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u0012\u0006\u0010\u0004\u001a\u00020\u0003\u0012\u0006\u0010\u0005\u001a\u00020\u0003\u0012\u0006\u0010\u0006\u001a\u00020\u0007\u0012\b\b\u0002\u0010\b\u001a\u00020\u0007\u0012\b\b\u0002\u0010\t\u001a\u00020\u0007\u0012\b\b\u0002\u0010\n\u001a\u00020\u0007¢\u0006\u0004\b\u000b\u0010\fJ\t\u0010\u0016\u001a\u00020\u0003HÆ\u0003J\t\u0010\u0017\u001a\u00020\u0003HÆ\u0003J\t\u0010\u0018\u001a\u00020\u0003HÆ\u0003J\t\u0010\u0019\u001a\u00020\u0007HÆ\u0003J\t\u0010\u001a\u001a\u00020\u0007HÆ\u0003J\t\u0010\u001b\u001a\u00020\u0007HÆ\u0003J\t\u0010\u001c\u001a\u00020\u0007HÆ\u0003JO\u0010\u001d\u001a\u00020\u00002\b\b\u0002\u0010\u0002\u001a\u00020\u00032\b\b\u0002\u0010\u0004\u001a\u00020\u00032\b\b\u0002\u0010\u0005\u001a\u00020\u00032\b\b\u0002\u0010\u0006\u001a\u00020\u00072\b\b\u0002\u0010\b\u001a\u00020\u00072\b\b\u0002\u0010\t\u001a\u00020\u00072\b\b\u0002\u0010\n\u001a\u00020\u0007HÆ\u0001J\u0013\u0010\u001e\u001a\u00020\u001f2\b\u0010 \u001a\u0004\u0018\u00010\u0001HÖ\u0003J\t\u0010!\u001a\u00020\u0003HÖ\u0001J\t\u0010\"\u001a\u00020\u0007HÖ\u0001R\u0011\u0010\u0002\u001a\u00020\u0003¢\u0006\b\n\u0000\u001a\u0004\b\r\u0010\u000eR\u0011\u0010\u0004\u001a\u00020\u0003¢\u0006\b\n\u0000\u001a\u0004\b\u000f\u0010\u000eR\u0011\u0010\u0005\u001a\u00020\u0003¢\u0006\b\n\u0000\u001a\u0004\b\u0010\u0010\u000eR\u0011\u0010\u0006\u001a\u00020\u0007¢\u0006\b\n\u0000\u001a\u0004\b\u0011\u0010\u0012R\u0011\u0010\b\u001a\u00020\u0007¢\u0006\b\n\u0000\u001a\u0004\b\u0013\u0010\u0012R\u0011\u0010\t\u001a\u00020\u0007¢\u0006\b\n\u0000\u001a\u0004\b\u0014\u0010\u0012R\u0011\u0010\n\u001a\u00020\u0007¢\u0006\b\n\u0000\u001a\u0004\b\u0015\u0010\u0012¨\u0006#"}, d2 = {"Lcom/example/generator/ClipAnalysisResult;", "", "surah", "", "startAyah", "endAyah", "reciterName", "", "title", "videoQuery", "category", "<init>", "(IIILjava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;)V", "getSurah", "()I", "getStartAyah", "getEndAyah", "getReciterName", "()Ljava/lang/String;", "getTitle", "getVideoQuery", "getCategory", "component1", "component2", "component3", "component4", "component5", "component6", "component7", "copy", "equals", "", "other", "hashCode", "toString", "app"}, k = 1, mv = {2, 2, 0}, xi = 48)
/* loaded from: /app/applet/classes5.dex */
public final class ClipAnalysisResult {
    public static final int $stable = 0;
    private final String category;
    private final int endAyah;
    private final String reciterName;
    private final int startAyah;
    private final int surah;
    private final String title;
    private final String videoQuery;

    public static /* synthetic */ ClipAnalysisResult copy$default(ClipAnalysisResult clipAnalysisResult, int i, int i2, int i3, String str, String str2, String str3, String str4, int i4, Object obj) {
        if ((i4 & 1) != 0) {
            i = clipAnalysisResult.surah;
        }
        if ((i4 & 2) != 0) {
            i2 = clipAnalysisResult.startAyah;
        }
        if ((i4 & 4) != 0) {
            i3 = clipAnalysisResult.endAyah;
        }
        if ((i4 & 8) != 0) {
            str = clipAnalysisResult.reciterName;
        }
        if ((i4 & 16) != 0) {
            str2 = clipAnalysisResult.title;
        }
        if ((i4 & 32) != 0) {
            str3 = clipAnalysisResult.videoQuery;
        }
        if ((i4 & 64) != 0) {
            str4 = clipAnalysisResult.category;
        }
        String str5 = str3;
        String str6 = str4;
        String str7 = str2;
        int i5 = i3;
        return clipAnalysisResult.copy(i, i2, i5, str, str7, str5, str6);
    }

    public final int component1() {
        return this.surah;
    }

    public final int component2() {
        return this.startAyah;
    }

    public final int component3() {
        return this.endAyah;
    }

    public final String component4() {
        return this.reciterName;
    }

    public final String component5() {
        return this.title;
    }

    public final String component6() {
        return this.videoQuery;
    }

    public final String component7() {
        return this.category;
    }

    public final ClipAnalysisResult copy(int i, int i2, int i3, String str, String str2, String str3, String str4) {
        Intrinsics.checkNotNullParameter(str, "reciterName");
        Intrinsics.checkNotNullParameter(str2, "title");
        Intrinsics.checkNotNullParameter(str3, "videoQuery");
        Intrinsics.checkNotNullParameter(str4, "category");
        return new ClipAnalysisResult(i, i2, i3, str, str2, str3, str4);
    }

    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj instanceof ClipAnalysisResult) {
            ClipAnalysisResult clipAnalysisResult = (ClipAnalysisResult) obj;
            return this.surah == clipAnalysisResult.surah && this.startAyah == clipAnalysisResult.startAyah && this.endAyah == clipAnalysisResult.endAyah && Intrinsics.areEqual(this.reciterName, clipAnalysisResult.reciterName) && Intrinsics.areEqual(this.title, clipAnalysisResult.title) && Intrinsics.areEqual(this.videoQuery, clipAnalysisResult.videoQuery) && Intrinsics.areEqual(this.category, clipAnalysisResult.category);
        }
        return false;
    }

    public int hashCode() {
        return (((((((((((Integer.hashCode(this.surah) * 31) + Integer.hashCode(this.startAyah)) * 31) + Integer.hashCode(this.endAyah)) * 31) + this.reciterName.hashCode()) * 31) + this.title.hashCode()) * 31) + this.videoQuery.hashCode()) * 31) + this.category.hashCode();
    }

    public String toString() {
        int i = this.surah;
        int i2 = this.startAyah;
        int i3 = this.endAyah;
        String str = this.reciterName;
        String str2 = this.title;
        String str3 = this.videoQuery;
        return "ClipAnalysisResult(surah=" + i + ", startAyah=" + i2 + ", endAyah=" + i3 + ", reciterName=" + str + ", title=" + str2 + ", videoQuery=" + str3 + ", category=" + this.category + ")";
    }

    public ClipAnalysisResult(int surah, int startAyah, int endAyah, String reciterName, String title, String videoQuery, String category) {
        Intrinsics.checkNotNullParameter(reciterName, "reciterName");
        Intrinsics.checkNotNullParameter(title, "title");
        Intrinsics.checkNotNullParameter(videoQuery, "videoQuery");
        Intrinsics.checkNotNullParameter(category, "category");
        this.surah = surah;
        this.startAyah = startAyah;
        this.endAyah = endAyah;
        this.reciterName = reciterName;
        this.title = title;
        this.videoQuery = videoQuery;
        this.category = category;
    }

    public /* synthetic */ ClipAnalysisResult(int i, int i2, int i3, String str, String str2, String str3, String str4, int i4, DefaultConstructorMarker defaultConstructorMarker) {
        this(i, i2, i3, str, (i4 & 16) != 0 ? "" : str2, (i4 & 32) != 0 ? "" : str3, (i4 & 64) != 0 ? "" : str4);
    }

    public final int getSurah() {
        return this.surah;
    }

    public final int getStartAyah() {
        return this.startAyah;
    }

    public final int getEndAyah() {
        return this.endAyah;
    }

    public final String getReciterName() {
        return this.reciterName;
    }

    public final String getTitle() {
        return this.title;
    }

    public final String getVideoQuery() {
        return this.videoQuery;
    }

    public final String getCategory() {
        return this.category;
    }
}
