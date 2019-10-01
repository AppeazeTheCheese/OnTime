plugins {
    java
    id("com.github.johnrengelman.shadow") version "5.1.0"
}

group = "me.camdenorrb.timekeeper"
version = "1.0.0"

repositories {

    mavenCentral()

    maven("https://oss.sonatype.org/content/repositories/snapshots") {
        name = "BungeeCord"
    }

    maven("https://jitpack.io") {
        name = "Jitpack"
    }
    
}

dependencies {
    compileOnly("net.md-5:bungeecord-api:1.14-SNAPSHOT")
    implementation("com.github.camdenorrb:JCommons:1.0.3")
    implementation("com.zaxxer:HikariCP:3.4.1")
}

configure<JavaPluginConvention> {
    sourceCompatibility = JavaVersion.VERSION_1_8
}