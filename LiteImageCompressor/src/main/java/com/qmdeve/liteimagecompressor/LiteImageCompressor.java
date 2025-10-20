package com.qmdeve.liteimagecompressor;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Looper;

import com.qmdeve.liteimagecompressor.callback.CompressCallback;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

public class LiteImageCompressor {
    private CompressConfig config;
    private CompressCallback callback;
    private Handler mainHandler;

    private LiteImageCompressor(Builder builder) {
        this.config = builder.config;
        this.callback = builder.callback;
        this.mainHandler = new Handler(Looper.getMainLooper());
    }

    public CompressResult compressSync(String filePath) {
        return compressFromFile(new File(filePath));
    }

    public CompressResult compressSync(File file) {
        return compressFromFile(file);
    }

    public CompressResult compressSync(Bitmap bitmap) {
        return compressFromBitmap(bitmap, 0);
    }

    public CompressResult compressSync(byte[] data) {
        Bitmap bitmap = BitmapFactory.decodeByteArray(data, 0, data.length);
        long originalSize = data.length;
        return compressFromBitmap(bitmap, originalSize);
    }

    public void compressAsync(String filePath) {
        new CompressTask().execute(filePath);
    }

    public void compressAsync(File file) {
        new CompressTask().execute(file);
    }

    public void compressAsync(Bitmap bitmap) {
        new CompressTask().execute(bitmap);
    }

    public void compressAsync(byte[] data) {
        new CompressTask().execute(data);
    }

    private CompressResult compressFromFile(File file) {
        if (file == null || !file.exists()) {
            return CompressResult.builder()
                    .setSuccess(false)
                    .setErrorMessage("File does not exist")
                    .build();
        }

        try {
            long originalFileSize = file.length();
            long originalFileSizeKB = originalFileSize / 1024;

            if (originalFileSizeKB <= config.getMaxSize()) {
                Bitmap originalBitmap = getBitmapFromFile(file);
                if (originalBitmap == null) {
                    return CompressResult.builder()
                            .setSuccess(false)
                            .setErrorMessage("Failed to load original bitmap")
                            .build();
                }

                return CompressResult.builder()
                        .setSuccess(true)
                        .setCompressedBitmap(originalBitmap)
                        .setOriginalSize(originalFileSize)
                        .setCompressedSize(originalFileSize)
                        .setWasCompressed(false)
                        .build();
            }

            Bitmap originalBitmap = getBitmapFromFile(file);
            if (originalBitmap == null) {
                return CompressResult.builder()
                        .setSuccess(false)
                        .setErrorMessage("Failed to load original bitmap")
                        .build();
            }

            return performCompression(originalBitmap, originalFileSize);

        } catch (Exception e) {
            return CompressResult.builder()
                    .setSuccess(false)
                    .setErrorMessage("Compression failed: " + e.getMessage())
                    .build();
        }
    }

    private CompressResult compressFromBitmap(Bitmap bitmap, long originalSize) {
        if (bitmap == null) {
            return CompressResult.builder()
                    .setSuccess(false)
                    .setErrorMessage("Original bitmap is null")
                    .build();
        }

        try {
            if (originalSize <= 0) {
                originalSize = estimateOriginalSize(bitmap);
            }

            long originalSizeKB = originalSize / 1024;

            if (originalSizeKB <= config.getMaxSize()) {
                return CompressResult.builder()
                        .setSuccess(true)
                        .setCompressedBitmap(bitmap)
                        .setOriginalSize(originalSize)
                        .setCompressedSize(originalSize)
                        .setWasCompressed(false)
                        .build();
            }

            return performCompression(bitmap, originalSize);

        } catch (Exception e) {
            return CompressResult.builder()
                    .setSuccess(false)
                    .setErrorMessage("Compression failed: " + e.getMessage())
                    .build();
        }
    }

