import com.modrinth.minotaur.dependencies.ModDependency
import dev.lambdaurora.mcdev.api.McVersionLookup
import dev.lambdaurora.mcdev.api.ModVersionDependency
import dev.lambdaurora.mcdev.task.packaging.PackageModrinthTask
import lambdynamiclights.Constants
import lambdynamiclights.Utils
import net.darkhax.curseforgegradle.TaskPublishCurseForge

plugins {
	id("lambdynamiclights")
	`maven-publish`
	id("com.gradleup.shadow").version("8.3.3")
	id("com.modrinth.minotaur").version("2.+")
	id("net.darkhax.curseforgegradle").version("1.1.+")
}

base.archivesName.set(Constants.NAME)

logger.lifecycle("Preparing version ${version}...")

val fabricApiModules = listOf(
	fabricApi.module("fabric-lifecycle-events-v1", libs.versions.fabric.api.get())!!,
	fabricApi.module("fabric-resource-loader-v0", libs.versions.fabric.api.get())!!,
	fabricApi.module("fabric-rendering-v1", libs.versions.fabric.api.get())!!
)

tasks.generateFmj.configure {
	val fmj = this.fmj.get()
		.withEntrypoints("yumi:client_init", "dev.lambdaurora.lambdynlights.LambDynLights")
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

repositories {
	mavenLocal()
	maven {
		name = "Terraformers"
		url = uri("https://maven.terraformersmc.com/releases/")
	}
	maven {
		name = "ParchmentMC"
		url = uri("https://maven.parchmentmc.org")
	}
	maven {
		name = "Ladysnake Libs"
		url = uri("https://maven.ladysnake.org/releases")
	}
	maven { url = uri("https://maven.wispforest.io/releases") }
}

loom {
	accessWidenerPath = file("src/main/resources/lambdynlights.accesswidener")
}

dependencies {
	api(project(":api", configuration = "namedElements"))
	include(project(":api"))

	modImplementation(libs.fabric.loader)
	fabricApiModules.forEach { modImplementation(it) }
	//modRuntimeOnly(fabricApi.module("fabric-renderer-indigo", libs.versions.fabric.api.get()))
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
	modLocalRuntime(libs.modmenu) {
		this.isTransitive = false
	}

	// Mod compatibility
	modCompileOnly(libs.trinkets)
	modCompileOnly(libs.accessories)

	shadow(libs.nightconfig.core)
	shadow(libs.nightconfig.toml)
}

tasks.processResources {
	inputs.property("version", project.version)

	filesMatching("fabric.mod.json") {
		expand("version" to inputs.properties["version"])
	}
}

tasks.shadowJar {
	dependsOn(tasks.jar)
	configurations = listOf(project.configurations["shadow"])
	destinationDirectory.set(file("${project.layout.buildDirectory.get()}/devlibs"))
	archiveClassifier.set("dev")

	relocate("com.electronwill.nightconfig", "dev.lambdaurora.lambdynlights.shadow.nightconfig")

	from(rootProject.file("LICENSE")) {
		rename { "${it}_${Constants.NAME}" }
	}
}

tasks.remapJar {
	dependsOn(tasks.shadowJar)
}

val packageModrinth by tasks.registering(PackageModrinthTask::class) {
	this.group = "publishing"
	this.versionType.set(Constants.getVersionType())
	this.versionName.set("${Constants.PRETTY_NAME} ${Constants.VERSION} (${McVersionLookup.getVersionTag(Constants.mcVersion())})")
	this.gameVersions.set(listOf(Constants.mcVersion()) + Constants.COMPATIBLE_MC_VERSIONS)
	this.loaders.set(listOf("fabric", "quilt"))
	this.dependencies.set(listOf(
		ModVersionDependency("P7dR8mSH", ModVersionDependency.Type.REQUIRED),
		ModVersionDependency("reCfnRvJ", ModVersionDependency.Type.INCOMPATIBLE),
		ModVersionDependency("PxQSWIcD", ModVersionDependency.Type.INCOMPATIBLE)
	))
	this.changelog.set(Utils.fetchChangelog(project))
	this.readme.set(Utils.parseReadme(project))
	this.files.setFrom(tasks.remapJar.get())
}

modrinth {
	projectId = project.property("modrinth_id") as String
	versionName = "${Constants.PRETTY_NAME} ${Constants.VERSION} (${McVersionLookup.getVersionTag(Constants.mcVersion())})"
	uploadFile.set(tasks.remapJar.get())
	loaders.set(listOf("fabric", "quilt"))
	gameVersions.set(listOf(Constants.mcVersion()) + Constants.COMPATIBLE_MC_VERSIONS)
	versionType.set(Constants.getVersionType().toString())
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

	val mainFile = upload(project.property("curseforge_id"), tasks.remapJar.get())
	mainFile.releaseType = Constants.getVersionType()
	mainFile.addGameVersion(McVersionLookup.getCurseForgeEquivalent(Constants.mcVersion()))
	Constants.COMPATIBLE_MC_VERSIONS.stream()
		.map { McVersionLookup.getCurseForgeEquivalent(it) }
		.forEach { mainFile.addGameVersion(it) }
	mainFile.addModLoader("Fabric", "Quilt")
	mainFile.addJavaVersion("Java 21", "Java 22")
	mainFile.addEnvironment("Client")

	mainFile.displayName = "${Constants.PRETTY_NAME} ${Constants.VERSION} (${McVersionLookup.getVersionTag(Constants.mcVersion())})"
	mainFile.addRequirement("fabric-api")
	mainFile.addOptional("modmenu")
	mainFile.addIncompatibility("optifabric")

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
