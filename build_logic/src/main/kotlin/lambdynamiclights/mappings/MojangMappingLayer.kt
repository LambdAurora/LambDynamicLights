package lambdynamiclights.mappings

import net.fabricmc.loom.api.mappings.layered.MappingLayer
import net.fabricmc.loom.api.mappings.layered.MappingsNamespace
import net.fabricmc.loom.configuration.providers.mappings.intermediary.IntermediaryMappingLayer
import net.fabricmc.loom.configuration.providers.mappings.utils.DstNameFilterMappingVisitor
import net.fabricmc.mappingio.MappingVisitor
import net.fabricmc.mappingio.adapter.MappingDstNsReorder
import net.fabricmc.mappingio.adapter.MappingSourceNsSwitch
import net.fabricmc.mappingio.format.proguard.ProGuardFileReader
import net.fabricmc.mappingio.tree.MemoryMappingTree
import org.gradle.api.logging.Logger
import java.io.IOException
import java.nio.file.Files
import java.nio.file.Path
import java.util.regex.Pattern

// Based off Loom, this is required as the releases at the time of writing this buildscript have
// a flaw with the mapping layering preventing the usage of the usual MojangMappingLayer.
@Suppress("UnstableApiUsage")
internal data class MojangMappingLayer(
	val clientMappings: Path, val serverMappings: Path, val nameSyntheticMembers: Boolean,
	val intermediaryMappings: MemoryMappingTree, val logger: Logger
) : MappingLayer {
	@Throws(IOException::class)
	override fun visit(mappingVisitor: MappingVisitor) {
		val mojmap = MemoryMappingTree()

		// Filter out field names matching the pattern
		val nameFilter = DstNameFilterMappingVisitor(mojmap, SYNTHETIC_NAME_PATTERN)

		// Make official the source namespace
		val nsSwitch = MappingSourceNsSwitch(if (nameSyntheticMembers) mojmap else nameFilter, MappingsNamespace.OFFICIAL.toString())

		Files.newBufferedReader(clientMappings).use { clientBufferedReader ->
			Files.newBufferedReader(serverMappings).use { serverBufferedReader ->
				ProGuardFileReader.read(
					clientBufferedReader,
					MappingsNamespace.NAMED.toString(),
					MappingsNamespace.OFFICIAL.toString(),
					nsSwitch
				)
				ProGuardFileReader.read(
					serverBufferedReader,
					MappingsNamespace.NAMED.toString(),
					MappingsNamespace.OFFICIAL.toString(),
					nsSwitch
				)
			}
		}

		intermediaryMappings.accept(MappingDstNsReorder(mojmap, MappingsNamespace.INTERMEDIARY.toString()))

		val switch = MappingSourceNsSwitch(MappingDstNsReorder(mappingVisitor, MappingsNamespace.NAMED.toString()), MappingsNamespace.INTERMEDIARY.toString(), true)
		mojmap.accept(switch)
	}

	override fun getSourceNamespace(): MappingsNamespace {
		return MappingsNamespace.INTERMEDIARY
	}

	override fun dependsOn(): List<Class<out MappingLayer?>> {
		return listOf(IntermediaryMappingLayer::class.java)
	}

	companion object {
		private val SYNTHETIC_NAME_PATTERN: Pattern = Pattern.compile("^(access|this|val\\\$this|lambda\\$.*)\\$[0-9]+$")
	}
}
