package lambdynamiclights

import org.gradle.accessors.dm.LibrariesForLibs

object Constants {
	const val GROUP = "dev.lambdaurora"
	const val NAME = "lambdynamiclights"
	const val VERSION = "3.1.1"
	const val JAVA_VERSION = 21

	private var minecraftVersion: String? = null

	fun finalizeInit(libs: LibrariesForLibs) {
		this.minecraftVersion = libs.versions.minecraft.get()
	}

	fun mcVersion(): String {
		return this.minecraftVersion!!
	}

	fun isMcVersionNonRelease(): Boolean {
		return this.mcVersion().matches(Regex("^\\d\\dw\\d\\d[a-z]$"))
				|| this.mcVersion().matches(Regex("\\d+\\.\\d+-(pre|rc)(\\d+)"))
	}

	fun getMcVersionString(): String {
		if (isMcVersionNonRelease()) {
			return this.mcVersion()
		}
		val version = this.mcVersion().split("\\.".toRegex())
		return version[0] + "." + version[1]
	}

	fun getVersionType(): String {
		return if (this.isMcVersionNonRelease() || "-alpha." in this.VERSION) {
			"alpha"
		} else if ("-beta." in this.VERSION) {
			"beta"
		} else {
			"release"
		}
	}
}
