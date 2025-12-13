package com.example.docker.utils

import com.example.docker.data.ServiceTemplate

object YamlGenerator {
    fun generateYaml(template: ServiceTemplate): String {
        val sb = StringBuilder()
        sb.append("version: '3.8'\n")
        sb.append("services:\n")
        sb.append("  ${template.name}:\n")
        sb.append("    image: ${template.image}\n")

        if (template.ports.isNotBlank()) {
            sb.append("    ports:\n")
            template.ports.split(",").forEach { port ->
                sb.append("      - \"${port.trim()}\"\n")
            }
        }

        if (template.environment.isNotBlank()) {
            sb.append("    environment:\n")
            template.environment.split(",").forEach { env ->
                sb.append("      - ${env.trim()}\n")
            }
        }

        if (template.volumes.isNotBlank()) {
            sb.append("    volumes:\n")
            template.volumes.split(",").forEach { vol ->
                sb.append("      - ${vol.trim()}\n")
            }
        }

        return sb.toString()
    }
}

