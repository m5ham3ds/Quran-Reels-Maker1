package com.example.generator;

import android.content.Context;
import android.net.Uri;
import kotlin.Metadata;
import kotlin.ResultKt;
import kotlin.Unit;
import kotlin.coroutines.Continuation;
import kotlin.coroutines.intrinsics.IntrinsicsKt;
import kotlin.coroutines.jvm.internal.DebugMetadata;
import kotlin.coroutines.jvm.internal.SuspendLambda;
import kotlin.jvm.functions.Function1;
import kotlin.jvm.functions.Function2;
import kotlinx.coroutines.CoroutineScope;
/* JADX INFO: Access modifiers changed from: package-private */
/* compiled from: VideoGenerator.kt */
@Metadata(d1 = {"\u0000\n\n\u0000\n\u0002\u0010\u0002\n\u0002\u0018\u0002\u0010\u0000\u001a\u00020\u0001*\u00020\u0002H\n"}, d2 = {"<anonymous>", "", "Lkotlinx/coroutines/CoroutineScope;"}, k = 3, mv = {2, 2, 0}, xi = 48)
@DebugMetadata(c = "com.example.generator.VideoGenerator$generateReel$2", f = "VideoGenerator.kt", i = {0, 0, 0, 1, 1, 1, 1, 1, 2, 2, 2, 2, 2, 2, 3, 3, 3, 3, 3, 3, 3, 4, 4, 4, 4, 4, 4, 4, 4, 5, 5, 5, 5, 5, 5, 5, 5, 5, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 9, 9, 9, 9, 9, 9, 9, 9, 9, 9, 9, 9, 9, 10, 10, 10, 10, 10, 10, 10, 10, 10, 10, 10, 10, 10, 10, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 12, 12, 12, 12, 12, 12, 12, 12, 12, 12, 12, 12, 12, 12, 12, 12, 13, 13, 13, 13, 13, 13, 13, 13, 13, 13, 13, 13, 13, 13, 13, 13, 13, 14, 14, 14, 14, 14, 14, 14, 14, 14, 14, 14, 14, 14, 14, 14, 14, 14, 14, 15, 15, 15, 15, 15, 15, 15, 15, 15, 15, 15, 15, 15, 15, 15, 15, 15, 15, 15, 16, 16, 16, 16, 16, 16, 16, 16, 16, 16, 16, 16, 16, 16, 16, 16, 16, 16, 16, 16, 17, 17, 17, 17, 17, 17, 17, 17, 17, 17, 17, 17, 17, 17, 17, 17, 17, 17, 17, 17, 17, 18, 18, 18, 18, 18, 18, 18, 18, 18, 18, 18, 18, 18, 18, 18, 18, 18, 18, 18, 18, 18, 18, 19, 19, 19, 19, 19, 19, 19, 19, 19, 19, 19, 19, 19, 19, 19, 19, 19, 19, 19, 19, 19, 19, 19, 20, 20, 20, 20, 20, 20, 20, 20, 20, 20, 20, 20, 20, 20, 20, 20, 20, 20, 20, 20, 20, 20, 20, 20, 21, 21, 21, 21, 21, 21, 21, 21, 21, 21, 21, 21, 21, 21, 21, 21, 21, 21, 21, 21, 21, 21, 21, 21, 21, 22, 22, 22, 22, 22, 22, 22, 22, 22, 22, 22, 22, 22, 22, 22, 22, 22, 22, 22, 22, 22, 22, 22, 22, 22, 22, 23, 23, 23, 23, 23, 23, 23, 23, 23, 23, 23, 23, 23, 23, 23, 23, 23, 23, 23, 23, 23, 23, 23, 23, 23, 23, 23, 24, 24, 24, 24, 24, 24, 24, 24, 24, 24, 24, 24, 24, 24, 24, 24, 24, 24, 24, 24, 24, 24, 24, 24, 24, 24, 24, 24, 25, 25, 25, 25, 25, 25, 25, 25, 25, 25, 25, 25, 25, 25, 25, 25, 25, 25, 25, 25, 25, 25, 25, 25, 25, 25, 25, 25, 25, 26, 26, 26, 26, 26, 26, 26, 26, 26, 26, 26, 26, 26, 26, 26, 26, 26, 26, 26, 26, 26, 26, 26, 26, 26, 26, 26, 26, 26, 26, 27, 27, 27, 27, 27, 27, 27, 27, 27, 27, 27, 27, 27, 27, 27, 27, 27, 27, 27, 27, 27, 27, 27, 27, 27, 27, 27, 27, 27, 27, 27, 28, 28, 28, 28, 28, 28, 28, 28, 28, 28, 28, 28, 28, 28, 28, 28, 28, 28, 28, 28, 28, 28, 28, 28, 28, 28, 28, 28, 28, 28, 28, 28, 29, 29, 29, 29, 29, 29, 29, 29, 29, 29, 29, 29, 29, 29, 29, 29, 29, 29, 29, 29, 29, 29, 29, 29, 29, 29, 29, 29, 29, 29, 29, 29, 29, 30, 30, 30, 30, 30, 30, 30, 30, 30, 30, 30, 30, 30, 30, 30, 30, 30, 30, 30, 30, 30, 30, 30, 30, 30, 30, 30, 30, 30, 30, 30, 30, 30, 30, 31, 31, 31, 31, 31, 31, 31, 31, 31, 31, 31, 31, 31, 31, 31, 31, 31, 31, 31, 31, 31, 31, 31, 31, 31, 31, 31, 31, 31, 31, 31, 31, 31, 31, 31, 31, 31, 31, 31, 31, 31, 31, 31, 31, 31, 31, 31, 31, 31, 31, 31, 31, 31, 32, 32, 32, 32, 32, 32, 32, 32, 32, 32, 32, 32, 32, 32, 32, 32, 32, 32, 32, 32, 32, 32, 32, 32, 32, 32, 32, 32, 32, 32, 32, 32, 32, 32, 32, 32, 32, 32, 32, 32, 32, 32, 32, 32, 32, 32, 32, 32, 32, 32, 32, 32, 32, 32, 32, 32, 32, 33, 33, 33, 33, 33, 33, 33, 33, 33, 33, 33, 33, 33, 33, 33, 33, 33, 33, 33, 33, 33, 33, 33, 33, 33, 33, 33, 33, 33, 33, 33, 33, 33, 33, 33, 33, 33, 33, 33, 33, 33, 33, 33, 33, 33, 33, 33, 33, 33, 33, 33, 33, 33, 33, 33, 33, 33, 33, 33, 33, 34, 34, 34, 34, 34, 34, 34, 34, 34, 34, 34, 34, 34, 34, 34, 34, 34, 34, 34, 34, 34, 34, 34, 34, 34, 34, 34, 34, 34, 34, 34, 34, 34, 34, 34, 34, 34, 34, 34, 34, 34, 34, 34, 34, 34, 34, 34, 34, 34, 35, 35, 35, 35, 35, 35, 35, 35, 35, 35, 35, 35, 35, 35, 35, 35, 35, 35, 35, 35, 35, 35, 35, 35, 35, 35, 35, 35, 35, 35, 35, 35, 35, 35, 35, 35, 35, 35, 35, 35, 35, 35, 35, 35, 35, 35, 35, 35, 35, 35, 35, 35, 35, 36, 36, 36, 36, 36, 36, 36, 36, 36, 36, 36, 36, 36, 36, 36, 36, 36, 36, 36, 36, 36, 36, 36, 36, 36, 36, 36, 36, 36, 36, 36, 36, 36, 36, 36, 36, 36, 36, 36, 36, 36, 36, 36, 36, 36, 36, 36, 36, 36, 36, 36, 36, 36, 36, 36, 36, 36, 36, 36, 36, 36, 36, 36, 36, 36, 36, 36, 37, 37, 37, 37}, l = {215, 218, 219, 220, 221, 222, 224, 225, 226, 227, 229, 230, 231, 233, 234, 235, 236, 237, 238, 239, 240, 241, 242, 243, 244, 245, 246, 247, 248, 249, 250, 337, 476, 624, 946, 957, 1337, 1376}, m = "invokeSuspend", n = {"verses", "settingsManager", "totalAyahs", "verses", "settingsManager", "language", "totalAyahs", "isArabic", "verses", "settingsManager", "language", "fontFamily", "totalAyahs", "isArabic", "verses", "settingsManager", "language", "fontFamily", "fontWeight", "totalAyahs", "isArabic", "verses", "settingsManager", "language", "fontFamily", "fontWeight", "totalAyahs", "isArabic", "textFontSize", "verses", "settingsManager", "language", "fontFamily", "fontWeight", "textColorStr", "totalAyahs", "isArabic", "textFontSize", "verses", "settingsManager", "language", "fontFamily", "fontWeight", "textColorStr", "totalAyahs", "isArabic", "textFontSize", "textOpacity", "verses", "settingsManager", "language", "fontFamily", "fontWeight", "textColorStr", "totalAyahs", "isArabic", "textFontSize", "textOpacity", "showTextBg", "verses", "settingsManager", "language", "fontFamily", "fontWeight", "textColorStr", "textBgColorStr", "totalAyahs", "isArabic", "textFontSize", "textOpacity", "showTextBg", "verses", "settingsManager", "language", "fontFamily", "fontWeight", "textColorStr", "textBgColorStr", "totalAyahs", "isArabic", "textFontSize", "textOpacity", "showTextBg", "textBgOpacity", "verses", "settingsManager", "language", "fontFamily", "fontWeight", "textColorStr", "textBgColorStr", "totalAyahs", "isArabic", "textFontSize", "textOpacity", "showTextBg", "textBgOpacity", "textBgRadius", "verses", "settingsManager", "language", "fontFamily", "fontWeight", "textColorStr", "textBgColorStr", "textPosition", "totalAyahs", "isArabic", "textFontSize", "textOpacity", "showTextBg", "textBgOpacity", "textBgRadius", "verses", "settingsManager", "language", "fontFamily", "fontWeight", "textColorStr", "textBgColorStr", "textPosition", "textAlign", "totalAyahs", "isArabic", "textFontSize", "textOpacity", "showTextBg", "textBgOpacity", "textBgRadius", "verses", "settingsManager", "language", "fontFamily", "fontWeight", "textColorStr", "textBgColorStr", "textPosition", "textAlign", "textAnimationType", "totalAyahs", "isArabic", "textFontSize", "textOpacity", "showTextBg", "textBgOpacity", "textBgRadius", "verses", "settingsManager", "language", "fontFamily", "fontWeight", "textColorStr", "textBgColorStr", "textPosition", "textAlign", "textAnimationType", "totalAyahs", "isArabic", "textFontSize", "textOpacity", "showTextBg", "textBgOpacity", "textBgRadius", "translationFontSize", "verses", "settingsManager", "language", "fontFamily", "fontWeight", "textColorStr", "textBgColorStr", "textPosition", "textAlign", "textAnimationType", "translationColorStr", "totalAyahs", "isArabic", "textFontSize", "textOpacity", "showTextBg", "textBgOpacity", "textBgRadius", "translationFontSize", "verses", "settingsManager", "language", "fontFamily", "fontWeight", "textColorStr", "textBgColorStr", "textPosition", "textAlign", "textAnimationType", "translationColorStr", "translationFontFamily", "totalAyahs", "isArabic", "textFontSize", "textOpacity", "showTextBg", "textBgOpacity", "textBgRadius", "translationFontSize", "verses", "settingsManager", "language", "fontFamily", "fontWeight", "textColorStr", "textBgColorStr", "textPosition", "textAlign", "textAnimationType", "translationColorStr", "translationFontFamily", "translationFontWeight", "totalAyahs", "isArabic", "textFontSize", "textOpacity", "showTextBg", "textBgOpacity", "textBgRadius", "translationFontSize", "verses", "settingsManager", "language", "fontFamily", "fontWeight", "textColorStr", "textBgColorStr", "textPosition", "textAlign", "textAnimationType", "translationColorStr", "translationFontFamily", "translationFontWeight", "totalAyahs", "isArabic", "textFontSize", "textOpacity", "showTextBg", "textBgOpacity", "textBgRadius", "translationFontSize", "translationTextX", "verses", "settingsManager", "language", "fontFamily", "fontWeight", "textColorStr", "textBgColorStr", "textPosition", "textAlign", "textAnimationType", "translationColorStr", "translationFontFamily", "translationFontWeight", "totalAyahs", "isArabic", "textFontSize", "textOpacity", "showTextBg", "textBgOpacity", "textBgRadius", "translationFontSize", "translationTextX", "translationTextY", "verses", "settingsManager", "language", "fontFamily", "fontWeight", "textColorStr", "textBgColorStr", "textPosition", "textAlign", "textAnimationType", "translationColorStr", "translationFontFamily", "translationFontWeight", "totalAyahs", "isArabic", "textFontSize", "textOpacity", "showTextBg", "textBgOpacity", "textBgRadius", "translationFontSize", "translationTextX", "translationTextY", "arabicTextX", "verses", "settingsManager", "language", "fontFamily", "fontWeight", "textColorStr", "textBgColorStr", "textPosition", "textAlign", "textAnimationType", "translationColorStr", "translationFontFamily", "translationFontWeight", "totalAyahs", "isArabic", "textFontSize", "textOpacity", "showTextBg", "textBgOpacity", "textBgRadius", "translationFontSize", "translationTextX", "translationTextY", "arabicTextX", "arabicTextY", "verses", "settingsManager", "language", "fontFamily", "fontWeight", "textColorStr", "textBgColorStr", "textPosition", "textAlign", "textAnimationType", "translationColorStr", "translationFontFamily", "translationFontWeight", "totalAyahs", "isArabic", "textFontSize", "textOpacity", "showTextBg", "textBgOpacity", "textBgRadius", "translationFontSize", "translationTextX", "translationTextY", "arabicTextX", "arabicTextY", "surahNameFontSize", "verses", "settingsManager", "language", "fontFamily", "fontWeight", "textColorStr", "textBgColorStr", "textPosition", "textAlign", "textAnimationType", "translationColorStr", "translationFontFamily", "translationFontWeight", "surahNameColorStr", "totalAyahs", "isArabic", "textFontSize", "textOpacity", "showTextBg", "textBgOpacity", "textBgRadius", "translationFontSize", "translationTextX", "translationTextY", "arabicTextX", "arabicTextY", "surahNameFontSize", "verses", "settingsManager", "language", "fontFamily", "fontWeight", "textColorStr", "textBgColorStr", "textPosition", "textAlign", "textAnimationType", "translationColorStr", "translationFontFamily", "translationFontWeight", "surahNameColorStr", "totalAyahs", "isArabic", "textFontSize", "textOpacity", "showTextBg", "textBgOpacity", "textBgRadius", "translationFontSize", "translationTextX", "translationTextY", "arabicTextX", "arabicTextY", "surahNameFontSize", "surahNameOpacity", "verses", "settingsManager", "language", "fontFamily", "fontWeight", "textColorStr", "textBgColorStr", "textPosition", "textAlign", "textAnimationType", "translationColorStr", "translationFontFamily", "translationFontWeight", "surahNameColorStr", "totalAyahs", "isArabic", "textFontSize", "textOpacity", "showTextBg", "textBgOpacity", "textBgRadius", "translationFontSize", "translationTextX", "translationTextY", "arabicTextX", "arabicTextY", "surahNameFontSize", "surahNameOpacity", "surahNameX", "verses", "settingsManager", "language", "fontFamily", "fontWeight", "textColorStr", "textBgColorStr", "textPosition", "textAlign", "textAnimationType", "translationColorStr", "translationFontFamily", "translationFontWeight", "surahNameColorStr", "totalAyahs", "isArabic", "textFontSize", "textOpacity", "showTextBg", "textBgOpacity", "textBgRadius", "translationFontSize", "translationTextX", "translationTextY", "arabicTextX", "arabicTextY", "surahNameFontSize", "surahNameOpacity", "surahNameX", "surahNameY", "verses", "settingsManager", "language", "fontFamily", "fontWeight", "textColorStr", "textBgColorStr", "textPosition", "textAlign", "textAnimationType", "translationColorStr", "translationFontFamily", "translationFontWeight", "surahNameColorStr", "totalAyahs", "isArabic", "textFontSize", "textOpacity", "showTextBg", "textBgOpacity", "textBgRadius", "translationFontSize", "translationTextX", "translationTextY", "arabicTextX", "arabicTextY", "surahNameFontSize", "surahNameOpacity", "surahNameX", "surahNameY", "iconOpacity", "verses", "settingsManager", "language", "fontFamily", "fontWeight", "textColorStr", "textBgColorStr", "textPosition", "textAlign", "textAnimationType", "translationColorStr", "translationFontFamily", "translationFontWeight", "surahNameColorStr", "totalAyahs", "isArabic", "textFontSize", "textOpacity", "showTextBg", "textBgOpacity", "textBgRadius", "translationFontSize", "translationTextX", "translationTextY", "arabicTextX", "arabicTextY", "surahNameFontSize", "surahNameOpacity", "surahNameX", "surahNameY", "iconOpacity", "iconX", "verses", "settingsManager", "language", "fontFamily", "fontWeight", "textColorStr", "textBgColorStr", "textPosition", "textAlign", "textAnimationType", "translationColorStr", "translationFontFamily", "translationFontWeight", "surahNameColorStr", "totalAyahs", "isArabic", "textFontSize", "textOpacity", "showTextBg", "textBgOpacity", "textBgRadius", "translationFontSize", "translationTextX", "translationTextY", "arabicTextX", "arabicTextY", "surahNameFontSize", "surahNameOpacity", "surahNameX", "surahNameY", "iconOpacity", "iconX", "iconY", "verses", "settingsManager", "language", "fontFamily", "fontWeight", "textColorStr", "textBgColorStr", "textPosition", "textAlign", "textAnimationType", "translationColorStr", "translationFontFamily", "translationFontWeight", "surahNameColorStr", "pixabayApiKey", "totalAyahs", "isArabic", "textFontSize", "textOpacity", "showTextBg", "textBgOpacity", "textBgRadius", "translationFontSize", "translationTextX", "translationTextY", "arabicTextX", "arabicTextY", "surahNameFontSize", "surahNameOpacity", "surahNameX", "surahNameY", "iconOpacity", "iconX", "iconY", "verses", "settingsManager", "language", "fontFamily", "fontWeight", "textColorStr", "textBgColorStr", "textPosition", "textAlign", "textAnimationType", "translationColorStr", "translationFontFamily", "translationFontWeight", "surahNameColorStr", "pixabayApiKey", "backgroundKeywords", "actualReciterId", "basmalahText", "basmalahTranslation", "audioFileName", "url", "destFile", "aacFileName", "aacFile", "timeline", "alignedData", "alignedSegments", "whisperXChunks", "ext", "totalAyahs", "isArabic", "textFontSize", "textOpacity", "showTextBg", "textBgOpacity", "textBgRadius", "translationFontSize", "translationTextX", "translationTextY", "arabicTextX", "arabicTextY", "surahNameFontSize", "surahNameOpacity", "surahNameX", "surahNameY", "iconOpacity", "iconX", "iconY", "isPopularUIState", "isPopularUrlDownload", "isYoutubeUrlDownload", "durationUs", "durationMs", "verses", "settingsManager", "language", "fontFamily", "fontWeight", "textColorStr", "textBgColorStr", "textPosition", "textAlign", "textAnimationType", "translationColorStr", "translationFontFamily", "translationFontWeight", "surahNameColorStr", "pixabayApiKey", "backgroundKeywords", "actualReciterId", "audioUrl", "destFile", "combinedArabic", "combinedTranslation", "fullArabicText", "fullTranslationText", "cacheKeyUrl", "cached", "aacFile", "timeline", "alignedSegments", "smartChunks", "alignedData", "whisperXChunks", "aacFileName", "ext", "totalAyahs", "isArabic", "textFontSize", "textOpacity", "showTextBg", "textBgOpacity", "textBgRadius", "translationFontSize", "translationTextX", "translationTextY", "arabicTextX", "arabicTextY", "surahNameFontSize", "surahNameOpacity", "surahNameX", "surahNameY", "iconOpacity", "iconX", "iconY", "isPopularUIState", "isPopularUrlDownload", "isYoutubeUrlDownload", "durationUs", "durationMs", "verses", "settingsManager", "language", "fontFamily", "fontWeight", "textColorStr", "textBgColorStr", "textPosition", "textAlign", "textAnimationType", "translationColorStr", "translationFontFamily", "translationFontWeight", "surahNameColorStr", "pixabayApiKey", "backgroundKeywords", "actualReciterId", "verseInfo", "text", "translation", "audioFileName", "url", "destFile", "cacheKeyUrl", "cached", "aacFile", "timeline", "alignedSegments", "smartChunks", "aacFileName", "alignedData", "whisperXChunks", "ext", "totalAyahs", "isArabic", "textFontSize", "textOpacity", "showTextBg", "textBgOpacity", "textBgRadius", "translationFontSize", "translationTextX", "translationTextY", "arabicTextX", "arabicTextY", "surahNameFontSize", "surahNameOpacity", "surahNameX", "surahNameY", "iconOpacity", "iconX", "iconY", "isPopularUIState", "isPopularUrlDownload", "isYoutubeUrlDownload", "i", "ayah", "globalAyahNumber", "durationUs", "durationMs", "muxer", "verses", "settingsManager", "language", "fontFamily", "fontWeight", "textColorStr", "textBgColorStr", "textPosition", "textAlign", "textAnimationType", "translationColorStr", "translationFontFamily", "translationFontWeight", "surahNameColorStr", "pixabayApiKey", "backgroundKeywords", "actualReciterId", "downloadedVideoFiles", "outputPath", "finalMuxer", "videoTrackIdx", "audioTrackIdx", "lastWrittenVideoPts", "muxerStarted", "audioFormat", "totalAyahs", "isArabic", "textFontSize", "textOpacity", "showTextBg", "textBgOpacity", "textBgRadius", "translationFontSize", "translationTextX", "translationTextY", "arabicTextX", "arabicTextY", "surahNameFontSize", "surahNameOpacity", "surahNameX", "surahNameY", "iconOpacity", "iconX", "iconY", "isPopularUIState", "isPopularUrlDownload", "isYoutubeUrlDownload", "videoLoaded", "muxer", "verses", "settingsManager", "language", "fontFamily", "fontWeight", "textColorStr", "textBgColorStr", "textPosition", "textAlign", "textAnimationType", "translationColorStr", "translationFontFamily", "translationFontWeight", "surahNameColorStr", "pixabayApiKey", "backgroundKeywords", "actualReciterId", "downloadedVideoFiles", "outputPath", "finalMuxer", "videoTrackIdx", "audioTrackIdx", "lastWrittenVideoPts", "muxerStarted", "audioFormat", "videoResString", "vidBitrate", "totalAyahs", "isArabic", "textFontSize", "textOpacity", "showTextBg", "textBgOpacity", "textBgRadius", "translationFontSize", "translationTextX", "translationTextY", "arabicTextX", "arabicTextY", "surahNameFontSize", "surahNameOpacity", "surahNameX", "surahNameY", "iconOpacity", "iconX", "iconY", "isPopularUIState", "isPopularUrlDownload", "isYoutubeUrlDownload", "videoLoaded", "vidWidth", "vidHeight", "verses", "settingsManager", "language", "fontFamily", "fontWeight", "textColorStr", "textBgColorStr", "textPosition", "textAlign", "textAnimationType", "translationColorStr", "translationFontFamily", "translationFontWeight", "surahNameColorStr", "pixabayApiKey", "backgroundKeywords", "actualReciterId", "downloadedVideoFiles", "outputPath", "finalMuxer", "videoTrackIdx", "audioTrackIdx", "lastWrittenVideoPts", "muxerStarted", "audioFormat", "videoResString", "vidBitrate", "videoFormat", "encoder", "drainLatch", "drainThread", "verseStartTimestampsUs", "audioThread", "uri", "finalUri", "totalAyahs", "isArabic", "textFontSize", "textOpacity", "showTextBg", "textBgOpacity", "textBgRadius", "translationFontSize", "translationTextX", "translationTextY", "arabicTextX", "arabicTextY", "surahNameFontSize", "surahNameOpacity", "surahNameX", "surahNameY", "iconOpacity", "iconX", "iconY", "isPopularUIState", "isPopularUrlDownload", "isYoutubeUrlDownload", "videoLoaded", "vidWidth", "vidHeight", "fpsVal", "currentStartUs", "fps", "frameDurationUs", "eosIdx", "totalReelDurationUs", "drainCompleted", "videoCodec", "muxer", "e", "errorMsg"}, s = {"L$0", "L$1", "I$0", "L$0", "L$1", "L$2", "I$0", "Z$0", "L$0", "L$1", "L$2", "L$3", "I$0", "Z$0", "L$0", "L$1", "L$2", "L$3", "L$4", "I$0", "Z$0", "L$0", "L$1", "L$2", "L$3", "L$4", "I$0", "Z$0", "I$1", "L$0", "L$1", "L$2", "L$3", "L$4", "L$5", "I$0", "Z$0", "I$1", "L$0", "L$1", "L$2", "L$3", "L$4", "L$5", "I$0", "Z$0", "I$1", "F$0", "L$0", "L$1", "L$2", "L$3", "L$4", "L$5", "I$0", "Z$0", "I$1", "F$0", "Z$1", "L$0", "L$1", "L$2", "L$3", "L$4", "L$5", "L$6", "I$0", "Z$0", "I$1", "F$0", "Z$1", "L$0", "L$1", "L$2", "L$3", "L$4", "L$5", "L$6", "I$0", "Z$0", "I$1", "F$0", "Z$1", "F$1", "L$0", "L$1", "L$2", "L$3", "L$4", "L$5", "L$6", "I$0", "Z$0", "I$1", "F$0", "Z$1", "F$1", "I$2", "L$0", "L$1", "L$2", "L$3", "L$4", "L$5", "L$6", "L$7", "I$0", "Z$0", "I$1", "F$0", "Z$1", "F$1", "I$2", "L$0", "L$1", "L$2", "L$3", "L$4", "L$5", "L$6", "L$7", "L$8", "I$0", "Z$0", "I$1", "F$0", "Z$1", "F$1", "I$2", "L$0", "L$1", "L$2", "L$3", "L$4", "L$5", "L$6", "L$7", "L$8", "L$9", "I$0", "Z$0", "I$1", "F$0", "Z$1", "F$1", "I$2", "L$0", "L$1", "L$2", "L$3", "L$4", "L$5", "L$6", "L$7", "L$8", "L$9", "I$0", "Z$0", "I$1", "F$0", "Z$1", "F$1", "I$2", "I$3", "L$0", "L$1", "L$2", "L$3", "L$4", "L$5", "L$6", "L$7", "L$8", "L$9", "L$10", "I$0", "Z$0", "I$1", "F$0", "Z$1", "F$1", "I$2", "I$3", "L$0", "L$1", "L$2", "L$3", "L$4", "L$5", "L$6", "L$7", "L$8", "L$9", "L$10", "L$11", "I$0", "Z$0", "I$1", "F$0", "Z$1", "F$1", "I$2", "I$3", "L$0", "L$1", "L$2", "L$3", "L$4", "L$5", "L$6", "L$7", "L$8", "L$9", "L$10", "L$11", "L$12", "I$0", "Z$0", "I$1", "F$0", "Z$1", "F$1", "I$2", "I$3", "L$0", "L$1", "L$2", "L$3", "L$4", "L$5", "L$6", "L$7", "L$8", "L$9", "L$10", "L$11", "L$12", "I$0", "Z$0", "I$1", "F$0", "Z$1", "F$1", "I$2", "I$3", "I$4", "L$0", "L$1", "L$2", "L$3", "L$4", "L$5", "L$6", "L$7", "L$8", "L$9", "L$10", "L$11", "L$12", "I$0", "Z$0", "I$1", "F$0", "Z$1", "F$1", "I$2", "I$3", "I$4", "I$5", "L$0", "L$1", "L$2", "L$3", "L$4", "L$5", "L$6", "L$7", "L$8", "L$9", "L$10", "L$11", "L$12", "I$0", "Z$0", "I$1", "F$0", "Z$1", "F$1", "I$2", "I$3", "I$4", "I$5", "I$6", "L$0", "L$1", "L$2", "L$3", "L$4", "L$5", "L$6", "L$7", "L$8", "L$9", "L$10", "L$11", "L$12", "I$0", "Z$0", "I$1", "F$0", "Z$1", "F$1", "I$2", "I$3", "I$4", "I$5", "I$6", "I$7", "L$0", "L$1", "L$2", "L$3", "L$4", "L$5", "L$6", "L$7", "L$8", "L$9", "L$10", "L$11", "L$12", "I$0", "Z$0", "I$1", "F$0", "Z$1", "F$1", "I$2", "I$3", "I$4", "I$5", "I$6", "I$7", "I$8", "L$0", "L$1", "L$2", "L$3", "L$4", "L$5", "L$6", "L$7", "L$8", "L$9", "L$10", "L$11", "L$12", "L$13", "I$0", "Z$0", "I$1", "F$0", "Z$1", "F$1", "I$2", "I$3", "I$4", "I$5", "I$6", "I$7", "I$8", "L$0", "L$1", "L$2", "L$3", "L$4", "L$5", "L$6", "L$7", "L$8", "L$9", "L$10", "L$11", "L$12", "L$13", "I$0", "Z$0", "I$1", "F$0", "Z$1", "F$1", "I$2", "I$3", "I$4", "I$5", "I$6", "I$7", "I$8", "F$2", "L$0", "L$1", "L$2", "L$3", "L$4", "L$5", "L$6", "L$7", "L$8", "L$9", "L$10", "L$11", "L$12", "L$13", "I$0", "Z$0", "I$1", "F$0", "Z$1", "F$1", "I$2", "I$3", "I$4", "I$5", "I$6", "I$7", "I$8", "F$2", "I$9", "L$0", "L$1", "L$2", "L$3", "L$4", "L$5", "L$6", "L$7", "L$8", "L$9", "L$10", "L$11", "L$12", "L$13", "I$0", "Z$0", "I$1", "F$0", "Z$1", "F$1", "I$2", "I$3", "I$4", "I$5", "I$6", "I$7", "I$8", "F$2", "I$9", "I$10", "L$0", "L$1", "L$2", "L$3", "L$4", "L$5", "L$6", "L$7", "L$8", "L$9", "L$10", "L$11", "L$12", "L$13", "I$0", "Z$0", "I$1", "F$0", "Z$1", "F$1", "I$2", "I$3", "I$4", "I$5", "I$6", "I$7", "I$8", "F$2", "I$9", "I$10", "F$3", "L$0", "L$1", "L$2", "L$3", "L$4", "L$5", "L$6", "L$7", "L$8", "L$9", "L$10", "L$11", "L$12", "L$13", "I$0", "Z$0", "I$1", "F$0", "Z$1", "F$1", "I$2", "I$3", "I$4", "I$5", "I$6", "I$7", "I$8", "F$2", "I$9", "I$10", "F$3", "I$11", "L$0", "L$1", "L$2", "L$3", "L$4", "L$5", "L$6", "L$7", "L$8", "L$9", "L$10", "L$11", "L$12", "L$13", "I$0", "Z$0", "I$1", "F$0", "Z$1", "F$1", "I$2", "I$3", "I$4", "I$5", "I$6", "I$7", "I$8", "F$2", "I$9", "I$10", "F$3", "I$11", "I$12", "L$0", "L$1", "L$2", "L$3", "L$4", "L$5", "L$6", "L$7", "L$8", "L$9", "L$10", "L$11", "L$12", "L$13", "L$14", "I$0", "Z$0", "I$1", "F$0", "Z$1", "F$1", "I$2", "I$3", "I$4", "I$5", "I$6", "I$7", "I$8", "F$2", "I$9", "I$10", "F$3", "I$11", "I$12", "L$0", "L$1", "L$2", "L$3", "L$4", "L$5", "L$6", "L$7", "L$8", "L$9", "L$10", "L$11", "L$12", "L$13", "L$14", "L$15", "L$16", "L$17", "L$18", "L$19", "L$20", "L$21", "L$22", "L$23", "L$24", "L$25", "L$26", "L$27", "L$28", "I$0", "Z$0", "I$1", "F$0", "Z$1", "F$1", "I$2", "I$3", "I$4", "I$5", "I$6", "I$7", "I$8", "F$2", "I$9", "I$10", "F$3", "I$11", "I$12", "Z$2", "I$13", "I$14", "J$0", "J$1", "L$0", "L$1", "L$2", "L$3", "L$4", "L$5", "L$6", "L$7", "L$8", "L$9", "L$10", "L$11", "L$12", "L$13", "L$14", "L$15", "L$16", "L$17", "L$18", "L$19", "L$20", "L$21", "L$22", "L$23", "L$24", "L$25", "L$26", "L$27", "L$28", "L$29", "L$30", "L$31", "L$32", "I$0", "Z$0", "I$1", "F$0", "Z$1", "F$1", "I$2", "I$3", "I$4", "I$5", "I$6", "I$7", "I$8", "F$2", "I$9", "I$10", "F$3", "I$11", "I$12", "Z$2", "I$13", "I$14", "J$0", "J$1", "L$0", "L$1", "L$2", "L$3", "L$4", "L$5", "L$6", "L$7", "L$8", "L$9", "L$10", "L$11", "L$12", "L$13", "L$14", "L$15", "L$16", "L$17", "L$18", "L$19", "L$20", "L$21", "L$22", "L$23", "L$24", "L$25", "L$26", "L$27", "L$28", "L$29", "L$30", "L$31", "L$32", "I$0", "Z$0", "I$1", "F$0", "Z$1", "F$1", "I$2", "I$3", "I$4", "I$5", "I$6", "I$7", "I$8", "F$2", "I$9", "I$10", "F$3", "I$11", "I$12", "Z$2", "I$13", "I$14", "I$15", "I$16", "I$17", "J$0", "J$1", "L$0", "L$1", "L$2", "L$3", "L$4", "L$5", "L$6", "L$7", "L$8", "L$9", "L$10", "L$11", "L$12", "L$13", "L$14", "L$15", "L$16", "L$17", "L$18", "L$19", "L$20", "L$21", "L$22", "L$23", "L$24", "L$25", "I$0", "Z$0", "I$1", "F$0", "Z$1", "F$1", "I$2", "I$3", "I$4", "I$5", "I$6", "I$7", "I$8", "F$2", "I$9", "I$10", "F$3", "I$11", "I$12", "Z$2", "I$13", "I$14", "I$15", "L$0", "L$1", "L$2", "L$3", "L$4", "L$5", "L$6", "L$7", "L$8", "L$9", "L$10", "L$11", "L$12", "L$13", "L$14", "L$15", "L$16", "L$17", "L$18", "L$19", "L$20", "L$21", "L$22", "L$23", "L$24", "L$25", "L$26", "L$27", "I$0", "Z$0", "I$1", "F$0", "Z$1", "F$1", "I$2", "I$3", "I$4", "I$5", "I$6", "I$7", "I$8", "F$2", "I$9", "I$10", "F$3", "I$11", "I$12", "Z$2", "I$13", "I$14", "I$15", "I$16", "I$17", "L$0", "L$1", "L$2", "L$3", "L$4", "L$5", "L$6", "L$7", "L$8", "L$9", "L$10", "L$11", "L$12", "L$13", "L$14", "L$15", "L$16", "L$17", "L$18", "L$19", "L$20", "L$21", "L$22", "L$23", "L$24", "L$25", "L$26", "L$27", "L$28", "L$29", "L$30", "L$31", "L$32", "L$33", "L$34", "I$0", "Z$0", "I$1", "F$0", "Z$1", "F$1", "I$2", "I$3", "I$4", "I$5", "I$6", "I$7", "I$8", "F$2", "I$9", "I$10", "F$3", "I$11", "I$12", "Z$2", "I$13", "I$14", "I$15", "I$16", "I$17", "I$18", "J$0", "I$19", "J$1", "I$20", "J$2", "Z$3", "L$0", "L$1", "L$2", "L$3"})
/* loaded from: /app/applet/classes5.dex */
public final class VideoGenerator$generateReel$2 extends SuspendLambda implements Function2<CoroutineScope, Continuation<? super Unit>, Object> {
    final /* synthetic */ Context $context;
    final /* synthetic */ int $endAyah;
    final /* synthetic */ boolean $includeBasmalah;
    final /* synthetic */ boolean $isPreviewMode;
    final /* synthetic */ boolean $isRetry;
    final /* synthetic */ Function1<Uri, Unit> $onComplete;
    final /* synthetic */ Function1<String, Unit> $onError;
    final /* synthetic */ Function2<String, Float, Unit> $onProgress;
    final /* synthetic */ String $pexelsApiKey;
    final /* synthetic */ String $reciterId;
    final /* synthetic */ boolean $showTranslation;
    final /* synthetic */ int $startAyah;
    final /* synthetic */ int $surah;
    final /* synthetic */ String $videoQuality;
    final /* synthetic */ String $videoQuery;
    float F$0;
    float F$1;
    float F$2;
    float F$3;
    int I$0;
    int I$1;
    int I$10;
    int I$11;
    int I$12;
    int I$13;
    int I$14;
    int I$15;
    int I$16;
    int I$17;
    int I$18;
    int I$19;
    int I$2;
    int I$20;
    int I$3;
    int I$4;
    int I$5;
    int I$6;
    int I$7;
    int I$8;
    int I$9;
    long J$0;
    long J$1;
    long J$2;
    Object L$0;
    Object L$1;
    Object L$10;
    Object L$11;
    Object L$12;
    Object L$13;
    Object L$14;
    Object L$15;
    Object L$16;
    Object L$17;
    Object L$18;
    Object L$19;
    Object L$2;
    Object L$20;
    Object L$21;
    Object L$22;
    Object L$23;
    Object L$24;
    Object L$25;
    Object L$26;
    Object L$27;
    Object L$28;
    Object L$29;
    Object L$3;
    Object L$30;
    Object L$31;
    Object L$32;
    Object L$33;
    Object L$34;
    Object L$4;
    Object L$5;
    Object L$6;
    Object L$7;
    Object L$8;
    Object L$9;
    boolean Z$0;
    boolean Z$1;
    boolean Z$2;
    boolean Z$3;
    int label;
    final /* synthetic */ VideoGenerator this$0;

