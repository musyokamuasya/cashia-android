// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.kotlin.compose) apply false
    alias(libs.plugins.android.library) apply false
    id("com.google.devtools.ksp") version "2.2.21-2.0.4" apply false
    id("com.google.dagger.hilt.android") version "2.57.2" apply false
    kotlin("plugin.serialization") version "1.9.0" apply false
}
tasks.register("clean", Delete::class) {
    delete(rootProject.buildDir)
}