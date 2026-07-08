package com.example.generator;

import android.content.Context;
import android.util.Log;
import com.example.settings.SettingsManager;
import java.io.Closeable;
import java.io.File;
import java.util.List;
import kotlin.Metadata;
import kotlin.io.CloseableKt;
import kotlin.jvm.internal.DefaultConstructorMarker;
import kotlin.jvm.internal.Intrinsics;
import kotlin.text.MatchResult;
import kotlin.text.Regex;
import kotlin.text.StringsKt;
import okhttp3.Headers;
import okhttp3.Interceptor;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;
/* compiled from: GoogleDriveSheetsPublisher.kt */
@Metadata(d1 = {"\u0000@\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\u0010\u000e\n\u0000\n\u0002\u0018\u0002\n\u0002\b\r\n\u0002\u0010\u000b\n\u0002\b\u0006\n\u0002\u0010 \n\u0002\b\u0005\b\u0007\u0018\u0000 '2\u00020\u0001:\u0001'B\u000f\u0012\u0006\u0010\u0002\u001a\u00020\u0003¢\u0006\u0004\b\u0004\u0010\u0005JD\u0010\n\u001a\u0010\u0012\u0004\u0012\u00020\f\u0012\u0004\u0012\u00020\f\u0018\u00010\u000b2\u0006\u0010\r\u001a\u00020\u000e2\u0006\u0010\u000f\u001a\u00020\f2\u0006\u0010\u0010\u001a\u00020\f2\u0006\u0010\u0011\u001a\u00020\f2\u0006\u0010\u0012\u001a\u00020\fH\u0086@¢\u0006\u0002\u0010\u0013J\u001a\u0010\u0014\u001a\u0004\u0018\u00010\f2\u0006\u0010\u0015\u001a\u00020\f2\u0006\u0010\u0016\u001a\u00020\fH\u0002J,\u0010\u0017\u001a\u0004\u0018\u00010\f2\u0006\u0010\u0015\u001a\u00020\f2\u0006\u0010\u0018\u001a\u00020\u000e2\u0006\u0010\u0019\u001a\u00020\f2\b\u0010\u001a\u001a\u0004\u0018\u00010\fH\u0002J\u0018\u0010\u001b\u001a\u00020\u001c2\u0006\u0010\u0015\u001a\u00020\f2\u0006\u0010\u001d\u001a\u00020\fH\u0002J\u001a\u0010\u001e\u001a\u0004\u0018\u00010\f2\u0006\u0010\u0015\u001a\u00020\f2\u0006\u0010\u001f\u001a\u00020\fH\u0002J&\u0010 \u001a\u00020\u001c2\u0006\u0010\u0015\u001a\u00020\f2\u0006\u0010!\u001a\u00020\f2\f\u0010\"\u001a\b\u0012\u0004\u0012\u00020\f0#H\u0002J\u001a\u0010$\u001a\u0004\u0018\u00010\f2\u0006\u0010%\u001a\u00020\f2\u0006\u0010&\u001a\u00020\fH\u0002R\u000e\u0010\u0002\u001a\u00020\u0003X\u0082\u0004¢\u0006\u0002\n\u0000R\u000e\u0010\u0006\u001a\u00020\u0007X\u0082\u0004¢\u0006\u0002\n\u0000R\u000e\u0010\b\u001a\u00020\tX\u0082\u0004¢\u0006\u0002\n\u0000¨\u0006("}, d2 = {"Lcom/example/generator/GoogleDriveSheetsPublisher;", "", "context", "Landroid/content/Context;", "<init>", "(Landroid/content/Context;)V", "client", "Lokhttp3/OkHttpClient;", "settingsManager", "Lcom/example/settings/SettingsManager;", "publishReel", "Lkotlin/Pair;", "", "videoFile", "Ljava/io/File;", "surahName", "ayahRange", "reciterName", "description", "(Ljava/io/File;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "createDriveFolder", "accessToken", "folderName", "uploadFileToDrive", "file", "fileName", "parentFolderId", "makeFilePubliclyReadable", "", "fileId", "createGoogleSpreadsheet", "title", "appendRowToSheet", "spreadsheetId", "rowValues", "", "extractJsonValue", "json", "key", "Companion", "app"}, k = 1, mv = {2, 2, 0}, xi = 48)
/* loaded from: /app/applet/classes5.dex */
public final class GoogleDriveSheetsPublisher {
    private static final String TAG = "GooglePublisher";
    private final OkHttpClient client;
    private final Context context;
    private final SettingsManager settingsManager;
    public static final Companion Companion = new Companion(null);
    public static final int $stable = 8;

    public GoogleDriveSheetsPublisher(Context context) {
        Intrinsics.checkNotNullParameter(context, "context");
        this.context = context;
        this.client = new OkHttpClient.Builder().addInterceptor(new Interceptor() { // from class: com.example.generator.GoogleDriveSheetsPublisher$special$$inlined$-addInterceptor$1
            public final Response intercept(Interceptor.Chain chain) {
                Intrinsics.checkNotNullParameter(chain, "chain");
                return chain.proceed(chain.request().newBuilder().header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36").header("Accept", "*/*").build());
            }
        }).build();
        this.settingsManager = new SettingsManager(this.context);
    }

    /* compiled from: GoogleDriveSheetsPublisher.kt */
    @Metadata(d1 = {"\u0000\u0012\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0002\b\u0003\n\u0002\u0010\u000e\n\u0000\b\u0086\u0003\u0018\u00002\u00020\u0001B\t\b\u0002¢\u0006\u0004\b\u0002\u0010\u0003R\u000e\u0010\u0004\u001a\u00020\u0005X\u0082T¢\u0006\u0002\n\u0000¨\u0006\u0006"}, d2 = {"Lcom/example/generator/GoogleDriveSheetsPublisher$Companion;", "", "<init>", "()V", "TAG", "", "app"}, k = 1, mv = {2, 2, 0}, xi = 48)
    /* loaded from: /app/applet/classes5.dex */
    public static final class Companion {
        public /* synthetic */ Companion(DefaultConstructorMarker defaultConstructorMarker) {
            this();
        }

        private Companion() {
        }
    }

    /*  JADX ERROR: JadxRuntimeException in pass: BlockProcessor
        jadx.core.utils.exceptions.JadxRuntimeException: Unreachable block: B:113:0x04e9
        	at jadx.core.dex.visitors.blocks.BlockProcessor.checkForUnreachableBlocks(BlockProcessor.java:81)
        	at jadx.core.dex.visitors.blocks.BlockProcessor.processBlocksTree(BlockProcessor.java:47)
        	at jadx.core.dex.visitors.blocks.BlockProcessor.visit(BlockProcessor.java:39)
        */
    public final java.lang.Object publishReel(java.io.File r28, java.lang.String r29, java.lang.String r30, java.lang.String r31, java.lang.String r32, kotlin.coroutines.Continuation<? super kotlin.Pair<java.lang.String, java.lang.String>> r33) {
        /*
            Method dump skipped, instructions count: 1364
            To view this dump add '--comments-level debug' option
        */
        throw new UnsupportedOperationException("Method not decompiled: com.example.generator.GoogleDriveSheetsPublisher.publishReel(java.io.File, java.lang.String, java.lang.String, java.lang.String, java.lang.String, kotlin.coroutines.Continuation):java.lang.Object");
    }

    private final String createDriveFolder(String accessToken, String folderName) {
        MediaType mediaType = MediaType.Companion.get("application/json; charset=utf-8");
        String jsonPayload = StringsKt.trimIndent("\n            {\n              \"name\": \"" + folderName + "\",\n              \"mimeType\": \"application/vnd.google-apps.folder\"\n            }\n        ");
        Request request = new Request.Builder().url("https://www.googleapis.com/drive/v3/files").addHeader("Authorization", "Bearer " + accessToken).post(RequestBody.Companion.create(jsonPayload, mediaType)).build();
        Response response = (Closeable) this.client.newCall(request).execute();
        try {
            Response response2 = response;
            ResponseBody body = response2.body();
            String str = (body == null || (str = body.string()) == null) ? "" : "";
            if (response2.isSuccessful()) {
                String extractJsonValue = extractJsonValue(str, "id");
                CloseableKt.closeFinally(response, (Throwable) null);
                return extractJsonValue;
            }
            Log.e(TAG, "createDriveFolder failed: " + response2.code() + " " + response2.message() + " -> " + str);
            CloseableKt.closeFinally(response, (Throwable) null);
            return null;
        } catch (Throwable th) {
            try {
                throw th;
            } catch (Throwable th2) {
                CloseableKt.closeFinally(response, th);
                throw th2;
            }
        }
    }

    private final String uploadFileToDrive(String accessToken, File file, String fileName, String parentFolderId) {
        Throwable th;
        MediaType mediaTypeJson = MediaType.Companion.get("application/json; charset=UTF-8");
        MediaType mediaTypeVideo = MediaType.Companion.get("video/mp4");
        String str = parentFolderId;
        String str2 = "";
        String parentSection = !(str == null || StringsKt.isBlank(str)) ? ", \"parents\": [\"" + parentFolderId + "\"]" : "";
        String metadata = StringsKt.trimIndent("\n            {\n              \"name\": \"" + fileName + "\",\n              \"mimeType\": \"video/mp4\"" + parentSection + "\n            }\n        ");
        Request request = new Request.Builder().url("https://www.googleapis.com/upload/drive/v3/files?uploadType=multipart").addHeader("Authorization", "Bearer " + accessToken).post(new MultipartBody.Builder((String) null, 1, (DefaultConstructorMarker) null).setType(MultipartBody.FORM).addPart(new Headers.Builder().add("Content-Type", "application/json; charset=UTF-8").build(), RequestBody.Companion.create(metadata, mediaTypeJson)).addPart(new Headers.Builder().add("Content-Type", "video/mp4").build(), RequestBody.Companion.create(file, mediaTypeVideo)).build()).build();
        Response response = (Closeable) this.client.newCall(request).execute();
        try {
            Response response2 = response;
            ResponseBody body = response2.body();
            if (body != null) {
                try {
                    String string = body.string();
                    if (string != null) {
                        str2 = string;
                    }
                } catch (Throwable th2) {
                    th = th2;
                    try {
                        throw th;
                    } catch (Throwable th3) {
                        CloseableKt.closeFinally(response, th);
                        throw th3;
                    }
                }
            }
            if (response2.isSuccessful()) {
                String extractJsonValue = extractJsonValue(str2, "id");
                CloseableKt.closeFinally(response, (Throwable) null);
                return extractJsonValue;
            }
            try {
                Log.e(TAG, "uploadFileToDrive failed: " + response2.code() + " " + response2.message() + " -> " + str2);
                CloseableKt.closeFinally(response, (Throwable) null);
                return null;
            } catch (Throwable th4) {
                th = th4;
                throw th;
            }
        } catch (Throwable th5) {
            th = th5;
        }
    }

    private final boolean makeFilePubliclyReadable(String accessToken, String fileId) {
        String url = "https://www.googleapis.com/drive/v3/files/" + fileId + "/permissions";
        MediaType mediaType = MediaType.Companion.get("application/json; charset=utf-8");
        Request request = new Request.Builder().url(url).addHeader("Authorization", "Bearer " + accessToken).post(RequestBody.Companion.create("{\n  \"role\": \"reader\",\n  \"type\": \"anyone\"\n}", mediaType)).build();
        Response response = (Closeable) this.client.newCall(request).execute();
        try {
            Response response2 = response;
            if (response2.isSuccessful()) {
                CloseableKt.closeFinally(response, (Throwable) null);
                return true;
            }
            ResponseBody body = response2.body();
            Log.e(TAG, "makeFilePublic failed: " + response2.code() + " " + response2.message() + " -> " + ((body == null || (r10 = body.string()) == null) ? "" : ""));
            CloseableKt.closeFinally(response, (Throwable) null);
            return false;
        } finally {
        }
    }

    private final String createGoogleSpreadsheet(String accessToken, String title) {
        MediaType mediaType = MediaType.Companion.get("application/json; charset=utf-8");
        String jsonPayload = StringsKt.trimIndent("\n            {\n              \"properties\": {\n                \"title\": \"" + title + "\"\n              }\n            }\n        ");
        Request request = new Request.Builder().url("https://sheets.googleapis.com/v4/spreadsheets").addHeader("Authorization", "Bearer " + accessToken).post(RequestBody.Companion.create(jsonPayload, mediaType)).build();
        Response response = (Closeable) this.client.newCall(request).execute();
        try {
            Response response2 = response;
            ResponseBody body = response2.body();
            String str = (body == null || (str = body.string()) == null) ? "" : "";
            if (response2.isSuccessful()) {
                String extractJsonValue = extractJsonValue(str, "spreadsheetId");
                CloseableKt.closeFinally(response, (Throwable) null);
                return extractJsonValue;
            }
            Log.e(TAG, "createGoogleSpreadsheet failed: " + response2.code() + " " + response2.message() + " -> " + str);
            CloseableKt.closeFinally(response, (Throwable) null);
            return null;
        } catch (Throwable th) {
            try {
                throw th;
            } catch (Throwable th2) {
                CloseableKt.closeFinally(response, th);
                throw th2;
            }
        }
    }

    /* JADX WARN: Code restructure failed: missing block: B:15:0x012f, code lost:
        if (r13 == null) goto L16;
     */
    /*
        Code decompiled incorrectly, please refer to instructions dump.
        To view partially-correct add '--show-bad-code' argument
    */
    private final boolean appendRowToSheet(java.lang.String r31, java.lang.String r32, java.util.List<java.lang.String> r33) {
        /*
            Method dump skipped, instructions count: 389
            To view this dump add '--comments-level debug' option
        */
        throw new UnsupportedOperationException("Method not decompiled: com.example.generator.GoogleDriveSheetsPublisher.appendRowToSheet(java.lang.String, java.lang.String, java.util.List):boolean");
    }

    private final String extractJsonValue(String json, String key) {
        List groupValues;
        Regex pattern = new Regex("\"" + key + "\"\\s*:\\s*\"([^\"]+)\"");
        MatchResult find$default = Regex.find$default(pattern, json, 0, 2, (Object) null);
        if (find$default == null || (groupValues = find$default.getGroupValues()) == null) {
            return null;
        }
        return (String) groupValues.get(1);
    }
}
