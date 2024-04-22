import codegen.GenerateJetStreamClasses

with(kotlin.sourceSets["commonMain"]) {
    kotlin.srcDir("src/commonGenerated")

    dependencies {
        api(projects.natsCore)
    }
}

tasks.create<GenerateJetStreamClasses>("generateJetStreamClasses") {
    outputDirectory = file("src/commonGenerated")
}