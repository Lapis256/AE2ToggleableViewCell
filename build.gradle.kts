import net.neoforged.moddevgradle.internal.RunGameTask
import org.apache.tools.ant.filters.ReplaceTokens
import org.gradle.kotlin.dsl.publishMods
import java.text.SimpleDateFormat
import java.util.*

plugins {
    id("java")
    id("java-library")
    id("idea")

    id("localRuntime")

    alias(libs.plugins.moddev)
    alias(libs.plugins.modPublishPlugin)
}

val modId = Constants.Mod.ID
val mcVersion: String = libs.versions.minecraft.get()

val jdkVersion = Constants.Dev.JDK_VERSION
val jvmVendor = Constants.Dev.JVM_VENDOR


base {
    archivesName = project.name
    version = Constants.Mod.VERSION
    group = Constants.Mod.GROUP
}

val modDependencies = buildDeps(
    ModDep("neoforge", libs.versions.neoforge.gte()),
    ModDep("minecraft", mcVersion.gte()),
    ModDep("ae2", libs.versions.ae2.gte()),
)

val generateModMetadata by tasks.registering(ProcessResources::class) {
    val replaceProperties: Map<String, String> = mapOf(
        "version" to Constants.Mod.VERSION,
        "group" to Constants.Mod.GROUP,
        "minecraft_version" to mcVersion,
        "mod_loader" to "javafml",
        "mod_loader_version_range" to "[2,)",
        "mod_name" to Constants.Mod.NAME,
        "mod_author" to Constants.Mod.AUTHOR,
        "mod_id" to Constants.Mod.ID,
        "logo_file" to "assets/ae2_toggleable_view_cell/textures/item/toggleable_view_cell_enabled.png",
        "logo_blur" to "false",
        "license" to Constants.Mod.LICENSE,
        "description" to Constants.Mod.DESCRIPTION,
        "display_url" to Constants.Mod.REPOSITORY_URL,
        "display_test" to DisplayTest.IGNORE_SERVER_VERSION.toString(),
        "issue_tracker_url" to Constants.Mod.ISSUE_TRACKER_URL,

        "dependencies" to modDependencies
    )

    inputs.properties(replaceProperties)
    filter<ReplaceTokens>("beginToken" to "\${", "endToken" to "}", "tokens" to replaceProperties)
    from(rootProject.file("src/templates"))
    into("build/generated/sources/$modId")
}

