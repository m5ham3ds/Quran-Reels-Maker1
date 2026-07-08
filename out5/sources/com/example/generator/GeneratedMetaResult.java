package com.example.generator;

import kotlin.Metadata;
import kotlin.jvm.internal.Intrinsics;
/* compiled from: GeminiMetaGenerator.kt */
@Metadata(d1 = {"\u0000&\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0010\n\u0002\u0010\u000b\n\u0002\b\u0002\n\u0002\u0010\b\n\u0000\n\u0002\u0010\u000e\n\u0000\b\u0087\b\u0018\u00002\u00020\u0001B/\u0012\b\u0010\u0002\u001a\u0004\u0018\u00010\u0003\u0012\b\u0010\u0004\u001a\u0004\u0018\u00010\u0003\u0012\b\u0010\u0005\u001a\u0004\u0018\u00010\u0003\u0012\b\u0010\u0006\u001a\u0004\u0018\u00010\u0003¢\u0006\u0004\b\u0007\u0010\bJ\u000b\u0010\u000e\u001a\u0004\u0018\u00010\u0003HÆ\u0003J\u000b\u0010\u000f\u001a\u0004\u0018\u00010\u0003HÆ\u0003J\u000b\u0010\u0010\u001a\u0004\u0018\u00010\u0003HÆ\u0003J\u000b\u0010\u0011\u001a\u0004\u0018\u00010\u0003HÆ\u0003J9\u0010\u0012\u001a\u00020\u00002\n\b\u0002\u0010\u0002\u001a\u0004\u0018\u00010\u00032\n\b\u0002\u0010\u0004\u001a\u0004\u0018\u00010\u00032\n\b\u0002\u0010\u0005\u001a\u0004\u0018\u00010\u00032\n\b\u0002\u0010\u0006\u001a\u0004\u0018\u00010\u0003HÆ\u0001J\u0013\u0010\u0013\u001a\u00020\u00142\b\u0010\u0015\u001a\u0004\u0018\u00010\u0001HÖ\u0003J\t\u0010\u0016\u001a\u00020\u0017HÖ\u0001J\t\u0010\u0018\u001a\u00020\u0019HÖ\u0001R\u0013\u0010\u0002\u001a\u0004\u0018\u00010\u0003¢\u0006\b\n\u0000\u001a\u0004\b\t\u0010\nR\u0013\u0010\u0004\u001a\u0004\u0018\u00010\u0003¢\u0006\b\n\u0000\u001a\u0004\b\u000b\u0010\nR\u0013\u0010\u0005\u001a\u0004\u0018\u00010\u0003¢\u0006\b\n\u0000\u001a\u0004\b\f\u0010\nR\u0013\u0010\u0006\u001a\u0004\u0018\u00010\u0003¢\u0006\b\n\u0000\u001a\u0004\b\r\u0010\n¨\u0006\u001a"}, d2 = {"Lcom/example/generator/GeneratedMetaResult;", "", "tiktok", "Lcom/example/generator/PlatformMeta;", "instagram", "facebook", "youtube", "<init>", "(Lcom/example/generator/PlatformMeta;Lcom/example/generator/PlatformMeta;Lcom/example/generator/PlatformMeta;Lcom/example/generator/PlatformMeta;)V", "getTiktok", "()Lcom/example/generator/PlatformMeta;", "getInstagram", "getFacebook", "getYoutube", "component1", "component2", "component3", "component4", "copy", "equals", "", "other", "hashCode", "", "toString", "", "app"}, k = 1, mv = {2, 2, 0}, xi = 48)
/* loaded from: /app/applet/classes5.dex */
public final class GeneratedMetaResult {
    public static final int $stable = 0;
    private final PlatformMeta facebook;
    private final PlatformMeta instagram;
    private final PlatformMeta tiktok;
    private final PlatformMeta youtube;

    public static /* synthetic */ GeneratedMetaResult copy$default(GeneratedMetaResult generatedMetaResult, PlatformMeta platformMeta, PlatformMeta platformMeta2, PlatformMeta platformMeta3, PlatformMeta platformMeta4, int i, Object obj) {
        if ((i & 1) != 0) {
            platformMeta = generatedMetaResult.tiktok;
        }
        if ((i & 2) != 0) {
            platformMeta2 = generatedMetaResult.instagram;
        }
        if ((i & 4) != 0) {
            platformMeta3 = generatedMetaResult.facebook;
        }
        if ((i & 8) != 0) {
            platformMeta4 = generatedMetaResult.youtube;
        }
        return generatedMetaResult.copy(platformMeta, platformMeta2, platformMeta3, platformMeta4);
    }

    public final PlatformMeta component1() {
        return this.tiktok;
    }

    public final PlatformMeta component2() {
        return this.instagram;
    }

    public final PlatformMeta component3() {
        return this.facebook;
    }

    public final PlatformMeta component4() {
        return this.youtube;
    }

    public final GeneratedMetaResult copy(PlatformMeta platformMeta, PlatformMeta platformMeta2, PlatformMeta platformMeta3, PlatformMeta platformMeta4) {
        return new GeneratedMetaResult(platformMeta, platformMeta2, platformMeta3, platformMeta4);
    }

    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj instanceof GeneratedMetaResult) {
            GeneratedMetaResult generatedMetaResult = (GeneratedMetaResult) obj;
            return Intrinsics.areEqual(this.tiktok, generatedMetaResult.tiktok) && Intrinsics.areEqual(this.instagram, generatedMetaResult.instagram) && Intrinsics.areEqual(this.facebook, generatedMetaResult.facebook) && Intrinsics.areEqual(this.youtube, generatedMetaResult.youtube);
        }
        return false;
    }

    public int hashCode() {
        return ((((((this.tiktok == null ? 0 : this.tiktok.hashCode()) * 31) + (this.instagram == null ? 0 : this.instagram.hashCode())) * 31) + (this.facebook == null ? 0 : this.facebook.hashCode())) * 31) + (this.youtube != null ? this.youtube.hashCode() : 0);
    }

    public String toString() {
        PlatformMeta platformMeta = this.tiktok;
        PlatformMeta platformMeta2 = this.instagram;
        PlatformMeta platformMeta3 = this.facebook;
        return "GeneratedMetaResult(tiktok=" + platformMeta + ", instagram=" + platformMeta2 + ", facebook=" + platformMeta3 + ", youtube=" + this.youtube + ")";
    }

    public GeneratedMetaResult(PlatformMeta tiktok, PlatformMeta instagram, PlatformMeta facebook, PlatformMeta youtube) {
        this.tiktok = tiktok;
        this.instagram = instagram;
        this.facebook = facebook;
        this.youtube = youtube;
    }

    public final PlatformMeta getTiktok() {
        return this.tiktok;
    }

    public final PlatformMeta getInstagram() {
        return this.instagram;
    }

    public final PlatformMeta getFacebook() {
        return this.facebook;
    }

    public final PlatformMeta getYoutube() {
        return this.youtube;
    }
}
