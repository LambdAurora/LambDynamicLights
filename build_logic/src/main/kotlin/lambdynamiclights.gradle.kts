import lambdynamiclights.Constants
import lambdynamiclights.mappings.MojangMappingsSpec
import org.gradle.accessors.dm.LibrariesForLibs

plugins {
	id("fabric-loom")
	`java-library`
	`maven-publish`
	id("dev.yumi.gradle.licenser")
}

// Seriously you should not worry about it, definitely not a hack.
// https://github.com/gradle/gradle/issues/15383#issuecomment-779893192
val libs = the<LibrariesForLibs>()
Constants.finalizeInit(libs)

group = Constants.GROUP
version = "${Constants.VERSION}+${Constants.mcVersion()}"

loom {
	runtimeOnlyLog4j = true
}

dependencies {
	minecraft(libs.minecraft)
	@Suppress("UnstableApiUsage")
	mappings(loom.layered {
		addLayer(MojangMappingsSpec(false))
		parchment("org.parchmentmc.data:parchment-${Constants.getMcVersionString()}:${libs.versions.mappings.parchment.get()}@zip")
		mappings("dev.lambdaurora:yalmm:${Constants.mcVersion()}+build.${libs.versions.mappings.yalmm.get()}")
	})
	modImplementation(libs.fabric.loader)

	api(libs.yumi.commons.event)
}

java {
	sourceCompatibility = JavaVersion.toVersion(Constants.JAVA_VERSION)
	targetCompatibility = JavaVersion.toVersion(Constants.JAVA_VERSION)

	withSourcesJar()
}

tasks.withType<JavaCompile>().configureEach {
	options.encoding = "UTF-8"

	options.release.set(Constants.JAVA_VERSION)
}

tasks.jar {
	from(rootProject.file("LICENSE")) {
		rename { "${it}_${Constants.NAME}" }
	}
}

license {
	rule(rootProject.file("HEADER"))
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
