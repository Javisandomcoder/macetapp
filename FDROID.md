# F-Droid Build Configuration

This file contains specific configurations for F-Droid builds to ensure reproducibility.

## Usage
Include this configuration in your build.gradle.kts for F-Droid builds:

```kotlin
apply(from = "../fdroid-reproducible.gradle")
```

## Notes
- Sets reproducible build settings
- Filters NDK ABIs for consistent builds
- Ensures Java 8 compatibility across all builds