    /* JADX INFO: Access modifiers changed from: package-private */
    /* JADX WARN: 'super' call moved to the top of the method (can break code semantics) */
    /* JADX WARN: Multi-variable type inference failed */
    public VideoGenerator$generateReel$2(int i, int i2, int i3, String str, boolean z, VideoGenerator videoGenerator, Context context, Function2<? super String, ? super Float, Unit> function2, boolean z2, boolean z3, String str2, String str3, String str4, boolean z4, Function1<? super Uri, Unit> function1, Function1<? super String, Unit> function12, Continuation<? super VideoGenerator$generateReel$2> continuation) {
        super(2, continuation);
        this.$surah = i;
        this.$startAyah = i2;
        this.$endAyah = i3;
        this.$reciterId = str;
        this.$showTranslation = z;
        this.this$0 = videoGenerator;
        this.$context = context;
        this.$onProgress = function2;
        this.$includeBasmalah = z2;
        this.$isRetry = z3;
        this.$pexelsApiKey = str2;
        this.$videoQuery = str3;
        this.$videoQuality = str4;
        this.$isPreviewMode = z4;
        this.$onComplete = function1;
        this.$onError = function12;
    }

    public final Continuation<Unit> create(Object obj, Continuation<?> continuation) {
        return new VideoGenerator$generateReel$2(this.$surah, this.$startAyah, this.$endAyah, this.$reciterId, this.$showTranslation, this.this$0, this.$context, this.$onProgress, this.$includeBasmalah, this.$isRetry, this.$pexelsApiKey, this.$videoQuery, this.$videoQuality, this.$isPreviewMode, this.$onComplete, this.$onError, continuation);
    }

