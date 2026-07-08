package com.example.generator;

import android.graphics.Bitmap;
import android.media.Image;
import android.media.MediaCodec;
import android.media.MediaCrypto;
import android.media.MediaExtractor;
import android.media.MediaFormat;
import android.view.Surface;
import java.nio.ByteBuffer;
import kotlin.Metadata;
import kotlin.jvm.internal.Intrinsics;
import kotlin.ranges.RangesKt;
import kotlin.text.StringsKt;
/* compiled from: VideoGenerator.kt */
@Metadata(d1 = {"\u0000F\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0000\n\u0002\u0010\u000e\n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\b\n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u000b\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u0002\n\u0000\b\u0007\u0018\u00002\u00020\u0001B\u000f\u0012\u0006\u0010\u0002\u001a\u00020\u0003¢\u0006\u0004\b\u0004\u0010\u0005J\b\u0010\u0012\u001a\u0004\u0018\u00010\u0013J\u0010\u0010\u0014\u001a\u00020\u00132\u0006\u0010\u0015\u001a\u00020\u0016H\u0002J\u0006\u0010\u0017\u001a\u00020\u0018R\u000e\u0010\u0002\u001a\u00020\u0003X\u0082\u0004¢\u0006\u0002\n\u0000R\u0010\u0010\u0006\u001a\u0004\u0018\u00010\u0007X\u0082\u000e¢\u0006\u0002\n\u0000R\u0010\u0010\b\u001a\u0004\u0018\u00010\tX\u0082\u000e¢\u0006\u0002\n\u0000R\u000e\u0010\n\u001a\u00020\u000bX\u0082\u000e¢\u0006\u0002\n\u0000R\u000e\u0010\f\u001a\u00020\u000bX\u0082\u000e¢\u0006\u0002\n\u0000R\u000e\u0010\r\u001a\u00020\u000bX\u0082\u000e¢\u0006\u0002\n\u0000R\u000e\u0010\u000e\u001a\u00020\u000fX\u0082\u0004¢\u0006\u0002\n\u0000R\u000e\u0010\u0010\u001a\u00020\u0011X\u0082\u000e¢\u0006\u0002\n\u0000¨\u0006\u0019"}, d2 = {"Lcom/example/generator/SequentialFrameDecoder;", "", "videoPath", "", "<init>", "(Ljava/lang/String;)V", "extractor", "Landroid/media/MediaExtractor;", "decoder", "Landroid/media/MediaCodec;", "width", "", "height", "trackIndex", "bufferInfo", "Landroid/media/MediaCodec$BufferInfo;", "isEOS", "", "getNextFrame", "Landroid/graphics/Bitmap;", "convertYUVImageToBitmap", "image", "Landroid/media/Image;", "release", "", "app"}, k = 1, mv = {2, 2, 0}, xi = 48)
/* loaded from: /app/applet/classes5.dex */
public final class SequentialFrameDecoder {
    public static final int $stable = 8;
    private final MediaCodec.BufferInfo bufferInfo;
    private MediaCodec decoder;
    private MediaExtractor extractor;
    private int height;
    private boolean isEOS;
    private int trackIndex;
    private final String videoPath;
    private int width;

