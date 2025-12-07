import com.modrinth.minotaur.dependencies.ModDependency
import dev.lambdaurora.mcdev.api.MappingVariant
import dev.lambdaurora.mcdev.api.McVersionLookup
import dev.lambdaurora.mcdev.api.ModVersionDependency
import dev.lambdaurora.mcdev.api.manifest.Nmt
import dev.lambdaurora.mcdev.task.GenerateNeoForgeJiJDataTask
import dev.lambdaurora.mcdev.task.packaging.PackageModrinthTask
import lambdynamiclights.Constants
import lambdynamiclights.Utils
import lambdynamiclights.task.AssembleFinalJarTask
import lambdynamiclights.task.AssembleNeoForgeJarTask
import net.darkhax.curseforgegradle.TaskPublishCurseForge
import net.fabricmc.loom.task.RemapJarTask
import net.fabricmc.loom.task.RemapSourcesJarTask

plugins {
	id("lambdynamiclights")
	`maven-publish`
	id("com.gradleup.shadow").version("9.1.0")
	id("com.modrinth.minotaur").version("2.+")
	id("net.darkhax.curseforgegradle").version("1.1.+")
}

base.archivesName.set(Constants.NAME)

logger.lifecycle("Preparing version ${version}...")
lambdamcdev.setupActionsRefCheck()

val fabricApiModules = listOf(
	fabricApi.module("fabric-lifecycle-events-v1", libs.versions.fabric.api.get())!!,
	fabricApi.module("fabric-resource-loader-v0", libs.versions.fabric.api.get())!!,
	fabricApi.module("fabric-resource-loader-v1", libs.versions.fabric.api.get())!!,
	fabricApi.module("fabric-resource-conditions-api-v1", libs.versions.fabric.api.get())!!
)

val neoforge: SourceSet by sourceSets.creating {
	this.compileClasspath += sourceSets.main.get().compileClasspath
	this.runtimeClasspath += sourceSets.main.get().runtimeClasspath
}

tasks.generateFmj.configure {
	val fmj = this.fmj.get()
		.withEntrypoints("yumi:client_init", "dev.lambdaurora.lambdynlights.LambDynLights::INSTANCE")
		.withEntrypoints("lambdynlights:platform", "dev.lambdaurora.lambdynlights.platform.fabric.FabricPlatform")
		.withEntrypoints("modmenu", "dev.lambdaurora.lambdynlights.LambDynLightsModMenu")
		.withAccessWidener("lambdynlights.accesswidener")
		.withMixins("lambdynlights.mixins.json", "lambdynlights.lightsource.mixins.json")
		.withDepend("${Constants.NAMESPACE}_api", ">=${version}")
		.withDepend("spruceui", ">=${libs.versions.spruceui.get()}")
		.withDepend("yumi_mc_core", ">=${libs.versions.yumi.mc.foundation.get()}")
		.withRecommend("modmenu", ">=${libs.versions.modmenu.get()}")
		.withBreak("optifabric", "*")
		.withBreak("sodiumdynamiclights", "*")
		.withBreak("ryoamiclights", "*")

	fabricApiModules.forEach { module -> fmj.withDepend(module.name, ">=${module.version}") }
}

lambdamcdev.manifests {
	val fmj = this.fmj().get()

	nmt {
		fmj.copyTo(this)
		withNamespace(Constants.NAMESPACE + "_runtime")
		withName(Constants.PRETTY_NAME + " (Runtime)")
		withDescription(Constants.RUNTIME_DESCRIPTION)
		withLoaderVersion("[2,)")
		withYumiEntrypoints("yumi:client_init", "dev.lambdaurora.lambdynlights.LambDynLights::INSTANCE")
		withYumiEntrypoints("lambdynlights:platform", "dev.lambdaurora.lambdynlights.platform.neoforge.NeoForgePlatform::INSTANCE")
		withAccessTransformer("META-INF/accesstransformer.cfg")
		withMixins("lambdynlights.mixins.json", "lambdynlights.lightsource.mixins.json")
		withDepend(Constants.NAMESPACE + "_api", "[${version},)", Nmt.DependencySide.CLIENT)
		withDepend("minecraft", project.property("neoforge_mc_constraints").toString())
		withDepend("spruceui", "[${libs.versions.spruceui.get()},)", Nmt.DependencySide.CLIENT)
		withDepend("yumi_mc_core", "[${libs.versions.yumi.mc.foundation.get()},)", Nmt.DependencySide.CLIENT)
		withBreak("sodiumdynamiclights", "*", Nmt.DependencySide.CLIENT)
		withBreak("ryoamiclights", "*", Nmt.DependencySide.CLIENT)
	}
}

