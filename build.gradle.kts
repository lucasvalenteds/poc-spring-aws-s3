plugins {
    java
}

repositories {
    mavenCentral()
}

dependencies {
}

configure<JavaPluginExtension> {
    sourceCompatibility = JavaVersion.VERSION_11
    targetCompatibility = JavaVersion.VERSION_11
}
