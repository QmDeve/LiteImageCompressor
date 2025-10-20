
<div align="center">
  <img src="https://img.shields.io/badge/License-Apache%202.0-blue.svg" alt="Apache"/>
  <img src="https://img.shields.io/badge/Java-21-orange?style=for-the-badge&logo=java" alt="Java 21"/>
  <img src="https://img.shields.io/badge/Android-7.0%2B-brightgreen.svg" alt="Android 7"/>
  <img src="https://jitpack.io/v/QmDeve/LiteImageCompressor.svg" alt="Jitpack 7"/>

  <br>
  轻量级且易于使用的 Android 图片压缩库，旨在压缩图片的同时保持画质，并提供灵活的配置选项。
  
</div>

---

## 特性

- **智能阈值压缩**: 仅压缩超过指定大小阈值的图片
- **画质保持**: 在有效减小文件大小的同时保持图片质量
- **双压缩模式**: 支持同步和异步操作
- **链式调用**: 流畅的 API 设计，易于配置
- **多输入源**: 支持文件路径、File 对象、Bitmap 和字节数组
- **详细结果**: 提供完整的压缩统计信息

---

## 截图
<div align="center">
  <img src="https://raw.githubusercontent.com/QmDeve/LiteImageCompressor/refs/heads/master/img/screenshot.png" alt="Screenshot"/>
</div>

---

## 快速集成

在项目根目录的 settings.gradle 中添加：

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

### 2. 添加依赖：
<br>
在模块的 build.gradle 中添加：

```gradle
dependencies {
   implementation 'com.github.QmDeve:LiteImageCompressor:v1.0.0'
}
```

---

## 快速使用
### 同步调用

```java
// 同步调用
CompressResult result = LiteImageCompressor.builder()
        .setMaxSize(500) // 仅当图片大于 500KB 时压缩
        .setQuality(80)  // 80% 质量
        .build()
        .compressSync("image.png");

if (result.isSuccess()) {
    Bitmap imageBitmap = result.getCompressedBitmap();
   // 处理压缩后的图片
}
```

### 异步调用
```java
LiteImageCompressor.builder()
    .setMaxSize(1000)
    .setQuality(85)
    .setCallback(new CompressCallback() {
        @Override
        public void onStart() {
            // 压缩开始
        }

        @Override
        public void onSuccess(CompressResult result) {
            Bitmap imageBitmap = result.getCompressedBitmap();
            // 处理压缩后的图片
        }

        @Override
        public void onError(String errorMessage) {
            // 处理错误
        }
    })
    .build()
    .compressAsync("image.png");
```

### 多输入源支持
```java
LiteImageCompressor compressor = ImageCompressor.builder()
        .setMaxSize(500)
        .setQuality(80)
        .build();

// 从文件路径
// compressor.compressAsync("image.png");

// 从 File 对象
// File imageFile = new File("image.png");
// compressor.compressAsync(imageFile);

// 从 Bitmap
// Bitmap imageBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.image);
// compressor.compressAsync(imageBitmap);
```

### 高级配置
```java
CompressConfig config = CompressConfig.builder()
        .setMaxSize(2000)        // 2MB 阈值
        .setQuality(90)          // 90% 质量
        .setFormat(Bitmap.CompressFormat.JPEG) // 输出格式
        .build();

LiteImageCompressor.builder()
        .setConfig(config)
        .setCallback(new CompressCallback() {
            @Override
            public void onStart() { }
            
            @Override
            public void onSuccess(CompressResult result) {
                // 处理结果
            }
            
            @Override
            public void onError(String errorMessage) { }
        })
        .build()
        .compressAsync("image.png");
```

### 注意思想
1. **设置合适的阈值**: 根据应用需求选择合适的压缩阈值
2. **质量平衡**: `80-90%` 的质量通常能在大小和质量之间提供良好平衡
3. **内存管理**: 不再需要时及时回收 `bitmap`
4. **错误处理**: 在回调中实现适当的错误处理
5. **详细用法请参考Demo**
