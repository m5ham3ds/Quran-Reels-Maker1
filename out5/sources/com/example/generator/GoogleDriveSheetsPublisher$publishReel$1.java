package com.example.generator;

import kotlin.Metadata;
import kotlin.coroutines.Continuation;
import kotlin.coroutines.jvm.internal.ContinuationImpl;
import kotlin.coroutines.jvm.internal.DebugMetadata;
/* JADX INFO: Access modifiers changed from: package-private */
/* compiled from: GoogleDriveSheetsPublisher.kt */
@Metadata(k = 3, mv = {2, 2, 0}, xi = 48)
@DebugMetadata(c = "com.example.generator.GoogleDriveSheetsPublisher", f = "GoogleDriveSheetsPublisher.kt", i = {0, 0, 0, 0, 0, 1, 1, 1, 1, 1, 1, 2, 2, 2, 2, 2, 2, 2, 3, 3, 3, 3, 3, 3, 3, 3, 3, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5}, l = {48, 49, 60, 66, 85, 91}, m = "publishReel", n = {"videoFile", "surahName", "ayahRange", "reciterName", "description", "videoFile", "surahName", "ayahRange", "reciterName", "description", "linked", "videoFile", "surahName", "ayahRange", "reciterName", "description", "accessToken", "linked", "videoFile", "surahName", "ayahRange", "reciterName", "description", "accessToken", "folderId", "createdFolderId", "linked", "videoFile", "surahName", "ayahRange", "reciterName", "description", "accessToken", "folderId", "videoName", "driveFileId", "driveVideoLink", "linked", "videoFile", "surahName", "ayahRange", "reciterName", "description", "accessToken", "folderId", "videoName", "driveFileId", "driveVideoLink", "spreadsheetId", "createdSheetId", "linked"}, s = {"L$0", "L$1", "L$2", "L$3", "L$4", "L$0", "L$1", "L$2", "L$3", "L$4", "Z$0", "L$0", "L$1", "L$2", "L$3", "L$4", "L$5", "Z$0", "L$0", "L$1", "L$2", "L$3", "L$4", "L$5", "L$6", "L$7", "Z$0", "L$0", "L$1", "L$2", "L$3", "L$4", "L$5", "L$6", "L$7", "L$8", "L$9", "Z$0", "L$0", "L$1", "L$2", "L$3", "L$4", "L$5", "L$6", "L$7", "L$8", "L$9", "L$10", "L$11", "Z$0"})
/* loaded from: /app/applet/classes5.dex */
public final class GoogleDriveSheetsPublisher$publishReel$1 extends ContinuationImpl {
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
    boolean Z$0;
    int label;
    /* synthetic */ Object result;
    final /* synthetic */ GoogleDriveSheetsPublisher this$0;

    /* JADX INFO: Access modifiers changed from: package-private */
    /* JADX WARN: 'super' call moved to the top of the method (can break code semantics) */
    public GoogleDriveSheetsPublisher$publishReel$1(GoogleDriveSheetsPublisher googleDriveSheetsPublisher, Continuation<? super GoogleDriveSheetsPublisher$publishReel$1> continuation) {
        super(continuation);
        this.this$0 = googleDriveSheetsPublisher;
    }

    public final Object invokeSuspend(Object obj) {
        this.result = obj;
        this.label |= Integer.MIN_VALUE;
        return this.this$0.publishReel(null, null, null, null, null, (Continuation) this);
    }
}
