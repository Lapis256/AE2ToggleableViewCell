import java.text.SimpleDateFormat
import java.util.*

plugins {
    id("java")
    id("java-library")
    id("idea")

    alias(libs.plugins.forge)
    alias(libs.plugins.mixin)
    alias(libs.plugins.parchmentmc)
}

val modId = Constants.Mod.id
val minecraftVersion: String = libs.versions.minecraft.get()
val forgeMajorVersion: String = extractVersionSegments(libs.versions.forge)
val jdkVersion = 17


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

minecraft {
    mappings("parchment", "${libs.versions.parchmentmc.get()}-$minecraftVersion")

    copyIdeResources = true

    file("src/main/resources/META-INF/accesstransformer.cfg").takeIf(File::exists)?.let {
        println("Adding access transformer: $it")
        accessTransformer(it)
    }

    runs {
        configureEach {
            ideaModule("${rootProject.name}.${project.name}.main")

            properties(
                mapOf(
                    "forge.logging.markers" to "REGISTRIES", "forge.logging.console.level" to "debug"
                )
            )

            jvmArgs(
                "-XX:+AllowEnhancedClassRedefinition", "-Dmixin.debug.export=true"
            )

            mods {
                create(modId) {
                    source(sourceSets["main"])
                }
            }
        }

        create("client") {
            taskName("Forge Client")

            workingDirectory(project.file("run"))

            property("forge.enabledGameTestNamespaces", modId)
        }

        create("server") {
            taskName("Forge Server")

            workingDirectory(project.file("run-server"))

            property("forge.enabledGameTestNamespaces", modId)
        }

        create("data") {
            taskName("Generate Data")

            workingDirectory(project.file("run-data"))

            args(
                "--mod", modId, "--all", "--output", file("src/generated/resources/"), "--existing", file("src/main/resources/")
            )
            jvmArgs("-Dmixin.debug.export=false")
        }
    }
}

mixin {
    add(sourceSets["main"], "${modId}.refmap.json")

    config("${modId}.mixins.json")
}

repositories {
    mavenCentral()
    maven {
        name = "Sponge / Mixin"
        url = uri("https://repo.spongepowered.org/repository/maven-public/")
    }
    maven {
        name = "JEI / AE2"
        url = uri("https://modmaven.dev/")
    }
    maven {
        name = "Curse Maven"
        url = uri("https://cursemaven.com")
    }
}

dependencies {
    minecraft(libs.minecraftForge)

    implementation(deobf(libs.ae2))
    runtimeOnly(deobf(libs.jei))

    annotationProcessor(variantOf(libs.mixin, "processor"))
    libs.mixinExtrasCommon.let {
        annotationProcessor(it)
        compileOnly(it)
    }
    libs.mixinExtrasForge.let {
        jarJar(it) {
            jarJar.ranged(this, "[${it.get().version},)")
        }
        implementation(it)
    }
}

val modDependencies = buildDeps(
    ModDep("forge", forgeMajorVersion),
    ModDep("minecraft", minecraftVersion),
    ModDep("ae2", extractVersionSegments(libs.versions.ae2)),
)

tasks {
    withType<JavaCompile> {
        options.encoding = "UTF-8"
        options.release = jdkVersion
    }

    java {
        withSourcesJar()
        toolchain {
            languageVersion = JavaLanguageVersion.of(jdkVersion)
            vendor = JvmVendorSpec.JETBRAINS
        }
        JavaVersion.toVersion(jdkVersion).let {
            sourceCompatibility = it
            targetCompatibility = it
        }
    }

    processResources {
        val prop: Map<String, String> = mapOf(
            "version" to Constants.Mod.version,
            "group" to Constants.Mod.group,
            "minecraft_version" to minecraftVersion,
            "mod_loader" to "javafml",
            "mod_loader_version_range" to "[$forgeMajorVersion,)",
            "mod_name" to Constants.Mod.name,
            "mod_author" to Constants.Mod.author,
            "mod_id" to Constants.Mod.id,
            "logo_file" to "assets/ae2_toggleable_view_cell/textures/item/toggleable_view_cell_enabled.png",
            "logo_blur" to "false",
            "license" to Constants.Mod.license,
            "description" to Constants.Mod.description,
            "display_url" to Constants.Mod.repositoryUrl,
            "display_test" to DisplayTest.IGNORE_SERVER_VERSION.toString(),
            "issue_tracker_url" to Constants.Mod.issueTrackerUrl,

            "dependencies" to modDependencies
        )

        filesMatching(listOf("pack.mcmeta", "META-INF/mods.toml", "*.mixins.json")) {
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

        finalizedBy("reobfJar")
    }

    named<Jar>("sourcesJar") {
        from(rootProject.file("LICENSE")) {
            rename { "LICENSE_${Constants.Mod.id}" }
        }
    }
}

fun deobf(dependency: Provider<MinimalExternalModuleDependency>) = fg.deobf(dependency.get())