repositories {
	mavenLocal()
	maven {
		name = "Terraformers"
		url = uri("https://maven.terraformersmc.com/releases/")
		content {
			includeGroupAndSubgroups("com.terraformersmc")
			includeGroup("dev.emi")
		}
	}
	maven {
		name = "Ladysnake Libs"
		url = uri("https://maven.ladysnake.org/releases")
		content {
			includeGroup("org.ladysnake.cardinal-components-api")
		}
	}
	maven {
		name = "Wispforest"
		url = uri("https://maven.wispforest.io/releases")
		content {
			includeGroupAndSubgroups("io.wispforest")
		}
	}
	maven {
		name = "NeoForge"
		url = uri("https://maven.neoforged.net/")
		content {
			includeGroupAndSubgroups("net.neoforged")
			includeGroupAndSubgroups("cpw.mods")
		}
	}
}

loom {
	accessWidenerPath = file("src/main/resources/lambdynlights.accesswidener")
}

val mojmap = lambdamcdev.setupMojmapRemapping()

afterEvaluate {
	val shims: SourceSet by sourceSets.creating {
		this.compileClasspath += configurations["minecraftNamedCompile"]
	}

	dependencies {
		"shimsCompileOnly"(libs.fabric.loader) // Due to MC classes referring to EnvType.
		"shimsCompileOnly"(libs.neoforge.loader)
		"neoforgeCompileOnly"(shims.output)
	}

	license {
		exclude(shims)
	}
}

dependencies {
	api(project(":api", configuration = "namedElements"))
	include(project(":api"))

	modImplementation(libs.fabric.loader)
	fabricApiModules.forEach { modImplementation(it) }
	modImplementation(libs.yumi.mc.foundation)
	include(libs.yumi.mc.foundation)

	implementation(libs.nightconfig.core)
	implementation(libs.nightconfig.toml)
	modImplementation(libs.spruceui)
	include(libs.spruceui)
	modImplementation(libs.pridelib)
	include(libs.pridelib)

	modCompileOnly(libs.modmenu) {
		this.isTransitive = false
	}
	/*modLocalRuntime(libs.modmenu) {
		this.isTransitive = false
	}*/

	// Mod compatibility
	modCompileOnly(libs.trinkets)
	modCompileOnly(libs.accessories)

	shadow(libs.nightconfig.core)
	shadow(libs.nightconfig.toml)

	"neoforgeCompileOnly"(libs.neoforge.loader)
	"neoforgeImplementation"(sourceSets.main.get().output)

	//region Mojmap
	"mojmapInclude"(project(":api", configuration = "mojmapRuntimeElements"))
	"mojmapApi"(project(":api")) {
		attributes {
			attribute(MappingVariant.ATTRIBUTE, objects.named(MappingVariant.MOJMAP))
		}
	}
	"mojmapImplementation"(libs.yumi.mc.foundation) {
		attributes {
			attribute(MappingVariant.ATTRIBUTE, objects.named(MappingVariant.MOJMAP))
		}
	}
	"mojmapInclude"(libs.yumi.mc.foundation) {
		attributes {
			attribute(MappingVariant.ATTRIBUTE, objects.named(MappingVariant.MOJMAP))
		}
	}
	"mojmapImplementation"(libs.spruceui) {
		attributes {
			attribute(MappingVariant.ATTRIBUTE, objects.named(MappingVariant.MOJMAP))
		}
	}
	"mojmapInclude"(libs.spruceui) {
		attributes {
			attribute(MappingVariant.ATTRIBUTE, objects.named(MappingVariant.MOJMAP))
		}
	}
	"mojmapImplementation"(libs.pridelib) {
		attributes {
			attribute(MappingVariant.ATTRIBUTE, objects.named(MappingVariant.MOJMAP))
		}
	}
	"mojmapInclude"(libs.pridelib) {
		attributes {
			attribute(MappingVariant.ATTRIBUTE, objects.named(MappingVariant.MOJMAP))
		}
	}
	//endregion
}

loom.runs.getByName("client") {
	this.vmArg("-DMC_DEBUG_ENABLED")
	this.vmArg("-DMC_DEBUG_HOTKEYS")
}

tasks.shadowJar {
	dependsOn(tasks.jar)
	configurations = listOf(project.configurations["shadow"])
	destinationDirectory.set(project.layout.buildDirectory.dir("devlibs"))
	archiveClassifier.set("dev")

	relocate("com.electronwill.nightconfig", "dev.lambdaurora.lambdynlights.shadow.nightconfig")

	from(rootProject.file("LICENSE")) {
		rename { "${it}_${Constants.NAME}" }
	}
}

tasks.remapJar {
	this.dependsOn(tasks.shadowJar)

	this.destinationDirectory = project.layout.buildDirectory.dir("devlibs")
}

