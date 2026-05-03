// Top-level build file where you can add configuration options common to all sub-projects/modules.
import java.io.File

plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.compose) apply false
}

/** Load KEY=value pairs from `.env` at the repo root (comments and blank lines ignored). */
fun loadDotEnv(envFile: File): Map<String, String> {
    if (!envFile.exists()) return emptyMap()
    val out = mutableMapOf<String, String>()
    envFile.readLines().forEach { raw ->
        var line = raw.trim()
        if (line.isEmpty() || line.startsWith("#")) return@forEach
        if (line.startsWith("export ")) line = line.removePrefix("export ").trim()
        val eq = line.indexOf('=')
        if (eq <= 0) return@forEach
        val key = line.substring(0, eq).trim()
        var value = line.substring(eq + 1).trim()
        if (value.length >= 2) {
            val q = value.first()
            if ((q == '"' || q == '\'') && value.last() == q) {
                value = value.substring(1, value.length - 1)
            }
        }
        out[key] = value
    }
    return out
}

val dotEnv: Map<String, String> = loadDotEnv(rootProject.projectDir.resolve(".env"))

fun Project.resolveSupabaseProperty(envKey: String): String {
    val fromFile = dotEnv[envKey]?.trim().orEmpty().takeIf {
        it.isNotBlank() && !it.contains("YOUR_") // treat template `.env` as unset
    }
    val fromEnv = System.getenv(envKey)?.trim().orEmpty().takeIf { it.isNotBlank() }
    val fromGradle = findProperty(envKey)?.toString()?.trim().orEmpty().takeIf { it.isNotBlank() }
    return fromFile ?: fromEnv ?: fromGradle ?: ""
}

subprojects {
    extra["SUPABASE_URL"] = resolveSupabaseProperty("SUPABASE_URL")
    extra["SUPABASE_ANON_KEY"] = resolveSupabaseProperty("SUPABASE_ANON_KEY")
}