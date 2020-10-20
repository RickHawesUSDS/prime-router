package gov.cdc.prime.router

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory
import com.fasterxml.jackson.module.kotlin.KotlinModule
import com.fasterxml.jackson.module.kotlin.readValue
import java.io.File
import java.io.FilenameFilter

// A schema defines
data class Schema(
    val name: String, // Name should include version
    val topic: String,
    val elements: List<Element> = emptyList(),
) {
    data class Element(
        val name: String,
        val type: Type = Type.TEXT,
        val dateFormat: String = "",
        val codeSystem: CodeSystem = CodeSystem.NONE,
        val code: String = "",
        val optional: Boolean = true,
        val default: String? = "",
        val pii: Boolean = false,
        val validation: String? = null,
        val hl7_field: String? = null,
        val hl7_operation: String? = null,
        val hl7_validation: String? = null,
        val hl7_template: String? = null,
    ) {
        enum class Type {
            TEXT,
            NUMERIC,
            DATE,
            CODED,
            // Add types defined by HL7 with specific validation rules including DLN, SSN, ADDRESS,
        }

        enum class CodeSystem {
            NONE,
            LOINC,
            SNOMED
        }
    }

    data class Mapping(
        val toSchema: Schema,
        val fromSchema: Schema,
        val useFromName: Map<String, String>,
        val useDefault: Set<String>,
        val missing: Set<String>
    )

    fun findElement(name: String): Element? {
        return elements.find { it.name == name }
    }

    fun buildMapping(toSchema: Schema): Mapping {
        if (toSchema.topic != this.topic) error("Trying to match schema with different topics")
        val useFromName = HashMap<String, String>()
        val useDefault = HashSet<String>()
        val missing = HashSet<String>()
        toSchema.elements.forEach {
            val mappedName = findMatchingElement(it)
            if (mappedName != null) {
                useFromName[it.name] = mappedName
            } else {
                if (it.optional) {
                    useDefault.add(it.name)
                } else {
                    missing.add(it.name)
                }
            }
        }
        return Mapping(toSchema, this, useFromName, useDefault, missing)
    }

    private fun findMatchingElement(matchElement: Element): String? {
        // TODO: Much more can be done here
        val matchName = normalizeName(matchElement.name)
        for (element in elements) {
            if (matchName == normalizeName(element.name)) return element.name
        }
        return null
    }

    private fun normalizeName(name: String): String {
        return name.replace("_|\\s".toRegex(),"").toLowerCase()
    }

    companion object {
        val schemas: Map<String, Schema> get() = schemaStore

        private val schemaStore = HashMap<String, Schema>()
        private const val defaultCatalog = "./metadata/schemas"
        private const val schemaExtension = ".yml"
        private val mapper = ObjectMapper(YAMLFactory()).registerModule(KotlinModule())

        // Load the schema catalog either from the default location or from the passed location
        fun loadSchemaCatalog(catalog: String? = null) {
            val newSchemas = HashMap<String, Schema>()
            val rootDir = File(catalog ?: defaultCatalog)

            fun addSchemasInADirectory(dirSubPath: String) {
                val schemaExtFilter = FilenameFilter { _: File, name: String -> name.endsWith(schemaExtension) }
                val dir = File(rootDir.absolutePath, dirSubPath)
                val files = dir.listFiles(schemaExtFilter) ?: return
                files.forEach {
                    val schema = mapper.readValue<Schema>(it.inputStream())
                    val fullName = if (dirSubPath.isEmpty()) schema.name else dirSubPath + "/" + schema.name
                    newSchemas[fullName] = schema
                }

                val subDirs = dir.listFiles { file -> file.isDirectory } ?: return
                subDirs.forEach {
                    addSchemasInADirectory(if (dirSubPath.isEmpty()) it.name else dirSubPath + "/" + it.name)
                }
            }

            if (!rootDir.isDirectory) error("Expected ${rootDir.absolutePath} to be a directory")
            addSchemasInADirectory("")
            loadSchemas(newSchemas)
        }

        fun loadSchemas(schemas: Map<String, Schema>) {
            schemaStore.clear()
            schemaStore.putAll(schemas)
        }
    }
}