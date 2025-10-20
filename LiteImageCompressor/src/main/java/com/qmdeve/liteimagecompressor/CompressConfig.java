package com.qmdeve.liteimagecompressor;

import android.graphics.Bitmap;

public class CompressConfig {
    private int maxSize;
    private int quality;
    private Bitmap.CompressFormat format;

    private CompressConfig(Builder builder) {
        this.maxSize = builder.maxSize;
        this.quality = builder.quality;
        this.format = builder.format;
    }

    public int getMaxSize() {
        return maxSize;
    }

    public int getQuality() {
        return quality;
    }

    public Bitmap.CompressFormat getFormat() {
        return format;
    }

    public static class Builder {
        private int maxSize = 1024;
        private int quality = 80;
        private Bitmap.CompressFormat format = Bitmap.CompressFormat.JPEG;

        public Builder setMaxSize(int maxSizeKB) {
            this.maxSize = maxSizeKB;
            return this;
        }

        public Builder setQuality(int quality) {
            if (quality < 0 || quality > 100) {
                throw new IllegalArgumentException("Quality must be between 0 and 100");
            }
            this.quality = quality;
            return this;
        }

        public Builder setFormat(Bitmap.CompressFormat format) {
            this.format = format;
            return this;
        }

        public CompressConfig build() {
            return new CompressConfig(this);
        }
    }

    public static Builder builder() {
        return new Builder();
    }
}