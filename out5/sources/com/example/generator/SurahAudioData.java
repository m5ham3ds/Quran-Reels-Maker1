package com.example.generator;

import java.util.List;
import java.util.Map;
import kotlin.Metadata;
import kotlin.jvm.internal.Intrinsics;
/* compiled from: VideoGenerator.kt */
@Metadata(d1 = {"\u0000,\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0000\n\u0002\u0010$\n\u0002\u0010\b\n\u0002\u0010 \n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u000e\n\u0002\b\t\n\u0002\u0010\u000b\n\u0002\b\u0004\b\u0087\b\u0018\u00002\u00020\u0001B5\u0012\u0018\u0010\u0002\u001a\u0014\u0012\u0004\u0012\u00020\u0004\u0012\n\u0012\b\u0012\u0004\u0012\u00020\u00060\u00050\u0003\u0012\u0012\u0010\u0007\u001a\u000e\u0012\u0004\u0012\u00020\u0004\u0012\u0004\u0012\u00020\b0\u0003¢\u0006\u0004\b\t\u0010\nJ\u001b\u0010\u000e\u001a\u0014\u0012\u0004\u0012\u00020\u0004\u0012\n\u0012\b\u0012\u0004\u0012\u00020\u00060\u00050\u0003HÆ\u0003J\u0015\u0010\u000f\u001a\u000e\u0012\u0004\u0012\u00020\u0004\u0012\u0004\u0012\u00020\b0\u0003HÆ\u0003J;\u0010\u0010\u001a\u00020\u00002\u001a\b\u0002\u0010\u0002\u001a\u0014\u0012\u0004\u0012\u00020\u0004\u0012\n\u0012\b\u0012\u0004\u0012\u00020\u00060\u00050\u00032\u0014\b\u0002\u0010\u0007\u001a\u000e\u0012\u0004\u0012\u00020\u0004\u0012\u0004\u0012\u00020\b0\u0003HÆ\u0001J\u0013\u0010\u0011\u001a\u00020\u00122\b\u0010\u0013\u001a\u0004\u0018\u00010\u0001HÖ\u0003J\t\u0010\u0014\u001a\u00020\u0004HÖ\u0001J\t\u0010\u0015\u001a\u00020\bHÖ\u0001R#\u0010\u0002\u001a\u0014\u0012\u0004\u0012\u00020\u0004\u0012\n\u0012\b\u0012\u0004\u0012\u00020\u00060\u00050\u0003¢\u0006\b\n\u0000\u001a\u0004\b\u000b\u0010\fR\u001d\u0010\u0007\u001a\u000e\u0012\u0004\u0012\u00020\u0004\u0012\u0004\u0012\u00020\b0\u0003¢\u0006\b\n\u0000\u001a\u0004\b\r\u0010\f¨\u0006\u0016"}, d2 = {"Lcom/example/generator/SurahAudioData;", "", "segments", "", "", "", "Lcom/example/generator/WordSegment;", "audioUrls", "", "<init>", "(Ljava/util/Map;Ljava/util/Map;)V", "getSegments", "()Ljava/util/Map;", "getAudioUrls", "component1", "component2", "copy", "equals", "", "other", "hashCode", "toString", "app"}, k = 1, mv = {2, 2, 0}, xi = 48)
/* loaded from: /app/applet/classes5.dex */
public final class SurahAudioData {
    public static final int $stable = 8;
    private final Map<Integer, String> audioUrls;
    private final Map<Integer, List<WordSegment>> segments;

    /* JADX WARN: Multi-variable type inference failed */
    public static /* synthetic */ SurahAudioData copy$default(SurahAudioData surahAudioData, Map map, Map map2, int i, Object obj) {
        if ((i & 1) != 0) {
            map = surahAudioData.segments;
        }
        if ((i & 2) != 0) {
            map2 = surahAudioData.audioUrls;
        }
        return surahAudioData.copy(map, map2);
    }

    public final Map<Integer, List<WordSegment>> component1() {
        return this.segments;
    }

    public final Map<Integer, String> component2() {
        return this.audioUrls;
    }

    public final SurahAudioData copy(Map<Integer, ? extends List<WordSegment>> map, Map<Integer, String> map2) {
        Intrinsics.checkNotNullParameter(map, "segments");
        Intrinsics.checkNotNullParameter(map2, "audioUrls");
        return new SurahAudioData(map, map2);
    }

    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj instanceof SurahAudioData) {
            SurahAudioData surahAudioData = (SurahAudioData) obj;
            return Intrinsics.areEqual(this.segments, surahAudioData.segments) && Intrinsics.areEqual(this.audioUrls, surahAudioData.audioUrls);
        }
        return false;
    }

    public int hashCode() {
        return (this.segments.hashCode() * 31) + this.audioUrls.hashCode();
    }

    public String toString() {
        Map<Integer, List<WordSegment>> map = this.segments;
        return "SurahAudioData(segments=" + map + ", audioUrls=" + this.audioUrls + ")";
    }

    /* JADX WARN: Multi-variable type inference failed */
    public SurahAudioData(Map<Integer, ? extends List<WordSegment>> map, Map<Integer, String> map2) {
        Intrinsics.checkNotNullParameter(map, "segments");
        Intrinsics.checkNotNullParameter(map2, "audioUrls");
        this.segments = map;
        this.audioUrls = map2;
    }

    public final Map<Integer, List<WordSegment>> getSegments() {
        return this.segments;
    }

    public final Map<Integer, String> getAudioUrls() {
        return this.audioUrls;
    }
}
