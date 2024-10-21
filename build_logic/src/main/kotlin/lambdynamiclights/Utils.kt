package lambdynamiclights

import org.gradle.api.Project

object Utils {
	fun parseReadme(project: Project): String {
		val linkRegex = "!\\[([A-z_ ]+)]\\((images\\/[A-z.\\/_]+)\\)"

		var readme = project.rootProject.file("README.md").readText()
		val lines = readme.split("\n").toMutableList()
		val it = lines.listIterator()

		var shouldRemove = false;
		while (it.hasNext()) {
			val line = it.next();

			if (line == "<!-- modrinth_exclude.long_start -->") {
				shouldRemove = true
			}

			if (shouldRemove) {
				it.remove()
			}

			if (line == "<!-- modrinth_exclude.long_end -->") {
				shouldRemove = false
			}
		}

		readme = lines.joinToString("\n")
		readme = readme.replace(linkRegex.toRegex(), "![\$1](https://raw.githubusercontent.com/LambdAurora/LambDynamicLights/1.21.2/\$2)")
		return readme
	}

	fun fetchChangelog(project: Project): String? {
		val changelogText = project.rootProject.file("CHANGELOG.md").readText()
		val regexVersion = Constants.VERSION.replace("\\.".toRegex(), "\\.").replace("\\+".toRegex(), "\\+")
		val changelogRegex = "###? ${regexVersion}\\n\\n(( *- .+\\n)+)".toRegex()
		val matcher = changelogRegex.find(changelogText)

		if (matcher != null) {
			var changelogContent = matcher.groupValues[1]

			val changelogLines = changelogText.split("\n")
			val linkRefRegex = "^\\[([A-z\\d _\\-/+.]+)]: ".toRegex()
			for (i in changelogLines.size - 1 downTo 0) {
				val line = changelogLines[i]
				if (line matches linkRefRegex)
					changelogContent += "\n" + line
				else break
			}
			return changelogContent
		} else {
			return null;
		}
	}
}