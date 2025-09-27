package lambdynamiclights

import dev.lambdaurora.mcdev.api.ModUtils
import org.gradle.api.Project

object Utils {
	fun parseReadme(project: Project): String {
		return ModUtils.parseReadme(
			project,
			"https://raw.githubusercontent.com/LambdAurora/LambDynamicLights/1.21.8/\$2"
		)
	}

	fun fetchChangelog(project: Project): String? {
		return ModUtils.fetchChangelog(project, project.property("base_version").toString())
	}
}
