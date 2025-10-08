import lambdynamiclights.Constants

plugins {
	id("lambdynamiclights")
}

val prettyName = "${Constants.PRETTY_NAME} (API)"

base.archivesName.set(Constants.NAME + "-api")

dependencies {
	include(libs.yumi.commons.core)
	include(libs.yumi.commons.collections)
	include(libs.yumi.commons.event)
}

lambdamcdev {
	namespace = Constants.NAMESPACE + "_api"

	manifests {
		val fmj = fmj {
			withNamespace(Constants.NAMESPACE + "_api")
				.withName(prettyName)
				.withDescription(Constants.API_DESCRIPTION)
				.withModMenu {
					it.withBadges("library")
						.withParent(Constants.NAMESPACE, Constants.PRETTY_NAME) { parent ->
							parent.withDescription(Constants.DESCRIPTION)
								.withIcon("assets/${Constants.NAMESPACE}/icon.png")
						}
				}
		}

		nmt {
			fmj.copyTo(this)
			withLoaderVersion("[2,)")
			withDepend("minecraft", project.property("neoforge_mc_constraints").toString())
		}

		fmj {
			withDepend("yumi-commons-core", "^${libs.versions.yumi.commons.get()}")
			withDepend("yumi-commons-collections", "^${libs.versions.yumi.commons.get()}")
			withDepend("yumi-commons-event", "^${libs.versions.yumi.commons.get()}")
		}
	}

	setupJarJarCompat()
}

val generateNmt = tasks.named("generateNmt")

tasks.generateFmj.configure {
	dependsOn(generateNmt)
}

val mojmap = lambdamcdev.setupMojmapRemapping()
val remapMojmap = mojmap.registerRemap(tasks.remapJar) {}
mojmap.setJarArtifact(remapMojmap)
val remapMojmapSources = mojmap.registerSourcesRemap(tasks.remapSourcesJar) {}
mojmap.setSourcesArtifact(remapMojmapSources)

tasks.runClient {
	this.enabled = false
}

// Configure the maven publication.
publishing {
	publications {
		create<MavenPublication>("mavenJava") {
			from(components["java"])

			artifactId = "lambdynamiclights-api"

			pom {
				name.set(prettyName)
				description.set(Constants.API_DESCRIPTION)
			}
		}
	}
}
