package json.schema.codegen.task

import kotlinx.serialization.json.Json
import org.gradle.api.DefaultTask
import org.gradle.api.provider.Property
import org.gradle.api.tasks.OutputDirectory
import java.io.File

abstract class GenerateJetStreamClasses : DefaultTask() {
    init {
        group = "build"
        outputs.upToDateWhen { false }
    }

    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
    }

    private val cl: ClassLoader = GenerateJetStreamClasses::class.java.classLoader

    @get:OutputDirectory
    abstract val outputDirectory: Property<File>
}
