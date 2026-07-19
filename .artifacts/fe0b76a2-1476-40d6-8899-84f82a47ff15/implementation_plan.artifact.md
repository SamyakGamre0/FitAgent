# Implementation Plan - Fix Gradle Warnings and Errors

The goal is to resolve the `kapt` unresolved reference error and address various warnings related to dependency management and versions in `app/build.gradle.kts`.

## User Review Required

> [!IMPORTANT]
> I will be migrating some dependencies to use the Version Catalog (`libs.versions.toml`) to align with modern Android development practices and resolve IDE warnings.
> I will also add the `kotlin-android` and `kotlin-kapt` plugins to the project.

## Proposed Changes

### 1. Build Configuration & Dependency Management

#### [MODIFY] [libs.versions.toml](file:///D:/Samyak/Projects/%20FitAgent/gradle/libs.versions.toml)
- Add versions and library definitions for:
    - Kotlin (`1.9.22` or latest stable)
    - Room (`2.6.1` or updated to `2.8.4`)
    - Lifecycle (`2.11.0`)
    - Coroutines (`1.11.0`)
    - RecyclerView and ConstraintLayout
- Add plugin definitions for `kotlin-android` and `kotlin-kapt`.

#### [MODIFY] [build.gradle.kts (root)](file:///D:/Samyak/Projects/%20FitAgent/build.gradle.kts)
- Add the Kotlin and Kapt plugins to the top-level `plugins` block (applied `false`).

#### [MODIFY] [app/build.gradle.kts](file:///D:/Samyak/Projects/%20FitAgent/app/build.gradle.kts)
- Apply `kotlin-android` and `kotlin-kapt` plugins.
- Replace all hardcoded dependency strings with references from the version catalog (`libs.*`).
- Update versions to the latest stable ones as identified in the research phase.

## Verification Plan

### Automated Tests
- Run `./gradlew build` to ensure the project compiles successfully.
- Run `./gradlew :app:assembleDebug` to verify the APK can be built.

### Manual Verification
- Sync the project in Android Studio and verify that the `kapt` error and version warnings are gone.
