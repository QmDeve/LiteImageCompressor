package com.qmdeve.liteimagecompressor;

import android.graphics.Bitmap;

public class CompressResult {
    private boolean success;
    private Bitmap compressedBitmap;
    private String errorMessage;
    private long originalSize;
    private long compressedSize;
    private boolean wasCompressed;

    private CompressResult(Builder builder) {
        this.success = builder.success;
        this.compressedBitmap = builder.compressedBitmap;
        this.errorMessage = builder.errorMessage;
        this.originalSize = builder.originalSize;
        this.compressedSize = builder.compressedSize;
        this.wasCompressed = builder.wasCompressed;
    }

    public boolean isSuccess() { return success; }
    public Bitmap getCompressedBitmap() { return compressedBitmap; }
    public String getErrorMessage() { return errorMessage; }
    public long getOriginalSize() { return originalSize; }
    public long getCompressedSize() { return compressedSize; }
    public boolean wasCompressed() { return wasCompressed; }

    public static class Builder {
        private boolean success;
        private Bitmap compressedBitmap;
        private String errorMessage;
        private long originalSize;
        private long compressedSize;
        private boolean wasCompressed;

        public Builder setSuccess(boolean success) {
            this.success = success;
            return this;
        }

        public Builder setCompressedBitmap(Bitmap compressedBitmap) {
            this.compressedBitmap = compressedBitmap;
            return this;
        }

        public Builder setErrorMessage(String errorMessage) {
            this.errorMessage = errorMessage;
            return this;
        }

        public Builder setOriginalSize(long originalSize) {
            this.originalSize = originalSize;
            return this;
        }

        public Builder setCompressedSize(long compressedSize) {
            this.compressedSize = compressedSize;
            return this;
        }

        public Builder setWasCompressed(boolean wasCompressed) {
            this.wasCompressed = wasCompressed;
            return this;
        }

        public CompressResult build() {
            return new CompressResult(this);
        }
    }

    public static Builder builder() {
        return new Builder();
    }
}