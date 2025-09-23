package lambdynamiclights.task

import com.google.gson.JsonArray
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import dev.lambdaurora.mcdev.api.manifest.Fmj
import dev.lambdaurora.mcdev.api.manifest.Nmt
import dev.lambdaurora.mcdev.util.JsonUtils
import lambdynamiclights.Constants
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.TaskAction
import java.nio.file.FileSystem
import java.nio.file.FileSystems
import java.nio.file.Files
import java.nio.file.Path
import javax.inject.Inject

abstract class AssembleFinalJarTask @Inject constructor() : AbstractAssembleJarTask() {
	@get:Input
	abstract val artifactGroup: Property<String>

	@get:Input
	abstract val version: Property<String>

	@get:Input
	abstract val fmj: Property<Fmj>

	@get:Input
	abstract val nmt: Property<Nmt>

	@get:InputFile
	abstract val runtimeIntermediaryJar: RegularFileProperty

	@get:InputFile
	abstract val runtimeNeoForgeJar: RegularFileProperty

	@get:InputFile
	abstract val jarJarMetadata: RegularFileProperty

	@TaskAction
	fun assemble() {
		val outputJar = this.archiveFile.get().asFile.toPath()
		val runtimeIntermediaryJarPath = this.runtimeIntermediaryJar.get().asFile.toPath()
		val neoForgeJarPath = this.runtimeNeoForgeJar.get().asFile.toPath()

		FileSystems.newFileSystem(outputJar).use { outFs ->
			val outJarsDir = outFs.getPath("META-INF/jars")
			val outFabricJar = outJarsDir.resolve(
				runtimeIntermediaryJarPath.fileName.toString().replace("-intermediary", "-fabric")
			)
			val outNeoForgeJar = outJarsDir.resolve(
				neoForgeJarPath.fileName.toString().replace("-mojmap", "-neoforge")
			)

			Files.createDirectories(outJarsDir)
			Files.copy(
				runtimeIntermediaryJarPath,
				outFabricJar
			)
			Files.copy(
				neoForgeJarPath,
				outNeoForgeJar
			)

			FileSystems.newFileSystem(outFabricJar).use { fabricJarFs ->
				FileSystems.newFileSystem(outNeoForgeJar).use { neoJarFs ->
					this.doAssemble(outFs, fabricJarFs, neoJarFs)
					this.writeFmj(fabricJarFs, outFs, outFabricJar)
					this.writeNmt(neoJarFs, outFs)
					this.handleJarJar(neoJarFs, outFs, outNeoForgeJar)
				}

				this.cleanupFabricJar(fabricJarFs)
			}
		}
	}

	private fun doAssemble(outFs: FileSystem, fabricJarFs: FileSystem, neoJarFs: FileSystem) {
		Files.createDirectories(outFs.getPath("dev/lambdaurora/lambdynlights"))
		this.biMove("dev/lambdaurora/lambdynlights/shadow", fabricJarFs, neoJarFs, outFs)
		this.biMove("META-INF/versions", fabricJarFs, neoJarFs, outFs)
		this.biMove("assets", fabricJarFs, neoJarFs, outFs)
		this.biMove("lambdynlights.toml", fabricJarFs, neoJarFs, outFs)
		this.copy("LICENSE_lambdynamiclights", fabricJarFs, outFs)

		this.copy("assets/lambdynlights/icon.png", outFs, fabricJarFs)
		this.copy("assets/lambdynlights/icon.png", outFs, neoJarFs)
	}

	fun cleanupFabricJar(fs: FileSystem) {
		Files.deleteIfExists(fs.getPath("META-INF/neoforge.mods.toml"))
	}

	private fun writeFmj(fabricFs: FileSystem, outFs: FileSystem, outFabricJar: Path) {
		val fmjPath = fabricFs.getPath("fabric.mod.json")
		val json = JsonParser.parseString(Files.readString(fmjPath)).asJsonObject

		json.addProperty("id", Constants.NAMESPACE + "_runtime")
		json.addProperty("name", Constants.PRETTY_NAME + " (Runtime)")
		json.addProperty("description", Constants.DESCRIPTION)

		val modmenuObject = json.getAsJsonObject("custom").getAsJsonObject("modmenu")
		val parent = JsonObject()
		modmenuObject.add("parent", parent)
		parent.addProperty("id", Constants.NAMESPACE)
		parent.addProperty("name", Constants.PRETTY_NAME)

		Files.writeString(fmjPath, JsonUtils.GSON.toJson(json))

		val parentFmj = this.fmj.get().derive(::Fmj)
		parentFmj.withDepend(Constants.NAMESPACE + "_runtime", ">=${this.version.get()}")
		parentFmj.withEnvironment(this.fmj.get().environment)
		parentFmj.withJar(outFabricJar.toString())
		parentFmj.withModMenu(this.fmj.get().modMenu.copy())
		Files.writeString(outFs.getPath("fabric.mod.json"), JsonUtils.GSON.toJson(parentFmj))
	}

	private fun handleJarJar(neoFs: FileSystem, outFs: FileSystem, neoJarPath: Path) {
		Files.createDirectories(outFs.getPath("META-INF/jarjar"))

		val jarJarMetadata = JsonObject()

		val jars = JsonArray()
		jarJarMetadata.add("jars", jars)
		val neoForgeJarEntry = JsonObject()

		val identifier = JsonObject()
		identifier.addProperty("group", this.artifactGroup.get())
		identifier.addProperty("artifact", Constants.NAMESPACE + "-runtime-neoforge")
		neoForgeJarEntry.add("identifier", identifier)

		val version = JsonObject()
		version.addProperty("range", "[${this.version.get()},)")
		version.addProperty("artifactVersion", this.version.get())
		neoForgeJarEntry.add("version", version)

		neoForgeJarEntry.addProperty("path", neoJarPath.toString())
		neoForgeJarEntry.addProperty("isObfuscated", false)

		jars.add(neoForgeJarEntry)

		Files.writeString(
			outFs.getPath("META-INF/jarjar/metadata.json"),
			JsonUtils.GSON.toJson(jarJarMetadata)
		)
	}

	private fun writeNmt(neoFs: FileSystem, outFs: FileSystem) {
		val parentNmt = this.nmt.get().derive(::Nmt)
		parentNmt.withName(Constants.PRETTY_NAME)
		parentNmt.withDescription(Constants.DESCRIPTION)
		parentNmt.withLoaderVersion(this.nmt.get().loaderVersion)
		parentNmt.withBlurIcon(this.nmt.get().shouldBlurIcon())
		parentNmt.withDepend(
			Constants.NAMESPACE + "_runtime",
			"[${this.version.get()},)",
			Nmt.DependencySide.CLIENT
		)

		Files.writeString(outFs.getPath("META-INF/neoforge.mods.toml"), parentNmt.toToml())

		val runtimeNmt = this.nmt.get().derive(::Nmt)
		this.nmt.get().copyTo(runtimeNmt)
		runtimeNmt.withNamespace(Constants.NAMESPACE + "_runtime")

		Files.writeString(neoFs.getPath("META-INF/neoforge.mods.toml"), runtimeNmt.toToml())
	}

	private fun biMove(path: String, sourceFs: FileSystem, dupedSourceFs: FileSystem, targetFs: FileSystem) {
		this.move(path, sourceFs, targetFs)
		this.recursivelyDelete(dupedSourceFs.getPath(path))
	}
}