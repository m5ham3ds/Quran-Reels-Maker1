package com.example.generator;

import android.app.ActivityManager;
import android.content.Context;
import android.util.Log;
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;
import kotlin.Metadata;
import kotlin.collections.CollectionsKt;
import kotlin.io.FilesKt;
import kotlin.jvm.internal.Intrinsics;
import kotlin.jvm.internal.StringCompanionObject;
import kotlin.text.Charsets;
import kotlin.text.StringsKt;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Response;
/* compiled from: SystemDiagnosticTracker.kt */
@Metadata(d1 = {"\u0000@\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0002\b\u0003\n\u0002\u0010!\n\u0002\u0010\u000e\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u0002\n\u0002\b\u0003\n\u0002\u0010 \n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0002\b\u0006\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\t\n\u0000\bÇ\u0002\u0018\u00002\u00020\u0001B\t\b\u0002¢\u0006\u0004\b\u0002\u0010\u0003J\u0016\u0010\t\u001a\u00020\n2\u0006\u0010\u000b\u001a\u00020\u00062\u0006\u0010\f\u001a\u00020\u0006J\f\u0010\r\u001a\b\u0012\u0004\u0012\u00020\u00060\u000eJ\u0006\u0010\u000f\u001a\u00020\nJ\u0016\u0010\u0010\u001a\u00020\u00062\u0006\u0010\u0011\u001a\u00020\u0012H\u0086@¢\u0006\u0002\u0010\u0013J\u0016\u0010\u0014\u001a\u00020\u00062\u0006\u0010\u0011\u001a\u00020\u00122\u0006\u0010\u0015\u001a\u00020\u0006J\u0010\u0010\u0016\u001a\u00020\u00062\u0006\u0010\u0011\u001a\u00020\u0012H\u0002J\u0010\u0010\u0017\u001a\u00020\u00062\u0006\u0010\u0018\u001a\u00020\u0019H\u0002J\u0010\u0010\u001a\u001a\u00020\u001b2\u0006\u0010\u0018\u001a\u00020\u0019H\u0002R\u0014\u0010\u0004\u001a\b\u0012\u0004\u0012\u00020\u00060\u0005X\u0082\u0004¢\u0006\u0002\n\u0000R\u000e\u0010\u0007\u001a\u00020\bX\u0082\u0004¢\u0006\u0002\n\u0000¨\u0006\u001c"}, d2 = {"Lcom/example/generator/SystemDiagnosticTracker;", "", "<init>", "()V", "logList", "", "", "client", "Lokhttp3/OkHttpClient;", "addLog", "", "tag", "message", "getLogs", "", "clearLogs", "runFullSystemAudit", "context", "Landroid/content/Context;", "(Landroid/content/Context;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "saveReportToFilesAndGetPath", "reportContent", "getSystemRamInfo", "getFolderSizeLabel", "f", "Ljava/io/File;", "getFolderSize", "", "app"}, k = 1, mv = {2, 2, 0}, xi = 48)
/* loaded from: /app/applet/classes5.dex */
public final class SystemDiagnosticTracker {
    public static final SystemDiagnosticTracker INSTANCE = new SystemDiagnosticTracker();
    private static final List<String> logList = new ArrayList();
    private static final OkHttpClient client = new OkHttpClient.Builder().connectTimeout(5, TimeUnit.SECONDS).readTimeout(5, TimeUnit.SECONDS).addInterceptor(new Interceptor() { // from class: com.example.generator.SystemDiagnosticTracker$special$$inlined$-addInterceptor$1
        public final Response intercept(Interceptor.Chain chain) {
            Intrinsics.checkNotNullParameter(chain, "chain");
            return chain.proceed(chain.request().newBuilder().header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36").header("Accept", "*/*").build());
        }
    }).build();
    public static final int $stable = 8;

    private SystemDiagnosticTracker() {
    }

    public final synchronized void addLog(String tag, String message) {
        Intrinsics.checkNotNullParameter(tag, "tag");
        Intrinsics.checkNotNullParameter(message, "message");
        String timestamp = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS", Locale.US).format(new Date());
        String formatted = "[" + timestamp + "] [" + tag + "] " + message;
        logList.add(formatted);
        Log.d("SystemDiagnostic", formatted);
    }

    public final synchronized List<String> getLogs() {
        return CollectionsKt.toList(logList);
    }

    public final synchronized void clearLogs() {
        logList.clear();
        addLog("SYSTEM", "Logs cleared. Beginning new diagnostic tracking session.");
    }

    /*  JADX ERROR: JadxRuntimeException in pass: BlockProcessor
        jadx.core.utils.exceptions.JadxRuntimeException: Unreachable block: B:130:0x0595
        	at jadx.core.dex.visitors.blocks.BlockProcessor.checkForUnreachableBlocks(BlockProcessor.java:81)
        	at jadx.core.dex.visitors.blocks.BlockProcessor.processBlocksTree(BlockProcessor.java:47)
        	at jadx.core.dex.visitors.blocks.BlockProcessor.visit(BlockProcessor.java:39)
        */
    public final java.lang.Object runFullSystemAudit(android.content.Context r29, kotlin.coroutines.Continuation<? super java.lang.String> r30) {
        /*
            Method dump skipped, instructions count: 2126
            To view this dump add '--comments-level debug' option
        */
        throw new UnsupportedOperationException("Method not decompiled: com.example.generator.SystemDiagnosticTracker.runFullSystemAudit(android.content.Context, kotlin.coroutines.Continuation):java.lang.Object");
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public static final boolean runFullSystemAudit$lambda$2(File file, String name) {
        Intrinsics.checkNotNull(name);
        return StringsKt.endsWith$default(name, ".mp4", false, 2, (Object) null);
    }

    public final String saveReportToFilesAndGetPath(Context context, String reportContent) {
        Intrinsics.checkNotNullParameter(context, "context");
        Intrinsics.checkNotNullParameter(reportContent, "reportContent");
        try {
            File targetFolder = context.getExternalFilesDir(null);
            if (targetFolder == null) {
                targetFolder = context.getFilesDir();
            }
            if (!targetFolder.exists()) {
                targetFolder.mkdirs();
            }
            File reportFile = new File(targetFolder, "QuranReel_System_Diagnostic_Report.txt");
            FilesKt.writeText(reportFile, reportContent, Charsets.UTF_8);
            String absolutePath = reportFile.getAbsolutePath();
            Intrinsics.checkNotNull(absolutePath);
            return absolutePath;
        } catch (Exception e) {
            e.printStackTrace();
            return "Error: " + e.getMessage();
        }
    }

    private final String getSystemRamInfo(Context context) {
        try {
            Object systemService = context.getSystemService("activity");
            Intrinsics.checkNotNull(systemService, "null cannot be cast to non-null type android.app.ActivityManager");
            ActivityManager actManager = (ActivityManager) systemService;
            ActivityManager.MemoryInfo memInfo = new ActivityManager.MemoryInfo();
            actManager.getMemoryInfo(memInfo);
            float totalGb = ((float) memInfo.totalMem) / 1.0737418E9f;
            float availGb = ((float) memInfo.availMem) / 1.0737418E9f;
            StringCompanionObject stringCompanionObject = StringCompanionObject.INSTANCE;
            String format = String.format(Locale.US, "%.2f GB / %.2f GB", Arrays.copyOf(new Object[]{Float.valueOf(availGb), Float.valueOf(totalGb)}, 2));
            Intrinsics.checkNotNullExpressionValue(format, "format(...)");
            return format;
        } catch (Exception e) {
            return "N/A";
        }
    }

    private final String getFolderSizeLabel(File f) {
        long bytes = getFolderSize(f);
        if (bytes < 1024) {
            return bytes + " Bytes";
        }
        double kb = bytes / 1024.0d;
        if (kb < 1024.0d) {
            StringCompanionObject stringCompanionObject = StringCompanionObject.INSTANCE;
            String format = String.format(Locale.US, "%.1f KB", Arrays.copyOf(new Object[]{Double.valueOf(kb)}, 1));
            Intrinsics.checkNotNullExpressionValue(format, "format(...)");
            return format;
        }
        double mb = kb / 1024.0d;
        StringCompanionObject stringCompanionObject2 = StringCompanionObject.INSTANCE;
        String format2 = String.format(Locale.US, "%.1f MB", Arrays.copyOf(new Object[]{Double.valueOf(mb)}, 1));
        Intrinsics.checkNotNullExpressionValue(format2, "format(...)");
        return format2;
    }

    private final long getFolderSize(File f) {
        long size = 0;
        if (!f.isDirectory()) {
            long size2 = 0 + f.length();
            return size2;
        }
        File[] files = f.listFiles();
        if (files == null) {
            return 0L;
        }
        for (File file : files) {
            Intrinsics.checkNotNull(file);
            size += getFolderSize(file);
        }
        return size;
    }
}
