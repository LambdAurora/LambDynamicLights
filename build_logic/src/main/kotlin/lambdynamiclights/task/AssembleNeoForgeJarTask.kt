package lambdynamiclights.task

import com.google.gson.JsonParser
import dev.lambdaurora.mcdev.api.AccessWidenerToTransformer
import dev.lambdaurora.mcdev.util.JsonUtils
import dev.lambdaurora.mcdev.util.ZipFix
import lambdynamiclights.Constants
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.TaskAction
import java.nio.file.FileSystem
import java.nio.file.FileSystems
import java.nio.file.Files
import javax.inject.Inject

abstract class AssembleNeoForgeJarTask @Inject constructor() : AbstractAssembleJarTask() {
	@get:Input
	abstract val artifactGroup: Property<String>

	@get:InputFile
	abstract val runtimeMojmapJar: RegularFileProperty

	@get:InputFile
	abstract val neoforgeJar: RegularFileProperty

	@get:InputFile
	abstract val jarJarMetadata: RegularFileProperty

	init {
		this.artifactGroup.convention(this.project.group as String)
	}

	@TaskAction
	fun assemble() {
		val outputJar = this.archiveFile.get().asFile.toPath()
		val runtimeMojmapJarPath = this.runtimeMojmapJar.get().asFile.toPath()
		val neoforgeJarPath = this.neoforgeJar.get().asFile.toPath()

		this.openJar(outputJar).use { outFs ->
			FileSystems.newFileSystem(
				runtimeMojmapJarPath, mapOf(
					"accessMode" to "readOnly",
					"enablePosixFileAttributes" to "true",
				)
			).use { runtimeJarFs ->
				FileSystems.newFileSystem(
					neoforgeJarPath, mapOf(
						"accessMode" to "readOnly",
						"enablePosixFileAttributes" to "true",
					)
				).use { neoJarFs ->
					runtimeJarFs.rootDirectories.forEach { rootDir ->
						Files.list(rootDir)
							.filter { !it.fileName.toString().endsWith("accesswidener") }
							.forEach {
								this.copy(it, outFs.getPath(it.toString())) { toCopy ->
									val raw = toCopy.toString()
									!raw.contains("fabric") && !raw.endsWith(".jar")
								}
							}
					}
					neoJarFs.rootDirectories.forEach { rootDir ->
						Files.list(rootDir)
							.forEach {
								this.copy(it, outFs.getPath(it.toString())) { toCopy ->
									!toCopy.toString().contains("fabric")
								}
							}
					}

					AccessWidenerToTransformer.convert(
						runtimeJarFs.getPath("lambdynlights.accesswidener"),
						outFs.getPath("META-INF/accesstransformer.cfg")
					)
					this.handleNeoJar(outFs)
				}
			}

			this.handleJarJarMetadata(outFs)
		}

		ZipFix.makeZipReproducible(outputJar)
	}

	private fun handleNeoJar(fs: FileSystem) {
		val mixinsJson = JsonParser.parseString(Files.readString(fs.getPath("lambdynlights.mixins.json")))
			.asJsonObject
		mixinsJson.addProperty("refmap", "lambdynlights-refmap.json")
		this.writeString(fs.getPath("lambdynlights.mixins.json"), JsonUtils.GSON.toJson(mixinsJson))
	}

	private fun handleJarJarMetadata(fs: FileSystem) {
		val jarjarDirPath = fs.getPath("META-INF/jarjar")
		this.createDirectories(jarjarDirPath)

		val metadata = JsonParser.parseString(Files.readString(this.jarJarMetadata.get().asFile.toPath())).asJsonObject
		val jars = metadata.getAsJsonArray("jars")

		for (jar in jars) {
			val obj = jar.asJsonObject
			val identifier = obj.getAsJsonObject("identifier")
			if (identifier.getAsJsonPrimitive("group").asString.equals(this.artifactGroup.get())) {
				identifier.addProperty("artifact", Constants.API_ARTIFACT)
			}
		}

		Files.writeString(jarjarDirPath.resolve("metadata.json"), JsonUtils.GSON.toJson(metadata))
	}
}
