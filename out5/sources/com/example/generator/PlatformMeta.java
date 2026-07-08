package com.example.generator;

import kotlin.Metadata;
import kotlin.jvm.internal.Intrinsics;
/* compiled from: GeminiMetaGenerator.kt */
@Metadata(d1 = {"\u0000\"\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0000\n\u0002\u0010\u000e\n\u0002\b\r\n\u0002\u0010\u000b\n\u0002\b\u0002\n\u0002\u0010\b\n\u0002\b\u0002\b\u0087\b\u0018\u00002\u00020\u0001B\u001f\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u0012\u0006\u0010\u0004\u001a\u00020\u0003\u0012\u0006\u0010\u0005\u001a\u00020\u0003¢\u0006\u0004\b\u0006\u0010\u0007J\t\u0010\f\u001a\u00020\u0003HÆ\u0003J\t\u0010\r\u001a\u00020\u0003HÆ\u0003J\t\u0010\u000e\u001a\u00020\u0003HÆ\u0003J'\u0010\u000f\u001a\u00020\u00002\b\b\u0002\u0010\u0002\u001a\u00020\u00032\b\b\u0002\u0010\u0004\u001a\u00020\u00032\b\b\u0002\u0010\u0005\u001a\u00020\u0003HÆ\u0001J\u0013\u0010\u0010\u001a\u00020\u00112\b\u0010\u0012\u001a\u0004\u0018\u00010\u0001HÖ\u0003J\t\u0010\u0013\u001a\u00020\u0014HÖ\u0001J\t\u0010\u0015\u001a\u00020\u0003HÖ\u0001R\u0011\u0010\u0002\u001a\u00020\u0003¢\u0006\b\n\u0000\u001a\u0004\b\b\u0010\tR\u0011\u0010\u0004\u001a\u00020\u0003¢\u0006\b\n\u0000\u001a\u0004\b\n\u0010\tR\u0011\u0010\u0005\u001a\u00020\u0003¢\u0006\b\n\u0000\u001a\u0004\b\u000b\u0010\t¨\u0006\u0016"}, d2 = {"Lcom/example/generator/PlatformMeta;", "", "title", "", "description", "hashtags", "<init>", "(Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;)V", "getTitle", "()Ljava/lang/String;", "getDescription", "getHashtags", "component1", "component2", "component3", "copy", "equals", "", "other", "hashCode", "", "toString", "app"}, k = 1, mv = {2, 2, 0}, xi = 48)
/* loaded from: /app/applet/classes5.dex */
public final class PlatformMeta {
    public static final int $stable = 0;
    private final String description;
    private final String hashtags;
    private final String title;

    public static /* synthetic */ PlatformMeta copy$default(PlatformMeta platformMeta, String str, String str2, String str3, int i, Object obj) {
        if ((i & 1) != 0) {
            str = platformMeta.title;
        }
        if ((i & 2) != 0) {
            str2 = platformMeta.description;
        }
        if ((i & 4) != 0) {
            str3 = platformMeta.hashtags;
        }
        return platformMeta.copy(str, str2, str3);
    }

    public final String component1() {
        return this.title;
    }

    public final String component2() {
        return this.description;
    }

    public final String component3() {
        return this.hashtags;
    }

    public final PlatformMeta copy(String str, String str2, String str3) {
        Intrinsics.checkNotNullParameter(str, "title");
        Intrinsics.checkNotNullParameter(str2, "description");
        Intrinsics.checkNotNullParameter(str3, "hashtags");
        return new PlatformMeta(str, str2, str3);
    }

    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj instanceof PlatformMeta) {
            PlatformMeta platformMeta = (PlatformMeta) obj;
            return Intrinsics.areEqual(this.title, platformMeta.title) && Intrinsics.areEqual(this.description, platformMeta.description) && Intrinsics.areEqual(this.hashtags, platformMeta.hashtags);
        }
        return false;
    }

    public int hashCode() {
        return (((this.title.hashCode() * 31) + this.description.hashCode()) * 31) + this.hashtags.hashCode();
    }

    public String toString() {
        String str = this.title;
        String str2 = this.description;
        return "PlatformMeta(title=" + str + ", description=" + str2 + ", hashtags=" + this.hashtags + ")";
    }

    public PlatformMeta(String title, String description, String hashtags) {
        Intrinsics.checkNotNullParameter(title, "title");
        Intrinsics.checkNotNullParameter(description, "description");
        Intrinsics.checkNotNullParameter(hashtags, "hashtags");
        this.title = title;
        this.description = description;
        this.hashtags = hashtags;
    }

    public final String getTitle() {
        return this.title;
    }

    public final String getDescription() {
        return this.description;
    }

    public final String getHashtags() {
        return this.hashtags;
    }
}