val neoforgeJar = tasks.register<Jar>("neoforgeJar") {
	this.group = "build"
	this.from(neoforge.output)
	this.archiveClassifier = "neoforge-dev"
	this.destinationDirectory = project.layout.buildDirectory.dir("devlibs/neoforge")
}

val neoforgeSourcesJar = tasks.register<Jar>("neoforgeSourcesJar") {
	this.group = "build"
	this.from(neoforge.java.sourceDirectories)
	this.from(neoforge.resources.sourceDirectories)
	this.archiveClassifier = "neoforge-dev-sources"
	this.destinationDirectory = project.layout.buildDirectory.dir("devlibs/neoforge")
}

val remapNeoforgeJar = tasks.register<RemapJarTask>("remapNeoforgeJarToIntermediary") {
	this.group = "remapping"
	this.dependsOn(neoforgeJar.get())
	this.inputFile.set(neoforgeJar.get().archiveFile)
	this.classpath.from(neoforge.compileClasspath)
	this.archiveClassifier = "neoforge-intermediary"
	this.destinationDirectory = project.layout.buildDirectory.dir("devlibs/neoforge")

	addNestedDependencies = false // Jars will be included later.
}

val remapNeoforgeSourcesJar = tasks.register<RemapSourcesJarTask>("remapNeoforgeSourcesJarToIntermediary") {
	this.group = "remapping"
	this.dependsOn(neoforgeSourcesJar.get())
	this.inputFile.set(neoforgeSourcesJar.get().archiveFile)
	this.classpath.from(neoforge.compileClasspath)
	this.archiveClassifier = "neoforge-intermediary-sources"
	this.destinationDirectory = project.layout.buildDirectory.dir("devlibs/neoforge")
}

//region Mojmap
val remapMojmap = mojmap.registerRemap(tasks.remapJar) {
	this.destinationDirectory = project.layout.buildDirectory.dir("devlibs")
}

val remapSourcesMojmap = mojmap.registerSourcesRemap(tasks.remapSourcesJar) {
	this.classpath.from(configurations["minecraftClientLibraries"])
	this.destinationDirectory = project.layout.buildDirectory.dir("devlibs")
}

val remapNeoforgeJarToMojmap = mojmap.registerRemap("remapNeoforgeJarToMojmap") {
	this.dependsOn(remapNeoforgeJar)

	inputFile.set(remapNeoforgeJar.flatMap { it.archiveFile })

	this.archiveClassifier = "neoforge-mojmap"
	this.destinationDirectory = project.layout.buildDirectory.dir("devlibs/neoforge")
}

val remapNeoforgeSourcesJarToMojmap = mojmap.registerSourcesRemap("remapNeoforgeSourcesJarToMojmap") {
	this.dependsOn(remapNeoforgeSourcesJar)

	inputFile.set(remapNeoforgeSourcesJar.flatMap { it.archiveFile })

	this.classpath.from(configurations["minecraftClientLibraries"])
	this.archiveClassifier = "neoforge-mojmap-sources"
	this.destinationDirectory = project.layout.buildDirectory.dir("devlibs/neoforge")
}

val generateJarJarMetadata by tasks.registering(GenerateNeoForgeJiJDataTask::class) {
	val includeConfig = project.configurations.getByName("mojmapIncludeInternal");
	this.from(includeConfig)
	this.outputFile.set(
		project.layout.buildDirectory
			.asFile
			.map(File::toPath)
			.map { path -> path.resolve("generated/jarjar/metadata.json").toFile() }
			.get()
	)
}

val mergedNeoForgeJar by tasks.registering(AssembleNeoForgeJarTask::class) {
	this.group = "build"
	this.dependsOn(
		remapMojmap,
		remapNeoforgeJarToMojmap,
		generateJarJarMetadata
	)

	this.runtimeMojmapJar.set(remapMojmap.flatMap { it.archiveFile })
	this.neoforgeJar.set(remapNeoforgeJarToMojmap.flatMap { it.archiveFile })
	this.jarJarMetadata.set(generateJarJarMetadata.flatMap { it.outputFile })
	this.archiveClassifier = "mojmap"
}

val mergedNeoForgeSourcesJar by tasks.registering(AssembleNeoForgeJarTask::class) {
	this.group = "build"
	this.dependsOn(
		remapSourcesMojmap,
		remapNeoforgeSourcesJarToMojmap,
		generateJarJarMetadata
	)

	this.runtimeMojmapJar.set(remapSourcesMojmap.flatMap { it.archiveFile })
	this.neoforgeJar.set(remapNeoforgeSourcesJarToMojmap.flatMap { it.archiveFile })
	this.jarJarMetadata.set(generateJarJarMetadata.flatMap { it.outputFile })
	this.archiveClassifier = "mojmap-sources"
}

