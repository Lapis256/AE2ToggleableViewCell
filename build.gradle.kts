import java.text.SimpleDateFormat
import java.util.*

plugins {
    id("java")
    id("java-library")
    id("idea")

    id("localRuntime")

    alias(libs.plugins.neoforge)
}

val modId = Constants.Mod.id
val minecraftVersion: String = libs.versions.minecraft.get()
val jdkVersion = 21


base {
    archivesName = "${project.name}-$minecraftVersion"
    version = Constants.Mod.version
    group = Constants.Mod.group
}

sourceSets {
    main {
        resources {
            srcDir("src/generated/resources")
        }
    }
}

neoForge {
    version = libs.versions.neoforge.get()

    file("src/main/resources/META-INF/accesstransformer.cfg").takeIf(File::exists)?.let {
        println("Adding access transformer: $it")
        setAccessTransformers(it)
    }

    parchment {
        mappingsVersion = libs.versions.parchmentmc.get()
        minecraftVersion = extractVersionSegments(libs.versions.minecraft, 2)
    }

    runs {
        create("client") {
            client()
            systemProperty("neoforge.enabledGameTestNamespaces", modId)
            jvmArgument("-Dmixin.debug.export=true")
        }

        create("server") {
            server()
            programArgument("--nogui")
            systemProperty("neoforge.enabledGameTestNamespaces", modId)
            jvmArgument("-Dmixin.debug.export=true")
        }

        create("gameTestServer") {
            type = "gameTestServer"
            systemProperty("neoforge.enabledGameTestNamespaces", modId)
        }

        create("data") {
            data()
            programArguments.addAll(
                "--mod",
                modId,
                "--all",
                "--output",
                file("src/generated/resources/").absolutePath,
                "--existing",
                file("src/main/resources/").absolutePath
            )
        }

        configureEach {
            systemProperty("forge.logging.markers", "REGISTRIES")

            logLevel = org.slf4j.event.Level.DEBUG
        }
    }

    mods {
        create(modId) {
            sourceSet(sourceSets["main"])
        }
    }
}

repositories {
    mavenCentral()
    maven {
        name = "JEI / AE2"
        url = uri("https://modmaven.dev/")
    }
}

dependencies {
    implementation(libs.ae2)
    localRuntime(libs.jei)
}

val modDependencies = buildDeps(
    ModDep("neoforge", extractVersionSegments(libs.versions.neoforge, 2)),
    ModDep("minecraft", minecraftVersion),
    ModDep("ae2", extractVersionSegments(libs.versions.ae2)),
)

tasks {
    withType<JavaCompile> {
        options.encoding = "UTF-8"
        options.release = jdkVersion
    }

    java {
        toolchain {
            languageVersion = JavaLanguageVersion.of(jdkVersion)
        }
        JavaVersion.toVersion(jdkVersion).let {
            sourceCompatibility = it
            targetCompatibility = it
        }
    }

    named<Wrapper>("wrapper").configure {
        distributionType = Wrapper.DistributionType.BIN
    }

    processResources {
        val prop: Map<String, String> = mapOf(
            "version" to Constants.Mod.version,
            "group" to Constants.Mod.group,
            "minecraft_version" to minecraftVersion,
            "mod_loader" to "javafml",
            "mod_loader_version_range" to "[2,)",
            "mod_name" to Constants.Mod.name,
            "mod_author" to Constants.Mod.author,
            "mod_id" to Constants.Mod.id,
            "license" to Constants.Mod.license,
            "description" to Constants.Mod.description,
            "display_url" to Constants.Mod.repositoryUrl,
            "display_test" to DisplayTest.IGNORE_SERVER_VERSION.toString(),
            "issue_tracker_url" to Constants.Mod.issueTrackerUrl,

            "dependencies" to modDependencies
        )

        filesMatching(listOf("pack.mcmeta", "META-INF/neoforge.mods.toml", "*.mixins.json")) {
            expand(prop)
        }
        inputs.properties(prop)
    }

    jar {
        from(rootProject.file("LICENSE")) {
            rename { "LICENSE_${Constants.Mod.id}" }
        }

        manifest {
            attributes(
                "Specification-Title" to Constants.Mod.name,
                "Specification-Vendor" to Constants.Mod.author,
                "Specification-Version" to version,
                "Implementation-Title" to project.name,
                "Implementation-Version" to version,
                "Implementation-Vendor" to Constants.Mod.author,
                "Implementation-Timestamp" to SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ").format(Date()),
                "Timestamp" to System.currentTimeMillis(),
                "Built-On-Java" to "${System.getProperty("java.vm.version")} (${System.getProperty("java.vm.vendor")})",
                "Built-On-Minecraft" to minecraftVersion,
            )
        }
    }
}

idea {
    module {
        isDownloadJavadoc = true
        isDownloadSources = true
    }
}
