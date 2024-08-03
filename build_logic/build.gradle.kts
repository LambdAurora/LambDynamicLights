import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
	`java-gradle-plugin`
	`kotlin-dsl`
}

val javaVersion = 21

repositories {
	gradlePluginPortal()
	maven {
		name = "Fabric"
		url = uri("https://maven.fabricmc.net/")
	}
}

dependencies {
	implementation(libs.gradle.licenser)
	implementation(libs.gradle.loom)
	implementation(libs.mappingio)

	// A bit of a hack you definitely should not worry about.
	// https://github.com/gradle/gradle/issues/15383#issuecomment-779893192
	implementation(files(libs.javaClass.superclass.protectionDomain.codeSource.location))
}

java {
	sourceCompatibility = JavaVersion.toVersion(javaVersion)
	targetCompatibility = JavaVersion.toVersion(javaVersion)
}

kotlin {
	compilerOptions {
		jvmTarget = JvmTarget.fromTarget(javaVersion.toString())
	}
}
