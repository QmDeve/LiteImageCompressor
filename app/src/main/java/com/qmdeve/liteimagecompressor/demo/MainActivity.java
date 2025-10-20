package com.qmdeve.liteimagecompressor.demo;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.qmdeve.liteimagecompressor.CompressResult;
import com.qmdeve.liteimagecompressor.LiteImageCompressor;
import com.qmdeve.liteimagecompressor.callback.CompressCallback;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.text.DecimalFormat;

public class MainActivity extends AppCompatActivity {

    private static final int REQUEST_CODE_PICK_IMAGE = 1001;
    private static final int REQUEST_CODE_PERMISSION = 1002;

    private Button btnSelectImage;
    private Button btnCompressSync;
    private Button btnCompressAsync;
    private EditText etThreshold;
    private SeekBar seekBarQuality;
    private TextView tvQuality;
    private TextView tvOriginalInfo;
    private TextView tvCompressResult;
    private ImageView ivOriginal;
    private ImageView ivCompressed;
    private ProgressBar progressBar;

    private Bitmap originalBitmap;
    private String selectedImagePath;
    private long originalFileSize;

    private final DecimalFormat sizeFormat = new DecimalFormat("#,##0.00");
    private final String[] requiredPermissions = {
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(0, systemBars.top, 0, systemBars.bottom);
            return insets;
        });

        initViews();
        setupListeners();
        checkPermissions();
    }

    private void initViews() {
        btnSelectImage = findViewById(R.id.btn_select_image);
        btnCompressSync = findViewById(R.id.btn_compress_sync);
        btnCompressAsync = findViewById(R.id.btn_compress_async);
        etThreshold = findViewById(R.id.et_threshold);
        seekBarQuality = findViewById(R.id.seekbar_quality);
        tvQuality = findViewById(R.id.tv_quality);
        tvOriginalInfo = findViewById(R.id.tv_original_info);
        tvCompressResult = findViewById(R.id.tv_compress_result);
        ivOriginal = findViewById(R.id.iv_original);
        ivCompressed = findViewById(R.id.iv_compressed);
        progressBar = findViewById(R.id.progress_bar);

        btnCompressSync.setEnabled(false);
        btnCompressAsync.setEnabled(false);
    }

    private void setupListeners() {
        btnSelectImage.setOnClickListener(v -> selectImage());
        btnCompressSync.setOnClickListener(v -> compressImageSync());
        btnCompressAsync.setOnClickListener(v -> compressImageAsync());

        seekBarQuality.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                tvQuality.setText(progress + "%");
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });
    }

    private void checkPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            boolean allPermissionsGranted = true;
            for (String permission : requiredPermissions) {
                if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
                    allPermissionsGranted = false;
                    break;
                }
            }

            if (!allPermissionsGranted) {
                ActivityCompat.requestPermissions(this, requiredPermissions, REQUEST_CODE_PERMISSION);
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CODE_PERMISSION) {
            boolean allGranted = true;
            for (int result : grantResults) {
                if (result != PackageManager.PERMISSION_GRANTED) {
                    allGranted = false;
                    break;
                }
            }

            if (!allGranted) {
                Toast.makeText(this, getString(R.string.a1), Toast.LENGTH_LONG).show();
            }
        }
    }

    private void selectImage() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        intent.setType("image/*");
        startActivityForResult(intent, REQUEST_CODE_PICK_IMAGE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_PICK_IMAGE && resultCode == RESULT_OK && data != null) {
            Uri selectedImageUri = data.getData();
            if (selectedImageUri != null) {
                loadSelectedImage(selectedImageUri);
            }
        }
    }

    private void loadSelectedImage(Uri imageUri) {
        try {
            selectedImagePath = getPathFromUri(imageUri);
            File imageFile = new File(selectedImagePath);
            originalFileSize = imageFile.length();

            InputStream inputStream = getContentResolver().openInputStream(imageUri);
            originalBitmap = BitmapFactory.decodeStream(inputStream);
            if (inputStream != null) {
                inputStream.close();
            }

            if (originalBitmap != null) {
                displayOriginalInfo();

                ivOriginal.setImageBitmap(originalBitmap);
                btnCompressSync.setEnabled(true);
                btnCompressAsync.setEnabled(true);
                tvCompressResult.setText(getString(R.string.a2));
                ivCompressed.setImageBitmap(null);
            } else {
                Toast.makeText(this, getString(R.string.a3), Toast.LENGTH_SHORT).show();
            }
        } catch (IOException e) {
            Toast.makeText(this, getString(R.string.a4) + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private String getPathFromUri(Uri contentUri) {
        String[] projection = {MediaStore.Images.Media.DATA};
        android.database.Cursor cursor = getContentResolver().query(contentUri, projection, null, null, null);
        if (cursor != null) {
            int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
            cursor.moveToFirst();
            String path = cursor.getString(column_index);
            cursor.close();
            return path;
        }
        return contentUri.getPath();
    }

    private void displayOriginalInfo() {
        if (originalBitmap != null) {
            @SuppressLint("DefaultLocale")
            String info = String.format(getString(R.string.a5),
                    originalBitmap.getWidth(),
                    originalBitmap.getHeight(),
                    sizeFormat.format(originalFileSize / 1024.0));
            tvOriginalInfo.setText(info);
        }
    }

    private long getBitmapSize(Bitmap bitmap) {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.KITKAT) {
            return bitmap.getAllocationByteCount();
        } else {
            return bitmap.getByteCount();
        }
    }

    private void compressImageSync() {
        if (originalBitmap == null) {
            Toast.makeText(this, getString(R.string.a6), Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            int threshold = Integer.parseInt(etThreshold.getText().toString());
            int quality = seekBarQuality.getProgress();

            showProgress(true);
            tvCompressResult.setText(getString(R.string.a7));
            new Thread(() -> {
                try {
                    CompressResult result = LiteImageCompressor.builder()
                            .setMaxSize(threshold)
                            .setQuality(quality)
                            .build()
                            .compressSync(selectedImagePath);

                    runOnUiThread(() -> {
                        showProgress(false);
                        handleCompressResult(result);
                    });

                } catch (Exception e) {
                    runOnUiThread(() -> {
                        showProgress(false);
                        Toast.makeText(MainActivity.this, getString(R.string.a8) + e.getMessage(), Toast.LENGTH_SHORT).show();
                        tvCompressResult.setText(getString(R.string.a8) + e.getMessage());
                    });
                }
            }).start();

        } catch (NumberFormatException e) {
            Toast.makeText(this, getString(R.string.a9), Toast.LENGTH_SHORT).show();
        }
    }

    private void compressImageAsync() {
        if (originalBitmap == null) {
            Toast.makeText(this, getString(R.string.a6), Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            int threshold = Integer.parseInt(etThreshold.getText().toString());
            int quality = seekBarQuality.getProgress();

            showProgress(true);

            LiteImageCompressor.builder()
                    .setMaxSize(threshold)
                    .setQuality(quality)
                    .setCallback(new CompressCallback() {
                        @Override
                        public void onStart() {
                            runOnUiThread(() -> {
                                tvCompressResult.setText(getString(R.string.a7));
                            });
                        }

                        @Override
                        public void onSuccess(CompressResult result) {
                            runOnUiThread(() -> {
                                showProgress(false);
                                handleCompressResult(result);
                            });
                        }

                        @Override
                        public void onError(String errorMessage) {
                            runOnUiThread(() -> {
                                showProgress(false);
                                Toast.makeText(MainActivity.this, getString(R.string.a8) + errorMessage, Toast.LENGTH_SHORT).show();
                                tvCompressResult.setText(getString(R.string.a8) + errorMessage);
                            });
                        }
                    })
                    .build()
                    .compressAsync(selectedImagePath);

        } catch (NumberFormatException e) {
            Toast.makeText(this, getString(R.string.a9), Toast.LENGTH_SHORT).show();
        }
    }

    private void handleCompressResult(CompressResult result) {
        if (result.isSuccess()) {
            Bitmap compressedBitmap = result.getCompressedBitmap();
            ivCompressed.setImageBitmap(compressedBitmap);

            StringBuilder resultText = new StringBuilder();
            resultText.append(String.format(getString(R.string.b1),
                    sizeFormat.format(result.getOriginalSize() / 1024.0)));
            resultText.append(String.format(getString(R.string.b2),
                    sizeFormat.format(result.getCompressedSize() / 1024.0)));

            tvCompressResult.setText(resultText.toString());
        } else {
            tvCompressResult.setText(getString(R.string.a8) + result.getErrorMessage());
            Toast.makeText(this, getString(R.string.a8) + result.getErrorMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void showProgress(boolean show) {
        progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
        btnCompressSync.setEnabled(!show);
        btnCompressAsync.setEnabled(!show);
        btnSelectImage.setEnabled(!show);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (originalBitmap != null && !originalBitmap.isRecycled()) {
            originalBitmap.recycle();
        }
    }
}