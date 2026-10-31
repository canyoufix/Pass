// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    alias(libs.plugins.kotlin.jvm) apply false
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.compose) apply false
    alias(libs.plugins.android.library) apply false
    alias(libs.plugins.kotlin.serialization) apply false
    alias(libs.plugins.ksp) apply false
}

val ktfmt =
    configurations.create("ktfmt") {
        isCanBeConsumed = false
        attributes {
            attribute(
                org.gradle.api.attributes.Usage.USAGE_ATTRIBUTE,
                objects.named(org.gradle.api.attributes.Usage.JAVA_RUNTIME),
            )
            attribute(
                org.gradle.api.attributes.Category.CATEGORY_ATTRIBUTE,
                objects.named(org.gradle.api.attributes.Category.LIBRARY),
            )
            attribute(
                org.gradle.api.attributes.LibraryElements.LIBRARY_ELEMENTS_ATTRIBUTE,
                objects.named(org.gradle.api.attributes.LibraryElements.JAR),
            )
            attribute(
                org.gradle.api.attributes.Bundling.BUNDLING_ATTRIBUTE,
                objects.named(org.gradle.api.attributes.Bundling.SHADOWED),
            )
        }
    }

dependencies { add(ktfmt.name, libs.ktfmt) }

val kotlinSources =
    fileTree(rootDir) {
        include("**/*.kt", "**/*.kts")
        exclude("**/build/**", ".gradle/**", ".kotlin/**", ".idea/**", ".git/**")
    }

tasks.register<JavaExec>("formatKotlin") {
    group = "formatting"
    description = "Format Kotlin sources and Gradle scripts with the Kotlin style"
    classpath = ktfmt
    mainClass.set("org.jetbrains.kotlinx.ktfmt.cli.Main")
    args("--kotlinlang-style")
    args(kotlinSources.files.sortedBy { it.path }.map { it.relativeTo(rootDir).path })
    workingDir = rootDir
}

tasks.register<JavaExec>("checkKotlinFormat") {
    group = "verification"
    description = "Check Kotlin formatting without changing files"
    classpath = ktfmt
    mainClass.set("org.jetbrains.kotlinx.ktfmt.cli.Main")
    args("--kotlinlang-style", "--dry-run", "--set-exit-if-changed")
    args(kotlinSources.files.sortedBy { it.path }.map { it.relativeTo(rootDir).path })
    workingDir = rootDir
}
