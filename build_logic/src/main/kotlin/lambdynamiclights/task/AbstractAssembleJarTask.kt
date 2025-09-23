package lambdynamiclights.task

import com.google.gson.JsonParser
import dev.lambdaurora.mcdev.util.JsonUtils
import dev.yumi.commons.function.YumiPredicates
import org.gradle.api.file.RegularFileProperty
import org.gradle.jvm.tasks.Jar
import java.nio.file.FileSystem
import java.nio.file.FileSystems
import java.nio.file.FileVisitResult
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.SimpleFileVisitor
import java.nio.file.StandardCopyOption
import java.nio.file.attribute.BasicFileAttributes
import java.util.function.Predicate
import javax.inject.Inject
import kotlin.use

abstract class AbstractAssembleJarTask @Inject constructor() : Jar() {
	protected fun openFs(jar: RegularFileProperty): FileSystem {
		return FileSystems.newFileSystem(jar.get().asFile.toPath())
	}

	protected fun copy(source: Path, target: Path, predicate: Predicate<Path>) {
		if (target.parent != null) {
			Files.createDirectories(target.parent)
		}

		if (Files.isRegularFile(source)) {
			if (predicate.test(source))
				Files.copy(source, target, StandardCopyOption.REPLACE_EXISTING)
			return
		}

		Files.walkFileTree(source, object : SimpleFileVisitor<Path>() {
			fun resolve(subSource: Path): Path {
				val relative = source.relativize(subSource)
				return target.resolve(relative)
			}

			override fun preVisitDirectory(dir: Path, attrs: BasicFileAttributes): FileVisitResult {
				if (!predicate.test(dir)) return FileVisitResult.SKIP_SUBTREE
				Files.createDirectories(this.resolve(dir))
				return FileVisitResult.CONTINUE
			}

			override fun visitFile(file: Path, attrs: BasicFileAttributes): FileVisitResult {
				if (predicate.test(file)) {
					Files.copy(file, this.resolve(file), StandardCopyOption.REPLACE_EXISTING)
				}
				return FileVisitResult.CONTINUE
			}
		})
	}

	protected fun copy(path: String, sourceFs: FileSystem, targetFs: FileSystem, predicate: Predicate<Path> = YumiPredicates.alwaysTrue()) {
		this.copy(sourceFs.getPath(path), targetFs.getPath(path), predicate)
	}

	protected fun move(source: Path, target: Path) {
		if (target.parent != null) {
			Files.createDirectories(target.parent)
		}

		if (Files.isRegularFile(source)) {
			Files.move(source, target)
			return
		}

		Files.walkFileTree(source, object : SimpleFileVisitor<Path>() {
			fun resolve(subSource: Path): Path {
				val relative = source.relativize(subSource)
				return target.resolve(relative)
			}

			override fun preVisitDirectory(dir: Path, attrs: BasicFileAttributes): FileVisitResult {
				Files.createDirectories(this.resolve(dir))
				return FileVisitResult.CONTINUE
			}

			override fun visitFile(file: Path, attrs: BasicFileAttributes): FileVisitResult {
				Files.move(file, this.resolve(file))
				return FileVisitResult.CONTINUE
			}
		})
		this.recursivelyDelete(source)
	}

	protected fun move(path: String, sourceFs: FileSystem, targetFs: FileSystem) {
		this.move(sourceFs.getPath(path), targetFs.getPath(path))
	}

	protected fun recursivelyDelete(path: Path) {
		Files.walk(path).use { walk ->
			walk.sorted(Comparator.reverseOrder())
				.forEach {
					Files.delete(it)
				}
		}
	}
}