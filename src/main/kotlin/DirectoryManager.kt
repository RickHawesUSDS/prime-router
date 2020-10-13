package gov.cdc.prime.router

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory
import com.fasterxml.jackson.module.kotlin.KotlinModule
import com.fasterxml.jackson.module.kotlin.readValue
import java.io.File
import java.io.InputStream

object DirectoryManager {
    const val defaultSchemaCatalog = "router_directory/schema_catalog.yml"
    const val defaultReceivers = "router_directory/recievers.yml"

    val schemas get() = schemaStore
    val receivers get() = receiversStore
    val mapper = ObjectMapper(YAMLFactory()).registerModule(KotlinModule())

    data class SchemaCatalog(
        val schema_catalog: List<Schema>
    )

    data class ReceiversList(
        val receivers: List<Receiver>
    )

    // Load the schema catalog either from the default location or from the passed location
    fun loadSchemaCatalog(catalog: InputStream? = null) {
        val loadingCatalog = catalog ?: File(defaultSchemaCatalog).inputStream()
        val schemaCatalog = mapper.readValue<SchemaCatalog>(loadingCatalog)
        loadSchemas(schemaCatalog.schema_catalog)
    }

    fun loadSchemas(schemas: List<Schema>) {
        schemaStore.clear()
        schemaStore.putAll(schemas.map {it.name to it}.toMap())

        // Fixup the parent references
        schemaStore.forEach {
            val parent = if (it.value.extends != null) schemaStore[it.value.extends] else null
            schemaStore[it.key] = it.value.copy(parent = parent)
        }
    }

    fun loadReceiversList(receiversStream: InputStream? = null) {
        val loadingStream = receiversStream ?: File(defaultReceivers).inputStream()
        val receiversList = mapper.readValue<ReceiversList>(loadingStream)
        loadReceivers(receiversList.receivers)
    }

    fun loadReceivers(receivers: List<Receiver>) {
        receiversStore.clear()
        receiversStore.putAll(receivers.map {it.name to it}.toMap())
    }

    private val schemaStore: MutableMap<String, Schema> = HashMap<String, Schema>()
    private val receiversStore: MutableMap<String, Receiver> = HashMap<String, Receiver>()
}