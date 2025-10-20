
<div align="center">
  A lightweight and easy-to-use Android image compression library designed to compress images while maintaining image quality and provide flexible configuration options.

<br>
<br>
<img src="https://img.shields.io/badge/License-Apache%202.0-blue.svg" alt="Apache"/>
<img src="https://img.shields.io/badge/Java-11-orange?style=for-the-badge&logo=java" alt="Java 11"/>
<img src="https://img.shields.io/badge/Android-7.0%2B-brightgreen.svg" alt="Android 7"/>
<img src="https://jitpack.io/v/QmDeve/LiteImageCompressor.svg" alt="Jitpack 7"/>

<br>
<br>
  
  [简体中文](https://github.com/QmDeve/LiteImageCompressor/blob/master/README_zh.md)
  
  </div>

---

## Characteristic

- **Smart Threshold Compression**: Only compress images that exceed the specified size threshold
- **Quality Preservation**: Maintain image quality while effectively reducing file size
- **Dual compression mode**: supports synchronous and asynchronous operations
- **Chain call**: Smooth API design, easy to configure
- **Multiple input sources**: Supports file paths, File objects, Bitmaps and byte arrays
- **Detailed Results**: Provides complete compression statistics

---

## Screenshot
<div align="center">
<img src="https://raw.githubusercontent.com/QmDeve/LiteImageCompressor/refs/heads/master/img/screenshot.png" alt="Screenshot"/>
</div>

---

## Quick integration

Add in settings.gradle in the root directory of the project:

```gradle
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
       mavenCentral()
       maven { url 'https://jitpack.io' }
  }
}
```

<br>

### 2. Add dependencies:
<br>
Add in the build.gradle of the module:

```gradle
dependencies {
   implementation 'com.github.QmDeve:LiteImageCompressor:v1.0.1'
}
```

---

## Quick to use
### Synchronous

```java
CompressResult result = LiteImageCompressor.builder()
        .setMaxSize(500) // Only compress the image if it is larger than 500KB
        .setQuality(80)  // 80% quality
        .build()
        .compressSync("image.png");

if (result.isSuccess()) {
    Bitmap imageBitmap = result.getCompressedBitmap();
   // Process the compressed image
}
```

### Asynchronous
```java
LiteImageCompressor.builder()
    .setMaxSize(1000)
    .setQuality(85)
    .setCallback(new CompressCallback() {
        @Override
        public void onStart() {
            // Compression starts
        }

        @Override
        public void onSuccess(CompressResult result) {
            Bitmap imageBitmap = result.getCompressedBitmap();
            // Process the compressed image
        }

        @Override
        public void onError(String errorMessage) {
            // Handle errors
        }
    })
    .build()
    .compressAsync("image.png");
```

### Multiple input source support
```java
LiteImageCompressor compressor = ImageCompressor.builder()
        .setMaxSize(500)
        .setQuality(80)
        .build();

// From file path
// compressor.compressAsync("image.png");

// From File object
// File imageFile = new File("image.png");
// compressor.compressAsync(imageFile);

// from Bitmap
// Bitmap imageBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.image);
// compressor.compressAsync(imageBitmap);
```

### Advanced configuration
```java
CompressConfig config = CompressConfig.builder()
        .setMaxSize(2000)        // 2MB threshold
        .setQuality(90)          // 90% quality
        .setFormat(Bitmap.CompressFormat.JPEG) // Output format
        .build();

LiteImageCompressor.builder()
        .setConfig(config)
        .setCallback(new CompressCallback() {
            @Override
            public void onStart() { }
            
            @Override
            public void onSuccess(CompressResult result) {
                // Process the results
            }
            
            @Override
            public void onError(String errorMessage) { }
        })
        .build()
        .compressAsync("image.png");
```

### Note
1. **Set an appropriate threshold**: Choose an appropriate compression threshold based on application requirements
2. **Mass Balance**:`80-90%`The quality usually provides a good balance between size and quality
3. **Memory Management**: Recycle in time when no longer needed`bitmap`
4. **Error Handling**: Implement appropriate error handling in callbacks
5. **Please refer to Demo for detailed usage**

## Star History

[![Star History Chart](https://api.star-history.com/svg?repos=QmDeve/LiteImageCompressor&type=date&legend=bottom-right)](https://www.star-history.com/#QmDeve/LiteImageCompressor&type=date&legend=bottom-right)
