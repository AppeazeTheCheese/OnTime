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
}

dependencies {
    compileOnly("net.md-5:bungeecord-api:1.14-SNAPSHOT")
    implementation("com.zaxxer:HikariCP:3.4.1")
    //implementation("com.sxtanna.database:Kuery:+")
    //implementation("com.sxtanna.database:Core:+")
    //implementation("org.jetbrains.kotlin:kotlin-stdlib-jre8:1.2.71")
    //implementation("org.jetbrains.kotlin:kotlin-reflect:1.2.71")
}

configure<JavaPluginConvention> {
    sourceCompatibility = JavaVersion.VERSION_1_8
}