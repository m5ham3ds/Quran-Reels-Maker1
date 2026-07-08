package com.example.generator;

import kotlin.Metadata;
import kotlin.jvm.internal.Intrinsics;
/* compiled from: VideoGenerator.kt */
@Metadata(d1 = {"\u0000*\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0000\n\u0002\u0010\u000e\n\u0002\b\u0002\n\u0002\u0010\t\n\u0002\b\u000f\n\u0002\u0010\u000b\n\u0002\b\u0002\n\u0002\u0010\b\n\u0002\b\u0002\b\u0087\b\u0018\u00002\u00020\u0001B)\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u0012\b\u0010\u0004\u001a\u0004\u0018\u00010\u0003\u0012\u0006\u0010\u0005\u001a\u00020\u0006\u0012\u0006\u0010\u0007\u001a\u00020\u0006¢\u0006\u0004\b\b\u0010\tJ\t\u0010\u0010\u001a\u00020\u0003HÆ\u0003J\u000b\u0010\u0011\u001a\u0004\u0018\u00010\u0003HÆ\u0003J\t\u0010\u0012\u001a\u00020\u0006HÆ\u0003J\t\u0010\u0013\u001a\u00020\u0006HÆ\u0003J3\u0010\u0014\u001a\u00020\u00002\b\b\u0002\u0010\u0002\u001a\u00020\u00032\n\b\u0002\u0010\u0004\u001a\u0004\u0018\u00010\u00032\b\b\u0002\u0010\u0005\u001a\u00020\u00062\b\b\u0002\u0010\u0007\u001a\u00020\u0006HÆ\u0001J\u0013\u0010\u0015\u001a\u00020\u00162\b\u0010\u0017\u001a\u0004\u0018\u00010\u0001HÖ\u0003J\t\u0010\u0018\u001a\u00020\u0019HÖ\u0001J\t\u0010\u001a\u001a\u00020\u0003HÖ\u0001R\u0011\u0010\u0002\u001a\u00020\u0003¢\u0006\b\n\u0000\u001a\u0004\b\n\u0010\u000bR\u0013\u0010\u0004\u001a\u0004\u0018\u00010\u0003¢\u0006\b\n\u0000\u001a\u0004\b\f\u0010\u000bR\u0011\u0010\u0005\u001a\u00020\u0006¢\u0006\b\n\u0000\u001a\u0004\b\r\u0010\u000eR\u0011\u0010\u0007\u001a\u00020\u0006¢\u0006\b\n\u0000\u001a\u0004\b\u000f\u0010\u000e¨\u0006\u001b"}, d2 = {"Lcom/example/generator/SmartChunk;", "", "arabic", "", "english", "startTimeMs", "", "endTimeMs", "<init>", "(Ljava/lang/String;Ljava/lang/String;JJ)V", "getArabic", "()Ljava/lang/String;", "getEnglish", "getStartTimeMs", "()J", "getEndTimeMs", "component1", "component2", "component3", "component4", "copy", "equals", "", "other", "hashCode", "", "toString", "app"}, k = 1, mv = {2, 2, 0}, xi = 48)
/* loaded from: /app/applet/classes5.dex */
public final class SmartChunk {
    public static final int $stable = 0;
    private final String arabic;
    private final long endTimeMs;
    private final String english;
    private final long startTimeMs;

    public static /* synthetic */ SmartChunk copy$default(SmartChunk smartChunk, String str, String str2, long j, long j2, int i, Object obj) {
        if ((i & 1) != 0) {
            str = smartChunk.arabic;
        }
        if ((i & 2) != 0) {
            str2 = smartChunk.english;
        }
        if ((i & 4) != 0) {
            j = smartChunk.startTimeMs;
        }
        if ((i & 8) != 0) {
            j2 = smartChunk.endTimeMs;
        }
        long j3 = j2;
        return smartChunk.copy(str, str2, j, j3);
    }

    public final String component1() {
        return this.arabic;
    }

    public final String component2() {
        return this.english;
    }

    public final long component3() {
        return this.startTimeMs;
    }

    public final long component4() {
        return this.endTimeMs;
    }

    public final SmartChunk copy(String str, String str2, long j, long j2) {
        Intrinsics.checkNotNullParameter(str, "arabic");
        return new SmartChunk(str, str2, j, j2);
    }

    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj instanceof SmartChunk) {
            SmartChunk smartChunk = (SmartChunk) obj;
            return Intrinsics.areEqual(this.arabic, smartChunk.arabic) && Intrinsics.areEqual(this.english, smartChunk.english) && this.startTimeMs == smartChunk.startTimeMs && this.endTimeMs == smartChunk.endTimeMs;
        }
        return false;
    }

    public int hashCode() {
        return (((((this.arabic.hashCode() * 31) + (this.english == null ? 0 : this.english.hashCode())) * 31) + Long.hashCode(this.startTimeMs)) * 31) + Long.hashCode(this.endTimeMs);
    }

    public String toString() {
        String str = this.arabic;
        String str2 = this.english;
        long j = this.startTimeMs;
        return "SmartChunk(arabic=" + str + ", english=" + str2 + ", startTimeMs=" + j + ", endTimeMs=" + this.endTimeMs + ")";
    }

    public SmartChunk(String arabic, String english, long startTimeMs, long endTimeMs) {
        Intrinsics.checkNotNullParameter(arabic, "arabic");
        this.arabic = arabic;
        this.english = english;
        this.startTimeMs = startTimeMs;
        this.endTimeMs = endTimeMs;
    }

    public final String getArabic() {
        return this.arabic;
    }

    public final String getEnglish() {
        return this.english;
    }

    public final long getStartTimeMs() {
        return this.startTimeMs;
    }

    public final long getEndTimeMs() {
        return this.endTimeMs;
    }
}
