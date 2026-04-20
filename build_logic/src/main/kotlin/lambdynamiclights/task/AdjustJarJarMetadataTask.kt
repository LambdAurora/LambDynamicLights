package lambdynamiclights.task

import com.google.gson.JsonParser
import dev.lambdaurora.mcdev.util.JsonUtils
import lambdynamiclights.Constants
import org.gradle.api.DefaultTask
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.TaskAction
import java.nio.file.Files
import javax.inject.Inject

abstract class AdjustJarJarMetadataTask @Inject constructor() : DefaultTask() {
	@get:Input
	abstract val artifactGroup: Property<String>

	@get:InputFile
	abstract val jarJarMetadata: RegularFileProperty

	@get:OutputFile
	abstract val outputFile: RegularFileProperty

	@TaskAction
	fun handleJarJarMetadata() {
		val metadata = JsonParser.parseString(Files.readString(this.jarJarMetadata.get().asFile.toPath())).asJsonObject
		val jars = metadata.getAsJsonArray("jars")

		for (jar in jars) {
			val obj = jar.asJsonObject
			val identifier = obj.getAsJsonObject("identifier")
			if (identifier.getAsJsonPrimitive("group").asString.equals(this.artifactGroup.get())) {
				identifier.addProperty("artifact", Constants.API_ARTIFACT)
			}
		}

		Files.writeString(this.outputFile.get().asFile.toPath(), JsonUtils.GSON.toJson(metadata))
	}
}
