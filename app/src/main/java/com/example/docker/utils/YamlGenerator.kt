package com.example.docker.utils

import com.example.docker.data.ServiceTemplate
import java.util.Locale

object YamlGenerator {

    fun generateCombinedYaml(templates: List<ServiceTemplate>): String {
        if (templates.isEmpty()) return ""

        val sb = StringBuilder()
        sb.append("version: '3.8'\n")
        sb.append("services:\n")

        val usedNames = mutableSetOf<String>()

        for (template in templates) {
            val serviceName = getUniqueServiceName(template.name, usedNames)
            usedNames.add(serviceName)

            sb.append("  $serviceName:\n")
            sb.append("    image: ${template.image}\n")

            if (template.ports.isNotBlank()) {
                sb.append("    ports:\n")
                // Assuming ports are comma or newline separated, or just one string
                // The user prompt says "8080:80" in the example.
                // Let's handle comma separated just in case, or just print it if it's a single line.
                // If the user inputs "8080:80", we output "- \"8080:80\""
                val portsList = template.ports.split(",", "\n").map { it.trim() }.filter { it.isNotEmpty() }
                for (port in portsList) {
                    sb.append("      - \"$port\"\n")
                }
            }

            if (template.volumes.isNotBlank()) {
                sb.append("    volumes:\n")
                val volList = template.volumes.split(",", "\n").map { it.trim() }.filter { it.isNotEmpty() }
                for (vol in volList) {
                    sb.append("      - $vol\n")
                }
            }

            val envMap = EnvVarConverter.jsonToMap(template.envVars)
            if (envMap.isNotEmpty()) {
                sb.append("    environment:\n")
                for ((key, value) in envMap) {
                    sb.append("      - $key=$value\n")
                }
            }

            if (template.restartPolicy.isNotBlank() && template.restartPolicy != "no") {
                sb.append("    restart: ${template.restartPolicy}\n")
            }

            sb.append("\n")
        }

        return sb.toString().trim()
    }

    private fun getUniqueServiceName(rawName: String, usedNames: Set<String>): String {
        val slug = rawName.lowercase(Locale.ROOT).replace(Regex("[^a-z0-9-]"), "-").replace(Regex("-+"), "-").trim('-')
        var candidate = slug
        var counter = 1
        while (usedNames.contains(candidate)) {
            candidate = "$slug-$counter"
            counter++
        }
        return candidate
    }
}

