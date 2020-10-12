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
        schemaStore.clear()
        schemas.forEach {
            schemaStore[it.name] = it
        }

        // Fixup the parent references
        schemaStore.forEach {
            val parent = if (it.value.extends != null) schemaStore[it.value.extends] else null
            schemaStore[it.key] = it.value.copy(parent = parent)
        }
    }

    private val schemaStore: MutableMap<String, Schema> = HashMap<String, Schema>()
}