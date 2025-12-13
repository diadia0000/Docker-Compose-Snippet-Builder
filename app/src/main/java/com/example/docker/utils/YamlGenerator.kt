package com.example.docker.utils

import com.example.docker.data.ServiceTemplate

object YamlGenerator {
    fun toYaml(template: ServiceTemplate): String {
        val sb = StringBuilder()
        sb.append("version: '3.8'\n")
        sb.append("services:\n")
        sb.append("  ${template.name}:\n")
        sb.append("    image: ${template.image}\n")

        if (template.ports.isNotBlank()) {
            sb.append("    ports:\n")
            sb.append("      - \"${template.ports}\"\n")
        }

        if (template.volumes.isNotBlank()) {
            sb.append("    volumes:\n")
            sb.append("      - ${template.volumes}\n")
        }

        if (template.envVars.isNotBlank()) {
            sb.append("    environment:\n")
            // Simple handling: split by new lines and indent
            template.envVars.lines().forEach { line ->
                if (line.isNotBlank()) {
                    sb.append("      - $line\n")
                }
            }
        }

        if (template.restartPolicy != "no") {
            sb.append("    restart: ${template.restartPolicy}\n")
        }

        return sb.toString()
    }
}