    private CompressResult performCompression(Bitmap originalBitmap, long originalSize) {
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            originalBitmap.compress(config.getFormat(), config.getQuality(), baos);
            byte[] compressedData = baos.toByteArray();
            long compressedSize = compressedData.length;

            if (compressedSize >= originalSize) {
                return CompressResult.builder()
                        .setSuccess(true)
                        .setCompressedBitmap(originalBitmap)
                        .setOriginalSize(originalSize)
                        .setCompressedSize(originalSize)
                        .setWasCompressed(false)
                        .setErrorMessage("Compressed image is larger than original, return original")
                        .build();
            }

            Bitmap compressedBitmap = BitmapFactory.decodeByteArray(compressedData, 0, compressedData.length);
            if (compressedBitmap == null) {
                return CompressResult.builder()
                        .setSuccess(true)
                        .setCompressedBitmap(originalBitmap)
                        .setOriginalSize(originalSize)
                        .setCompressedSize(originalSize)
                        .setWasCompressed(false)
                        .setErrorMessage("Failed to decode compressed image, return original")
                        .build();
            }

            return CompressResult.builder()
                    .setSuccess(true)
                    .setCompressedBitmap(compressedBitmap)
                    .setOriginalSize(originalSize)
                    .setCompressedSize(compressedSize)
                    .setWasCompressed(true)
                    .build();

        } catch (Exception e) {
            return CompressResult.builder()
                    .setSuccess(true)
                    .setCompressedBitmap(originalBitmap)
                    .setOriginalSize(originalSize)
                    .setCompressedSize(originalSize)
                    .setWasCompressed(false)
                    .setErrorMessage("Compression error: " + e.getMessage() + ", return original")
                    .build();
        }
    }

    private long estimateOriginalSize(Bitmap bitmap) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
        return baos.size();
    }

    private Bitmap getBitmapFromFile(File file) {
        try {
            FileInputStream fis = new FileInputStream(file);
            Bitmap bitmap = BitmapFactory.decodeStream(fis);
            fis.close();
            return bitmap;
        } catch (IOException e) {
            return null;
        }
    }

    private Bitmap getBitmapFromFile(String filePath) {
        try {
            File file = new File(filePath);
            return getBitmapFromFile(file);
        } catch (Exception e) {
            return null;
        }
    }

    private class CompressTask extends AsyncTask<Object, Void, CompressResult> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            if (callback != null) {
                mainHandler.post(() -> callback.onStart());
            }
        }

        @Override
        protected CompressResult doInBackground(Object... params) {
            if (params[0] instanceof String) {
                return compressSync((String) params[0]);
            } else if (params[0] instanceof File) {
                return compressSync((File) params[0]);
            } else if (params[0] instanceof Bitmap) {
                return compressSync((Bitmap) params[0]);
            } else if (params[0] instanceof byte[]) {
                return compressSync((byte[]) params[0]);
            }
            return CompressResult.builder()
                    .setSuccess(false)
                    .setErrorMessage("Unsupported input type")
                    .build();
        }

        @Override
        protected void onPostExecute(CompressResult result) {
            super.onPostExecute(result);
            if (callback != null) {
                if (result.isSuccess()) {
                    mainHandler.post(() -> callback.onSuccess(result));
                } else {
                    mainHandler.post(() -> callback.onError(result.getErrorMessage()));
                }
            }
        }
    }

    public static class Builder {
        private CompressConfig config = CompressConfig.builder().build();
        private CompressCallback callback;

        public Builder setConfig(CompressConfig config) {
            this.config = config;
            return this;
        }

        public Builder setCallback(CompressCallback callback) {
            this.callback = callback;
            return this;
        }

        public Builder setMaxSize(int maxSizeKB) {
            this.config = CompressConfig.builder()
                    .setMaxSize(maxSizeKB)
                    .setQuality(config.getQuality())
                    .setFormat(config.getFormat())
                    .build();
            return this;
        }

        public Builder setQuality(int quality) {
            this.config = CompressConfig.builder()
                    .setMaxSize(config.getMaxSize())
                    .setQuality(quality)
                    .setFormat(config.getFormat())
                    .build();
            return this;
        }

        public LiteImageCompressor build() {
            return new LiteImageCompressor(this);
        }
    }

    public static Builder builder() {
        return new Builder();
    }
}