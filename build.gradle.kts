plugins {
    id("java")
}

group = "io.github.pyke"
version = "1.0-SNAPSHOT"

java {
    toolchain.languageVersion.set(JavaLanguageVersion.of(21))
}

repositories {
    mavenCentral()
    maven {
        name = "papermc"
        url = uri("https://repo.papermc.io/repository/maven-public/")
    }
}

dependencies {
    testImplementation(platform("org.junit:junit-bom:5.10.0"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    compileOnly("io.papermc.paper:paper-api:1.21.4-R0.1-SNAPSHOT")
}

tasks.test {
    useJUnitPlatform()
}

val copyJarToB by tasks.registering(Copy::class) {
    dependsOn(tasks.named("build")) // 빌드 후 실행
    from(layout.buildDirectory.dir("libs")) // 기본 JAR 출력 경로
    include("*.jar")
    into("C:/Users/dog06/Desktop/1.21.4 Server/plugins") // 원하는 복사 대상 경로
}

tasks.named("build") {
    finalizedBy(copyJarToB)
}