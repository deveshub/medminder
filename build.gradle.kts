buildscript {
    repositories {
        google()
        mavenCentral()
    }
    dependencies {
        classpath("com.android.tools.build:gradle:8.1.0")
        classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:1.8.21")
        classpath("com.google.dagger:hilt-android-gradle-plugin:2.47")
    }
}

tasks.register("clean", Delete::class) {
    delete(rootProject.buildDir)
}

// Add root-level task for migration tests
tasks.register("testMigrations") {
    dependsOn(":app:testMigrations")
    description = "Run database migration tests (delegates to :app:testMigrations)"
    group = "verification"
} 