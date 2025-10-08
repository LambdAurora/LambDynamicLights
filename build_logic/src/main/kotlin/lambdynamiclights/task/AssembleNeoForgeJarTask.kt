package lambdynamiclights.task

import com.google.gson.JsonParser
import dev.lambdaurora.mcdev.api.AccessWidenerToTransformer
import dev.lambdaurora.mcdev.util.JsonUtils
import lambdynamiclights.ZipFix
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.TaskAction
import java.nio.file.FileSystem
import java.nio.file.FileSystems
import java.nio.file.Files
import javax.inject.Inject

abstract class AssembleNeoForgeJarTask @Inject constructor() : AbstractAssembleJarTask() {
	@get:InputFile
	abstract val runtimeMojmapJar: RegularFileProperty

	@get:InputFile
	abstract val neoforgeJar: RegularFileProperty

	@get:InputFile
	abstract val jarJarMetadata: RegularFileProperty

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

			val jarjarDirPath = outFs.getPath("META-INF/jarjar")
			this.createDirectories(jarjarDirPath)
			this.copy(this.jarJarMetadata.get().asFile.toPath(), jarjarDirPath.resolve("metadata.json"))
		}

		ZipFix.fixZip(outputJar)
	}

	private fun handleNeoJar(fs: FileSystem) {
		val mixinsJson = JsonParser.parseString(Files.readString(fs.getPath("lambdynlights.mixins.json")))
			.asJsonObject
		mixinsJson.addProperty("refmap", "lambdynlights-refmap.json")
		this.writeString(fs.getPath("lambdynlights.mixins.json"), JsonUtils.GSON.toJson(mixinsJson))
	}
}
