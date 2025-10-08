package lambdynamiclights

import org.apache.commons.compress.archivers.jar.JarArchiveEntry
import org.apache.commons.compress.archivers.jar.JarArchiveInputStream
import org.apache.commons.compress.archivers.jar.JarArchiveOutputStream
import org.apache.commons.compress.archivers.zip.UnixStat
import org.apache.commons.io.IOUtils
import java.io.ByteArrayInputStream
import java.nio.charset.StandardCharsets
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.StandardOpenOption
import java.util.*

object ZipFix {
	// Please see the insane comment on ZipEntryConstants in Gradle
	// https://github.com/gradle/gradle/blob/360f9eab2f6f1595025f746a03ee5895659b0b8c/platforms/core-runtime/files/src/main/java/org/gradle/api/internal/file/archive/ZipEntryConstants.java#L39
	val ZIP_EPOCH_TIME: Long = GregorianCalendar(
		1980, Calendar.FEBRUARY,
		1, 0, 0, 0
	).getTimeInMillis();

	fun fixZip(path: Path) {
		// Cache in-memory the contents of the ZIP so we can safely write back.
		val bytes = Files.readAllBytes(path)
		JarArchiveInputStream(ByteArrayInputStream(bytes), StandardCharsets.UTF_8.name()).use { ais ->
			JarArchiveOutputStream(Files.newOutputStream(path, StandardOpenOption.TRUNCATE_EXISTING)).use { out ->
				ais.forEach {
					val entry = JarArchiveEntry(it.name)
					entry.time = ZIP_EPOCH_TIME
					if (!it.isDirectory) {
						entry.method = it.method
						if (it.size != -1L) entry.size = it.size
					}

					entry.unixMode = if (entry.isDirectory) {
						UnixStat.DIR_FLAG or 0b111101101
					} else {
						UnixStat.FILE_FLAG or 0b110100100
					}

					out.putArchiveEntry(entry)

					if (!entry.isDirectory) {
						IOUtils.copy(ais, out)
					}

					out.closeArchiveEntry()
				}
			}
		}
	}
}