    public SequentialFrameDecoder(String videoPath) {
        Intrinsics.checkNotNullParameter(videoPath, "videoPath");
        this.videoPath = videoPath;
        this.width = 720;
        this.height = 1280;
        this.trackIndex = -1;
        this.bufferInfo = new MediaCodec.BufferInfo();
        try {
            MediaExtractor ext = new MediaExtractor();
            ext.setDataSource(this.videoPath);
            this.extractor = ext;
            int trackCount = ext.getTrackCount();
            for (int i = 0; i < trackCount; i++) {
                MediaFormat format = ext.getTrackFormat(i);
                Intrinsics.checkNotNullExpressionValue(format, "getTrackFormat(...)");
                String mime = format.getString("mime");
                mime = mime == null ? "" : mime;
                if (StringsKt.startsWith$default(mime, "video/", false, 2, (Object) null)) {
                    ext.selectTrack(i);
                    this.trackIndex = i;
                    this.width = format.getInteger("width");
                    this.height = format.getInteger("height");
                    MediaCodec dec = MediaCodec.createDecoderByType(mime);
                    Intrinsics.checkNotNullExpressionValue(dec, "createDecoderByType(...)");
                    format.setInteger("color-format", 2135033992);
                    dec.configure(format, (Surface) null, (MediaCrypto) null, 0);
                    dec.start();
                    this.decoder = dec;
                    return;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            release();
        }
    }

    public final Bitmap getNextFrame() {
        MediaExtractor ext;
        int inIdx;
        MediaCodec dec = this.decoder;
        if (dec == null || (ext = this.extractor) == null || this.trackIndex == -1) {
            return null;
        }
        int attempts = 0;
        while (attempts < 80) {
            int attempts2 = attempts + 1;
            try {
                if (!this.isEOS && (inIdx = dec.dequeueInputBuffer(5000L)) >= 0) {
                    ByteBuffer buf = dec.getInputBuffer(inIdx);
                    Intrinsics.checkNotNull(buf);
                    int sampleSize = ext.readSampleData(buf, 0);
                    if (sampleSize < 0) {
                        dec.queueInputBuffer(inIdx, 0, 0, 0L, 4);
                        this.isEOS = true;
                    } else {
                        dec.queueInputBuffer(inIdx, 0, sampleSize, ext.getSampleTime(), 0);
                        ext.advance();
                    }
                }
                int outIdx = dec.dequeueOutputBuffer(this.bufferInfo, 5000L);
                if (outIdx >= 0) {
                    Bitmap bitmap = null;
                    try {
                        Image image = dec.getOutputImage(outIdx);
                        if (image != null) {
                            bitmap = convertYUVImageToBitmap(image);
                            image.close();
                        }
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                    dec.releaseOutputBuffer(outIdx, false);
                    if (bitmap != null) {
                        return bitmap;
                    }
                } else if (outIdx == -2) {
                    MediaFormat format = dec.getOutputFormat();
                    Intrinsics.checkNotNullExpressionValue(format, "getOutputFormat(...)");
                    this.width = format.getInteger("width");
                    this.height = format.getInteger("height");
                } else if (this.isEOS && (this.bufferInfo.flags & 4) != 0) {
                    ext.seekTo(0L, 2);
                    this.isEOS = false;
                    dec.flush();
                }
                attempts = attempts2;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    private final Bitmap convertYUVImageToBitmap(Image image) {
        int w = image.getWidth();
        int h = image.getHeight();
        boolean z = false;
        Image.Plane yPlane = image.getPlanes()[0];
        Image.Plane uPlane = image.getPlanes()[1];
        Image.Plane vPlane = image.getPlanes()[2];
        ByteBuffer yBuffer = yPlane.getBuffer();
        ByteBuffer uBuffer = uPlane.getBuffer();
        ByteBuffer vBuffer = vPlane.getBuffer();
        int yRowStride = yPlane.getRowStride();
        int uRowStride = uPlane.getRowStride();
        int vRowStride = vPlane.getRowStride();
        int uPixelStride = uPlane.getPixelStride();
        int vPixelStride = vPlane.getPixelStride();
        Bitmap bitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
        Intrinsics.checkNotNullExpressionValue(bitmap, "createBitmap(...)");
        int[] pixels = new int[w * h];
        int y = 0;
        int index = 0;
        while (y < h) {
            int yRowStart = y * yRowStride;
            int x = 0;
            while (x < w) {
                Bitmap bitmap2 = bitmap;
                int yValue = yBuffer.get(yRowStart + x) & 255;
                int uvIndex = ((y / 2) * uRowStride) + ((x / 2) * uPixelStride);
                int[] pixels2 = pixels;
                int vIndex = ((y / 2) * vRowStride) + ((x / 2) * vPixelStride);
                int w2 = w;
                int uValue = uvIndex < uBuffer.capacity() ? (uBuffer.get(uvIndex) & 255) - 128 : 0;
                int vValue = vIndex < vBuffer.capacity() ? (vBuffer.get(vIndex) & 255) - 128 : 0;
                int rCol = (int) (yValue + (vValue * 1.370705f));
                int yRowStart2 = yRowStart;
                int gCol = (int) ((yValue - (uValue * 0.337633f)) - (vValue * 0.698001f));
                int bCol = (int) (yValue + (uValue * 1.732446f));
                pixels2[index] = (RangesKt.coerceIn(rCol, 0, 255) << 16) | (-16777216) | (RangesKt.coerceIn(gCol, 0, 255) << 8) | RangesKt.coerceIn(bCol, 0, 255);
                x++;
                index++;
                z = false;
                bitmap = bitmap2;
                w = w2;
                pixels = pixels2;
                yRowStart = yRowStart2;
            }
            y++;
            w = w;
        }
        bitmap.setPixels(pixels, 0, w, 0, 0, w, h);
        return bitmap;
    }

    public final void release() {
        try {
            MediaCodec mediaCodec = this.decoder;
            if (mediaCodec != null) {
                mediaCodec.stop();
            }
            MediaCodec mediaCodec2 = this.decoder;
            if (mediaCodec2 != null) {
                mediaCodec2.release();
            }
        } catch (Exception e) {
        }
        this.decoder = null;
        try {
            MediaExtractor mediaExtractor = this.extractor;
            if (mediaExtractor != null) {
                mediaExtractor.release();
            }
        } catch (Exception e2) {
        }
        this.extractor = null;
    }
}
