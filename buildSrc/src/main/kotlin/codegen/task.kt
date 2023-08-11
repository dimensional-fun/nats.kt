package codegen

import com.squareup.kotlinpoet.ANY
import com.squareup.kotlinpoet.FileSpec
import json.schema.JsonSchema
import kotlinx.serialization.json.Json
import org.gradle.api.DefaultTask
import org.gradle.api.provider.Property
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.TaskAction
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

    @TaskAction
    fun execute() {
        val schemas = cl.getResourceAsStream("schemas.txt")
            ?.bufferedReader()
            ?: error("Unable to read schemas")

        schemas.useLines { lines ->
            for (schemaIdentifier in lines) {
                if ("jetstream" !in schemaIdentifier) continue

                println("Generated classes for: $schemaIdentifier")

                val schema = cl.getResourceAsStream("schemas/$schemaIdentifier.json")
                    ?.bufferedReader()
                    ?.readText()
                    ?.let { json.decodeFromString(JsonSchema.serializer(), it) }
                    ?: error("Unable to find json file for: $schemaIdentifier")

                schema.createFileSpec(schemaIdentifier).generate()
            }
        }
    }


    fun FileSpec.generate() {
        val directory = outputDirectory.get()
        directory.mkdirs()
        writeTo(directory.toPath())
    }
}