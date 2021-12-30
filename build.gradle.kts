import org.gradle.api.artifacts.ComponentMetadataContext
import org.gradle.api.artifacts.ComponentMetadataRule

plugins {
    kotlin("multiplatform") version "1.6.10"
    id("com.android.library")
    `maven-publish`
}

group = "br.com.devsrsouza.test"
version = "1.0-SNAPSHOT"

// cat ~/.m2/repository/br/com/devsrsouza/test/dependency-android-debug/1.0-SNAPSHOT/dependency-android-debug-1.0-SNAPSHOT.pom
// cat ~/.m2/repository/br/com/devsrsouza/test/dependency-android-debug/1.0-SNAPSHOT/dependency-android-debug-1.0-SNAPSHOT.module

repositories {
    google()
    mavenCentral()
    maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
}

kotlin {
    android {
        publishAllLibraryVariants()
    }
    jvm("desktop") {
        compilations.all {
            kotlinOptions.jvmTarget = "11"
        }
    }
    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation("org.jetbrains.compose.runtime:runtime:1.0.1-rc2")
            }
        }
        val commonTest by getting
        val androidMain by getting
        val androidTest by getting
        val desktopMain by getting
        val desktopTest by getting
    }
}

android {
    compileSdkVersion(31)
    sourceSets["main"].manifest.srcFile("src/androidMain/AndroidManifest.xml")
    defaultConfig {
        minSdkVersion(24)
        targetSdkVersion(31)
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
}


// https://github.com/JetBrains/compose-jb/blob/master/gradle-plugins/compose/src/main/kotlin/org/jetbrains/compose/ComposePlugin.kt#L133
class RedirectAndroidVariants : ComponentMetadataRule {
    override fun execute(context: ComponentMetadataContext) = with(context.details) {
        if (id.group.startsWith("org.jetbrains.compose")) {
            val group = id.group.replaceFirst("org.jetbrains.compose", "androidx.compose")
            val newReference = "$group:${id.module.name}:$androidxVersion"
            println(newReference)
            listOf(
                "debugApiElements-published",
                "debugRuntimeElements-published",
                "releaseApiElements-published",
                "releaseRuntimeElements-published"
            ).forEach { variantNameToAlter ->
                withVariant(variantNameToAlter) {
                    withDependencies {
                        removeAll { true } //there are references to org.jetbrains artifacts now
                        add(newReference)
                    }
                }
            }
        }
    }

    companion object {
        val androidxVersion: String = "1.1.0-beta04"
    }
}

listOf(
    RedirectAndroidVariants::class.java,
).forEach(dependencies.components::all)