val finalJar by tasks.registering(AssembleFinalJarTask::class) {
	this.group = "build"
	this.dependsOn(
		remapMojmap,
		mergedNeoForgeJar,
		generateJarJarMetadata
	)

	this.artifactGroup.set(project.group.toString())
	this.version.set(project.version.toString())
	this.fmj.set(lambdamcdev.manifests.fmj())
	this.nmt.set(lambdamcdev.manifests.nmt())
	this.runtimeIntermediaryJar.set(tasks.remapJar.flatMap { it.archiveFile })
	this.runtimeNeoForgeJar.set(mergedNeoForgeJar.flatMap { it.archiveFile })
	this.jarJarMetadata.set(generateJarJarMetadata.flatMap { it.outputFile })
}

tasks.assemble.get().dependsOn(finalJar, mergedNeoForgeSourcesJar)
mojmap.setJarArtifact(mergedNeoForgeJar)
mojmap.setSourcesArtifact(mergedNeoForgeSourcesJar)
//endregion

val packageModrinth by tasks.registering(PackageModrinthTask::class) {
	this.group = "publishing"
	this.versionType.set(ldl.versionType())
	this.versionName.set("${Constants.PRETTY_NAME} ${ldl.version()} (${McVersionLookup.getVersionTag(ldl.mcVersion())})")
	this.gameVersions.set(ldl.compatibleMcVersions())
	this.loaders.set(listOf("fabric", "quilt", "neoforge"))
	this.dependencies.set(
		listOf(
			ModVersionDependency("P7dR8mSH", ModVersionDependency.Type.REQUIRED),
			ModVersionDependency("reCfnRvJ", ModVersionDependency.Type.INCOMPATIBLE),
			ModVersionDependency("PxQSWIcD", ModVersionDependency.Type.INCOMPATIBLE)
		)
	)
	this.changelog.set(Utils.fetchChangelog(project))
	this.readme.set(Utils.parseReadme(project))
	this.files.setFrom(finalJar)
}

modrinth {
	projectId = project.property("modrinth_id") as String
	versionName = "${Constants.PRETTY_NAME} ${ldl.version()} (${McVersionLookup.getVersionTag(ldl.mcVersion())})"
	uploadFile.set(finalJar)
	loaders.set(listOf("fabric", "quilt", "neoforge"))
	gameVersions.set(ldl.compatibleMcVersions())
	versionType.set(ldl.versionType().toString())
	syncBodyFrom.set(Utils.parseReadme(project))
	dependencies.set(
		listOf(
			ModDependency("P7dR8mSH", "required"),
			ModDependency("reCfnRvJ", "incompatible"),
			ModDependency("PxQSWIcD", "incompatible")
		)
	)

	// Changelog fetching
	val changelogContent = Utils.fetchChangelog(project)

	if (changelogContent != null) {
		changelog.set(changelogContent)
	} else {
		afterEvaluate {
			tasks.modrinth.get().isEnabled = false
		}
	}
}

tasks.register<TaskPublishCurseForge>("curseforge") {
	this.group = "publishing"

	val token = System.getenv("CURSEFORGE_TOKEN")
	if (token != null) {
		this.apiToken = token
	} else {
		this.isEnabled = false
		return@register
	}

	// Changelog fetching
	var changelogContent = Utils.fetchChangelog(project)

	if (changelogContent != null) {
		changelogContent = "Changelog:\n\n${changelogContent}"
	} else {
		this.isEnabled = false
		return@register
	}

	val mainFile = upload(project.property("curseforge_id"), finalJar)
	mainFile.releaseType = ldl.versionType()
	ldl.compatibleMcVersions().stream()
		.map { McVersionLookup.getCurseForgeEquivalent(it) }
		.forEach { mainFile.addGameVersion(it) }
	mainFile.addModLoader("Fabric", "Quilt", "NeoForge")
	mainFile.addJavaVersion("Java 21", "Java 22")
	mainFile.addEnvironment("Client")

	mainFile.displayName = "${Constants.PRETTY_NAME} ${ldl.version()} (${McVersionLookup.getVersionTag(ldl.mcVersion())})"
	mainFile.addRequirement("fabric-api")
	mainFile.addOptional("modmenu")
	mainFile.addIncompatibility("optifabric")
	mainFile.addIncompatibility("ryoamiclights")
	mainFile.addIncompatibility("dynamiclights-reforged")

	mainFile.changelogType = "markdown"
	mainFile.changelog = changelogContent
}

// Configure the maven publication.
publishing {
	publications {
		create<MavenPublication>("mavenJava") {
			from(components["java"])

			artifactId = "lambdynamiclights-runtime"

			pom {
				name.set(Constants.PRETTY_NAME)
				description.set(Constants.DESCRIPTION)
			}
		}
	}
}
