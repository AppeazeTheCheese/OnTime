plugins {
    java
    id("com.github.johnrengelman.shadow") version "6.0.0"
}

group = "me.camdenorrb.ontime"
version = "1.0.2"


repositories {

    mavenCentral()

    maven("https://hub.spigotmc.org/nexus/content/repositories/snapshots/") {
        name = "Spigot"
    }

    maven("https://oss.sonatype.org/content/repositories/snapshots") {
        name = "BungeeCord"
    }

    maven("https://jitpack.io") {
        name = "Jitpack"
    }
}

dependencies {
    compileOnly("org.spigotmc:spigot-api:1.15.2-R0.1-SNAPSHOT")
    compileOnly("net.md-5:bungeecord-api:1.15-SNAPSHOT")
    implementation(files("lib/VanishNoPacket.jar"))
    implementation(files("lib/commons-io-2.8.0.jar"))
    implementation(files("lib/json_simple.jar"))
    implementation("com.zaxxer:HikariCP:3.4.5")
    compileOnly("com.google.api-client:google-api-client:1.31.1")
    compileOnly("com.google.oauth-client:google-oauth-client-jetty:1.30.6")
    compileOnly("com.google.apis:google-api-services-sheets:v4-rev581-1.25.0")
}

configure<JavaPluginConvention> {
    sourceCompatibility = JavaVersion.VERSION_1_8
}