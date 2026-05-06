import org.gradle.jvm.toolchain.JvmVendorSpec

object Constants {
    object Mod {
        const val ID = "ae2_toggleable_view_cell"
        const val NAME = "AE2 Toggleable View Cell"
        const val DESCRIPTION = "A mod that adds a toggleable view cell to AE2"
        const val LICENSE = "MIT"
        const val VERSION = "26.1-1.0.0"
        const val GROUP = "io.github.lapis256"
        const val AUTHOR = "Lapis256"
        const val REPOSITORY_URL = "https://github.com/Lapis256/AE2ToggleableViewCell"
        const val ISSUE_TRACKER_URL = "$REPOSITORY_URL/issues"
    }

    object Publisher {
        const val CURSEFORGE_PROJECT_ID = "1141472"
        const val MODRINTH_PROJECT_ID = "7GNdjsfs"
    }

    object Dev {
        const val JDK_VERSION = 25
        @Suppress("UnstableApiUsage")
        val JVM_VENDOR: JvmVendorSpec = JvmVendorSpec.JETBRAINS
    }
}
