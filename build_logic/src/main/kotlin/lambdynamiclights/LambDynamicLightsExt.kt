package lambdynamiclights

import dev.lambdaurora.mcdev.api.ModUtils
import dev.lambdaurora.mcdev.api.VersionType
import org.gradle.accessors.dm.LibrariesForLibs
import org.gradle.api.Project

open class LambDynamicLightsExt(private val project: Project, private val libs: LibrariesForLibs) {
	fun version(): String {
		return this.project.property("base_version").toString()
	}

	fun mcVersion(): String {
		return this.libs.versions.minecraft.get();
	}

	fun extraCompatibleMcVersions(): List<String> {
		return this.project.property("compatible_mc_versions").toString().split(",").map { it.trim() }
	}

	fun compatibleMcVersions(): List<String> {
		return listOf(this.mcVersion()) + this.extraCompatibleMcVersions()
	}

	fun versionType(): VersionType {
		return ModUtils.getVersionType(this.version(), this.mcVersion())
	}
}
