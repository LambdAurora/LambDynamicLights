rootProject.name = "lambdynamiclights"

pluginManagement {
	repositories {
		maven {
			name = "Fabric"
			url = uri("https://maven.fabricmc.net/")
		}
		gradlePluginPortal()
	}
}

includeBuild("build_logic")
include("api")
