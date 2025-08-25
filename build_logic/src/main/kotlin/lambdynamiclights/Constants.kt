package lambdynamiclights

import dev.lambdaurora.mcdev.api.ModUtils
import dev.lambdaurora.mcdev.api.VersionType
import org.gradle.accessors.dm.LibrariesForLibs

object Constants {
	const val NAME = "lambdynamiclights"
	const val NAMESPACE = "lambdynlights"
	const val PRETTY_NAME = "LambDynamicLights"
	const val VERSION = "4.2.10"

	const val DESCRIPTION = "The most feature-complete dynamic lighting mod for Fabric."
	const val API_DESCRIPTION = "Library to provide dynamic lighting to Minecraft through LambDynamicLights."

	@JvmField
	val AUTHORS = listOf("LambdAurora")

	@JvmField
	val CONTRIBUTORS = listOf("Akarys")

	const val PROJECT_LINK = "https://lambdaurora.dev/projects/lambdynamiclights"
	const val SOURCES_LINK = "https://github.com/LambdAurora/LambDynamicLights"
	const val LICENSE = "Lambda License"

	@JvmField
	val COMPATIBLE_MC_VERSIONS = listOf<String>()

	private var minecraftVersion: String? = null

	fun finalizeInit(libs: LibrariesForLibs) {
		this.minecraftVersion = libs.versions.minecraft.get()
	}

	fun mcVersion(): String {
		return this.minecraftVersion!!
	}

	fun getVersionType(): VersionType {
		return ModUtils.getVersionType(this.VERSION, this.mcVersion())
	}
}
