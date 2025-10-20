package com.qmdeve.liteimagecompressor.callback;

import com.qmdeve.liteimagecompressor.CompressResult;

public interface CompressCallback {
    void onStart();
    void onSuccess(CompressResult result);
    void onError(String errorMessage);
}