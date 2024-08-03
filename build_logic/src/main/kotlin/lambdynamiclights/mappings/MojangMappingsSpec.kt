package lambdynamiclights.mappings

import net.fabricmc.loom.api.mappings.layered.MappingContext
import net.fabricmc.loom.api.mappings.layered.spec.MappingsSpec
import net.fabricmc.loom.util.download.DownloadException
import java.io.UncheckedIOException

@Suppress("UnstableApiUsage")
internal data class MojangMappingsSpec(val nameSyntheticMembers: Boolean) : MappingsSpec<MojangMappingLayer?> {
	override fun createLayer(context: MappingContext): MojangMappingLayer {
		val versionInfo = context.minecraftProvider().versionInfo
		val clientDownload = versionInfo.download(MANIFEST_CLIENT_MAPPINGS)
		val serverDownload = versionInfo.download(MANIFEST_SERVER_MAPPINGS)

		if (clientDownload == null) {
			throw RuntimeException("Failed to find official mojang mappings for " + context.minecraftVersion())
		}

		val clientMappings = context.workingDirectory("mojang").resolve("client.txt")
		val serverMappings = context.workingDirectory("mojang").resolve("server.txt")

		try {
			context.download(clientDownload.url())
				.sha1(clientDownload.sha1())
				.downloadPath(clientMappings)

			context.download(serverDownload.url())
				.sha1(serverDownload.sha1())
				.downloadPath(serverMappings)
		} catch (e: DownloadException) {
			throw UncheckedIOException("Failed to download mappings", e)
		}

		return MojangMappingLayer(
			clientMappings,
			serverMappings,
			nameSyntheticMembers,
			context.intermediaryTree().get(),
			context.logger
		)
	}

	companion object {
		// Keys in dependency manifest
		private const val MANIFEST_CLIENT_MAPPINGS = "client_mappings"
		private const val MANIFEST_SERVER_MAPPINGS = "server_mappings"
	}
}