    public final Object invoke(CoroutineScope coroutineScope, Continuation<? super Unit> continuation) {
        return create(coroutineScope, continuation).invokeSuspend(Unit.INSTANCE);
    }

    /*  JADX ERROR: JadxRuntimeException in pass: BlockProcessor
        jadx.core.utils.exceptions.JadxRuntimeException: Unreachable block: B:845:0x49d2
        	at jadx.core.dex.visitors.blocks.BlockProcessor.checkForUnreachableBlocks(BlockProcessor.java:81)
        	at jadx.core.dex.visitors.blocks.BlockProcessor.processBlocksTree(BlockProcessor.java:47)
        	at jadx.core.dex.visitors.blocks.BlockProcessor.visit(BlockProcessor.java:39)
        */
    public final java.lang.Object invokeSuspend(java.lang.Object r355) {
        /*
            Method dump skipped, instructions count: 27658
            To view this dump add '--comments-level debug' option
        */
        throw new UnsupportedOperationException("Method not decompiled: com.example.generator.VideoGenerator$generateReel$2.invokeSuspend(java.lang.Object):java.lang.Object");
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    /* JADX WARN: Incorrect condition in loop: B:5:0x0014 */
    /*
        Code decompiled incorrectly, please refer to instructions dump.
        To view partially-correct add '--show-bad-code' argument
    */
    public static final kotlin.Unit invokeSuspend$lambda$21(com.example.generator.VideoGenerator r16, android.media.MediaCodec r17, android.media.MediaFormat r18, kotlin.jvm.internal.Ref.IntRef r19, android.media.MediaMuxer r20, kotlin.jvm.internal.Ref.IntRef r21, java.util.concurrent.atomic.AtomicBoolean r22, java.util.concurrent.CountDownLatch r23, kotlin.jvm.internal.Ref.LongRef r24) {
        /*
            Method dump skipped, instructions count: 338
            To view this dump add '--comments-level debug' option
        */
        throw new UnsupportedOperationException("Method not decompiled: com.example.generator.VideoGenerator$generateReel$2.invokeSuspend$lambda$21(com.example.generator.VideoGenerator, android.media.MediaCodec, android.media.MediaFormat, kotlin.jvm.internal.Ref$IntRef, android.media.MediaMuxer, kotlin.jvm.internal.Ref$IntRef, java.util.concurrent.atomic.AtomicBoolean, java.util.concurrent.CountDownLatch, kotlin.jvm.internal.Ref$LongRef):kotlin.Unit");
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    /* JADX WARN: Code restructure failed: missing block: B:35:0x00c8, code lost:
        r25 = 0;
     */
    /*
        Code decompiled incorrectly, please refer to instructions dump.
        To view partially-correct add '--show-bad-code' argument
    */
    public static final kotlin.Unit invokeSuspend$lambda$24(java.util.List r28, com.example.generator.VideoGenerator r29, java.util.List r30, java.util.concurrent.atomic.AtomicBoolean r31, java.util.concurrent.CountDownLatch r32, android.media.MediaMuxer r33, kotlin.jvm.internal.Ref.IntRef r34) {
        /*
            Method dump skipped, instructions count: 368
            To view this dump add '--comments-level debug' option
        */
        throw new UnsupportedOperationException("Method not decompiled: com.example.generator.VideoGenerator$generateReel$2.invokeSuspend$lambda$24(java.util.List, com.example.generator.VideoGenerator, java.util.List, java.util.concurrent.atomic.AtomicBoolean, java.util.concurrent.CountDownLatch, android.media.MediaMuxer, kotlin.jvm.internal.Ref$IntRef):kotlin.Unit");
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    /* compiled from: VideoGenerator.kt */
    @Metadata(d1 = {"\u0000\n\n\u0000\n\u0002\u0010\u0002\n\u0002\u0018\u0002\u0010\u0000\u001a\u00020\u0001*\u00020\u0002H\n"}, d2 = {"<anonymous>", "", "Lkotlinx/coroutines/CoroutineScope;"}, k = 3, mv = {2, 2, 0}, xi = 48)
    @DebugMetadata(c = "com.example.generator.VideoGenerator$generateReel$2$5", f = "VideoGenerator.kt", i = {}, l = {}, m = "invokeSuspend", n = {}, s = {})
    /* renamed from: com.example.generator.VideoGenerator$generateReel$2$5  reason: invalid class name */
    /* loaded from: /app/applet/classes5.dex */
    public static final class AnonymousClass5 extends SuspendLambda implements Function2<CoroutineScope, Continuation<? super Unit>, Object> {
        final /* synthetic */ Uri $finalUri;
        final /* synthetic */ Function1<Uri, Unit> $onComplete;
        int label;

        /* JADX WARN: 'super' call moved to the top of the method (can break code semantics) */
        /* JADX WARN: Multi-variable type inference failed */
        AnonymousClass5(Function1<? super Uri, Unit> function1, Uri uri, Continuation<? super AnonymousClass5> continuation) {
            super(2, continuation);
            this.$onComplete = function1;
            this.$finalUri = uri;
        }

        public final Continuation<Unit> create(Object obj, Continuation<?> continuation) {
            return new AnonymousClass5(this.$onComplete, this.$finalUri, continuation);
        }

        public final Object invoke(CoroutineScope coroutineScope, Continuation<? super Unit> continuation) {
            return create(coroutineScope, continuation).invokeSuspend(Unit.INSTANCE);
        }

        public final Object invokeSuspend(Object $result) {
            IntrinsicsKt.getCOROUTINE_SUSPENDED();
            switch (this.label) {
                case 0:
                    ResultKt.throwOnFailure($result);
                    this.$onComplete.invoke(this.$finalUri);
                    return Unit.INSTANCE;
                default:
                    throw new IllegalStateException("call to 'resume' before 'invoke' with coroutine");
            }
        }
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    /* compiled from: VideoGenerator.kt */
    @Metadata(d1 = {"\u0000\n\n\u0000\n\u0002\u0010\u0002\n\u0002\u0018\u0002\u0010\u0000\u001a\u00020\u0001*\u00020\u0002H\n"}, d2 = {"<anonymous>", "", "Lkotlinx/coroutines/CoroutineScope;"}, k = 3, mv = {2, 2, 0}, xi = 48)
    @DebugMetadata(c = "com.example.generator.VideoGenerator$generateReel$2$6", f = "VideoGenerator.kt", i = {}, l = {}, m = "invokeSuspend", n = {}, s = {})
    /* renamed from: com.example.generator.VideoGenerator$generateReel$2$6  reason: invalid class name */
    /* loaded from: /app/applet/classes5.dex */
    public static final class AnonymousClass6 extends SuspendLambda implements Function2<CoroutineScope, Continuation<? super Unit>, Object> {
        final /* synthetic */ String $errorMsg;
        final /* synthetic */ Function1<String, Unit> $onError;
        int label;

        /* JADX WARN: 'super' call moved to the top of the method (can break code semantics) */
        /* JADX WARN: Multi-variable type inference failed */
        AnonymousClass6(Function1<? super String, Unit> function1, String str, Continuation<? super AnonymousClass6> continuation) {
            super(2, continuation);
            this.$onError = function1;
            this.$errorMsg = str;
        }

        public final Continuation<Unit> create(Object obj, Continuation<?> continuation) {
            return new AnonymousClass6(this.$onError, this.$errorMsg, continuation);
        }

        public final Object invoke(CoroutineScope coroutineScope, Continuation<? super Unit> continuation) {
            return create(coroutineScope, continuation).invokeSuspend(Unit.INSTANCE);
        }

        public final Object invokeSuspend(Object $result) {
            IntrinsicsKt.getCOROUTINE_SUSPENDED();
            switch (this.label) {
                case 0:
                    ResultKt.throwOnFailure($result);
                    this.$onError.invoke(this.$errorMsg);
                    return Unit.INSTANCE;
                default:
                    throw new IllegalStateException("call to 'resume' before 'invoke' with coroutine");
            }
        }
    }
}
