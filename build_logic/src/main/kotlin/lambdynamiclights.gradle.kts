import lambdynamiclights.Constants
import org.gradle.accessors.dm.LibrariesForLibs

plugins {
	id("lambdynamiclights-common")
	`java-library`
	`maven-publish`
	id("dev.yumi.gradle.licenser")
}

// Seriously, you should not worry about it, definitely not a hack.
// https://github.com/gradle/gradle/issues/15383#issuecomment-779893192
val libs = the<LibrariesForLibs>()
Constants.finalizeInit(libs)

lambdamcdev {
	manifests {
		fmj {
			withName(Constants.PRETTY_NAME)
				.withDescription(Constants.DESCRIPTION)
				.withAuthors(Constants.AUTHORS)
				.withContributors(Constants.CONTRIBUTORS)
				.withContact {
					it.withHomepage(Constants.PROJECT_LINK)
						.withSources(Constants.SOURCES_LINK)
						.withIssues("${Constants.SOURCES_LINK}/issues")
				}
				.withLicense(Constants.LICENSE)
				.withIcon("assets/${Constants.NAMESPACE}/icon.png")
				.withEnvironment("client")
				.withDepend("fabricloader", ">=${libs.versions.fabric.loader.get()}")
				.withDepend("minecraft", project.property("fabric_mc_constraints").toString())
				.withDepend("java", ">=${project.property("java_version")}")
				.withModMenu {
					it.withCurseForge("https://www.curseforge.com/minecraft/mc-mods/lambdynamiclights")
						.withDiscord("https://discord.lambdaurora.dev/")
						.withGitHubReleases("${Constants.SOURCES_LINK}/releases")
						.withModrinth("https://modrinth.com/mod/lambdynamiclights")
						.withLink("modmenu.bluesky", "https://bsky.app/profile/lambdaurora.dev")
						.withLink("modmenu.donate", "https://donate.lambdaurora.dev/")
				}
		}
	}
}

dependencies {
	@Suppress("UnstableApiUsage")
	mappings(loom.layered {
		officialMojangMappings()
		// Parchment is currently broken when used with the hacked mojmap layer due to remapping shenanigans.
		//parchment("org.parchmentmc.data:parchment-${Constants.getMcVersionString()}:${libs.versions.mappings.parchment.get()}@zip")
		mappings("dev.lambdaurora:yalmm:${libs.versions.minecraft.get()}+build.${libs.versions.mappings.yalmm.get()}")
	})

	api(libs.yumi.commons.event) {
		// Exclude Minecraft and loader-provided libraries.
		exclude(group = "org.slf4j")
		exclude(group = "org.ow2.asm")
	}
}

tasks.jar {
	from(rootProject.file("LICENSE")) {
		rename { "${it}_${Constants.NAME}" }
	}
}

license {
	rule(rootProject.file("metadata/HEADER"))
}

publishing {
	repositories {
		mavenLocal()
		maven {
			name = "BuildDirLocal"
			url = uri("${rootProject.layout.buildDirectory.get()}/repo")
		}

		val ldlMaven = System.getenv("LDL_MAVEN")
		if (ldlMaven != null) {
			maven {
				name = "LambDynamicLightsMaven"
				url = uri(ldlMaven)
				credentials {
					username = (project.findProperty("gpr.user") as? String) ?: System.getenv("MAVEN_USERNAME")
					password = (project.findProperty("gpr.key") as? String) ?: System.getenv("MAVEN_PASSWORD")
				}
			}
		}
	}
}
