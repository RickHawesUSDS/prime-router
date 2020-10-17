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
    val elements: List<Element> = emptyList(),
    val extends: String? = null,
    val parent: Schema? = null, // fixup after loading into manager
) {
    data class Element(
        val name: String,
        val type: Type = Type.TEXT,
        val dateFormat: String = "",
        val codeSystem: CodeSystem = CodeSystem.NONE,
        val code: String = "",
        val optional: Boolean = true,
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

    val allElements: List<Element>
        get() {
            return if (parent == null) elements else parent.allElements + elements
        }

    fun findElement(name: String): Element? {
        return elements.find { it.name == name } ?: parent?.findElement(name)
    }

    fun unionElements(otherSchema: Schema): List<Element> {
        return elements + otherSchema.diffElements(this)
    }

    fun diffElements(otherSchema: Schema): List<Element> {
        // Kind of brute force, but simple
        return elements.filter { otherSchema.findElement(it.name) == null }
    }

    companion object {
        val schemas get() = schemaStore

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

            // Fixup the parent references
            schemaStore.forEach {
                val parent = if (it.value.extends != null) schemaStore[it.value.extends] else null
                schemaStore[it.key] = it.value.copy(parent = parent)
            }
        }

    }
}