neoForge {
    enable {
        version = libs.versions.neoforge.get()
    }

    validateAccessTransformers = true

    accessTransformers {
        val atFile = rootProject.file("src/core/resources/META-INF/accesstransformer.cfg").takeIf(File::exists) ?: return@accessTransformers
        from(atFile)
        publish(atFile)
    }

    runs {
        register("client") {
            client()
            systemProperty("neoforge.enabledGameTestNamespaces", modId)
            jvmArgument("-Dmixin.debug.export=true")
        }

        register("server") {
            server()
            programArgument("--nogui")
            systemProperty("neoforge.enabledGameTestNamespaces", modId)
            jvmArgument("-Dmixin.debug.export=true")
        }

        register("gameTestServer") {
            type = "gameTestServer"
            systemProperty("neoforge.enabledGameTestNamespaces", modId)
        }

        register("data") {
            clientData()
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

    ideSyncTask(generateModMetadata)
}

repositories {
    mavenCentral()
    maven {
        name = "JEI"
        url = uri("https://modmaven.dev/")
    }
}

sourceSets {
    main {
        resources {
            srcDir("src/generated/resources")
            srcDir(generateModMetadata.get().outputs.files)
            exclude("**/.cache")
        }
    }
}

dependencies {
    implementation(libs.ae2)
    localRuntime(libs.jei)
}

java {
    withSourcesJar()
    toolchain {
        languageVersion = JavaLanguageVersion.of(jdkVersion)
        vendor = jvmVendor
    }
    JavaVersion.toVersion(jdkVersion).let {
        sourceCompatibility = it
        targetCompatibility = it
    }
}

tasks {
    withType<JavaCompile> {
        options.encoding = "UTF-8"
        options.release = jdkVersion
    }

    named<Wrapper>("wrapper").configure {
        distributionType = Wrapper.DistributionType.BIN
    }

    processResources {
        dependsOn(generateModMetadata)
    }

    jar {
        manifest {
            attributes(
                "Specification-Title" to Constants.Mod.NAME,
                "Specification-Vendor" to Constants.Mod.AUTHOR,
                "Specification-Version" to version,
                "Implementation-Title" to project.name,
                "Implementation-Version" to version,
                "Implementation-Vendor" to Constants.Mod.AUTHOR,
                "Implementation-Timestamp" to SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ").format(Date()),
                "Timestamp" to System.currentTimeMillis(),
                "Built-On-Java" to "${System.getProperty("java.vm.version")} (${System.getProperty("java.vm.vendor")})",
                "Built-On-Minecraft" to mcVersion,
            )
        }
    }

    withType<RunGameTask>().configureEach {
        javaLauncher.set(project.javaToolchains.launcherFor {
            languageVersion.set(JavaLanguageVersion.of(jdkVersion))
            vendor.set(jvmVendor)
        })
        standardInput = System.`in`
    }

    withType<Jar>().configureEach {
        from(rootProject.file("LICENSE")) {
            rename { "LICENSE_${Constants.Mod.ID}" }
        }

        destinationDirectory.set(rootProject.layout.buildDirectory.dir("libs"))
    }

    run {
        val tag = "v${Constants.Mod.VERSION}"

        val releaseTag by registering(Exec::class) {
            group = "release"
            description = "Create an annotated git tag"

            doFirst {
                commandLine("git", "tag", "-s", "-a", tag, "-m", "Release $tag")
            }
        }

        register<Exec>("pushReleaseTag") {
            group = "release"
            description = "Push the release tag to origin"
            dependsOn(releaseTag)

            doFirst {
                commandLine("git", "push", "origin", tag)
            }
        }
    }
}

idea {
    module {
        isDownloadJavadoc = true
        isDownloadSources = true
    }
}

publishMods {
    type = ALPHA
    modLoaders.add("neoforge")

    curseforge {
        requires("applied-energistics-2")

        minecraftVersions.add("26.1.2")
        clientRequired = true
        serverRequired = true

        projectId = Constants.Publisher.CURSEFORGE_PROJECT_ID
        accessToken = System.getenv("CURSEFORGE_TOKEN")
        javaVersions.add(JavaVersion.toVersion(jdkVersion))
        changelogType = "markdown"
    }

    modrinth {
        requires("ae2")

        minecraftVersions.add("26.1.2")

        projectId = Constants.Publisher.MODRINTH_PROJECT_ID
        accessToken = System.getenv("MODRINTH_TOKEN")
    }

    fun pickSingle(dir: File, include: (String) -> Boolean): File {
        val list = dir.listFiles()?.filter { it.isFile && include(it.name) }.orEmpty()
        require(list.size == 1) { "Expected exactly 1 match, but got ${list.size}: ${list.map { it.name }}" }
        return list.single()
    }

    val releaseFilesDir = providers.gradleProperty("releaseFilesDir").orElse("dist")

    val releaseDirFileProvider = releaseFilesDir.map { layout.projectDirectory.dir(it).asFile }

    val mainJarProvider = releaseDirFileProvider.map { dir ->
        pickSingle(dir) { name ->
            name.endsWith(".jar") &&
                !name.endsWith("-sources.jar") &&
                !name.endsWith("-javadoc.jar")
        }
    }

    val otherJarsProvider = releaseDirFileProvider.map { dir ->
        pickSingle(dir) { name ->
            name.endsWith("-sources.jar")
        }
    }

    file = mainJarProvider
    additionalFiles.from(otherJarsProvider)
    dryRun = project.hasProperty("modPublishDryRun")
    changelog = System.getenv("CHANGELOG") ?: "No changelog provided"
    displayName = "[$mcVersion] v${Constants.Mod.VERSION}"
}
