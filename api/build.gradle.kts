import lambdynamiclights.Constants

plugins {
	id("lambdynamiclights")
}

base.archivesName.set(Constants.NAME + "-api")

// Configure the maven publication.
publishing {
	publications {
		create("mavenJava", MavenPublication::class) {
			from(components["java"])

			groupId = "$group.lambdynamiclights"
			artifactId = "lambdynamiclights-api"

			pom {
				name.set("LambDynamicLights (API)")
				description.set("API for LambDynamicLights, a mod which adds dynamic lighting to Minecraft.")
			}
		}
	}
}
