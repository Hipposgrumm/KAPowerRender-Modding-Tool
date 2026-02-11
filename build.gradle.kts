plugins {
    java
    application
    id("org.javamodularity.moduleplugin") version "1.8.12"
    id("org.openjfx.javafxplugin") version "0.0.13"
    id("org.beryx.jlink") version "3.1.3"
}

group = "dev.hipposgrumm"
version = "0.3.0p1"

repositories {
    mavenCentral()
}

val junitVersion = "5.10.2"

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(21)
    }
}

tasks.withType<JavaCompile> {
    options.encoding = "UTF-8"
}

application {
    mainModule.set("dev.hipposgrumm.kamapreader")
    mainClass.set("dev.hipposgrumm.kamapreader.Main")
}

javafx {
    version = "17.0.6"
    modules = listOf("javafx.controls", "javafx.fxml")
}

dependencies {
    implementation("com.googlecode.soundlibs:vorbisspi:1.0.3.3")

    testImplementation("org.junit.jupiter:junit-jupiter-api:${junitVersion}")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:${junitVersion}")
}

tasks.withType<Test> {
    useJUnitPlatform()
}

jlink {
    imageName.set("KAPowerRenderModdingTool")
    options.set(listOf("--strip-debug", "--compress", "2", "--no-header-files", "--no-man-pages"))
    launcher {
        name = "app"
        //noConsole = true
    }
}

tasks.withType<org.beryx.jlink.JlinkTask> {
    doLast {
        copy {
            from("/buildextras") {
                include("run.bat")
            }
            into("${layout.buildDirectory.get()}/KAPowerRenderModdingTool")
        }
        copy {
            from("/KAMapViewer") {
                include("KAMapViewer.jar")
            }
            into("${layout.buildDirectory.get()}/KAPowerRenderModdingTool")
        }
    }
}
