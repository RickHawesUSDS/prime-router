package gov.cdc.prime.router

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory
import com.fasterxml.jackson.module.kotlin.KotlinModule
import com.fasterxml.jackson.module.kotlin.readValue
import java.io.File
import java.io.InputStream

object SchemaManager {
    const val defaultDirectory = "router_directory/schema_catalog.yml"
    val schemas get() = schemaStore
    val flattenSchemas get() = flattenStore

    data class Catalog(
        val schema_catalog: List<Schema>
    )

    // Load the schema catalog either from the default location or from the passed location
    fun loadSchemaCatalog(catalog: InputStream? = null) {
        val loadingCatalog = catalog ?: File(defaultDirectory).inputStream()
        val mapper = ObjectMapper(YAMLFactory()).registerModule(KotlinModule())
        val schemaCatalog = mapper.readValue<Catalog>(loadingCatalog)
        loadSchemas(schemaCatalog.schema_catalog)
    }

    fun loadSchemas(schemas: List<Schema>) {
        fun findAllElements(name: String, list: List<Schema>) : List<Element> {
            val schema: Schema = list.find { it.name == name } ?: error("invalid extend name: $name")
            return if (schema.extends != null)
                findAllElements(schema.extends, list) + schema.elements
            else
                schema.elements
        }

        schemaStore.clear()
        schemas.forEach {
            schemaStore[it.name] = it
        }

        flattenStore.clear()
        schemas.forEach {
            flattenStore[it.name] = it.copy(elements = findAllElements(it.name, schemas))
        }
    }

    private val schemaStore: MutableMap<String, Schema> = HashMap<String, Schema>()
    private val flattenStore: MutableMap<String, Schema> = HashMap<String, Schema>()
}