plugins {
    java
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
    implementation("com.sxtanna.database:Kuery:+")
}

configure<JavaPluginConvention> {
    sourceCompatibility = JavaVersion.VERSION_1_8
}