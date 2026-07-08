package com.example.generator;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import kotlin.Metadata;
import kotlin.collections.CollectionsKt;
import kotlin.jvm.functions.Function1;
import kotlin.jvm.internal.Intrinsics;
import kotlin.text.StringsKt;
/* compiled from: VideoGenerator.kt */
@Metadata(d1 = {"\u0000\u001a\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0002\b\u0003\n\u0002\u0010\u000e\n\u0000\n\u0002\u0010 \n\u0002\b\u0002\bÇ\u0002\u0018\u00002\u00020\u0001B\t\b\u0002¢\u0006\u0004\b\u0002\u0010\u0003J\u001c\u0010\u0004\u001a\u00020\u00052\f\u0010\u0006\u001a\b\u0012\u0004\u0012\u00020\u00050\u00072\u0006\u0010\b\u001a\u00020\u0005¨\u0006\t"}, d2 = {"Lcom/example/generator/SystemPromptTemplate;", "", "<init>", "()V", "getAlignmentPrompt", "", "arabicChunks", "", "fullTranslation", "app"}, k = 1, mv = {2, 2, 0}, xi = 48)
/* loaded from: /app/applet/classes5.dex */
public final class SystemPromptTemplate {
    public static final int $stable = 0;
    public static final SystemPromptTemplate INSTANCE = new SystemPromptTemplate();

    private SystemPromptTemplate() {
    }

    public final String getAlignmentPrompt(List<String> list, String fullTranslation) {
        Intrinsics.checkNotNullParameter(list, "arabicChunks");
        Intrinsics.checkNotNullParameter(fullTranslation, "fullTranslation");
        List<String> list2 = list;
        Collection arrayList = new ArrayList(CollectionsKt.collectionSizeOrDefault(list2, 10));
        int i = 0;
        for (Object obj : list2) {
            int i2 = i + 1;
            if (i < 0) {
                CollectionsKt.throwIndexOverflow();
            }
            arrayList.add("Chunk #" + (i + 1) + ": " + ((String) obj));
            i = i2;
        }
        String joinToString$default = CollectionsKt.joinToString$default((List) arrayList, "\n", (CharSequence) null, (CharSequence) null, 0, (CharSequence) null, (Function1) null, 62, (Object) null);
        return StringsKt.trimIndent("\n            You are an expert Quran translation alignment assistant.\n            Your task is to provide an accurate, context-aware English translation for each specific Arabic chunk provided below.\n            The user wants the English translation to match the exact meaning of the Arabic words in that specific chunk, rather than randomly splitting the full verse translation.\n            \n            Input Arabic Chunks:\n            " + joinToString$default + "\n\n            Return a single raw JSON object matching this schema:\n            {\n               \"aligned_translations\": [\n                  \"Accurate English translation for Chunk #1\",\n                  \"Accurate English translation for Chunk #2\", ...\n               ]\n            }\n            \n            CRITICAL RULES:\n            1. The number of translations MUST EXACTLY match the number of input chunks (" + list.size() + ").\n            2. Do not include any explanation, backticks or markdown formatting. Only return valid raw JSON.\n        ");
    }
}
