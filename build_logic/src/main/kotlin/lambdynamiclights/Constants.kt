package lambdynamiclights

import org.gradle.accessors.dm.LibrariesForLibs

object Constants {
	const val NAME = "lambdynamiclights"
	const val NAMESPACE = "lambdynlights"
	const val PRETTY_NAME = "LambDynamicLights"

	const val DESCRIPTION = "The most feature-complete dynamic lighting mod."
	const val API_DESCRIPTION = "Library to provide dynamic lighting to Minecraft through LambDynamicLights."
	const val RUNTIME_DESCRIPTION = "The runtime of the most feature-complete dynamic lighting mod."

	@JvmField
	val AUTHORS = listOf("LambdAurora")

	@JvmField
	val CONTRIBUTORS = listOf("Akarys")

	const val PROJECT_LINK = "https://lambdaurora.dev/projects/lambdynamiclights"
	const val SOURCES_LINK = "https://github.com/LambdAurora/LambDynamicLights"
	const val LICENSE = "Lambda License"

	private var minecraftVersion: String? = null

	fun finalizeInit(libs: LibrariesForLibs) {
		this.minecraftVersion = libs.versions.minecraft.get()
	}
}
