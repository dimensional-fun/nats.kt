package codegen

import org.gradle.configurationcache.extensions.capitalized

val String.schemaName: String get() = substringAfterLast('.').normalized.capitalized()

val String.normalized: String
    get() {
        val parts = split('_')
        return parts[0] + parts.drop(1).joinToString("", transform = String::capitalized)
    }