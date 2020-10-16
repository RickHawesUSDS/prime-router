package gov.cdc.prime.router

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory
import com.fasterxml.jackson.module.kotlin.KotlinModule
import com.fasterxml.jackson.module.kotlin.readValue
import java.io.File
import java.io.InputStream

object DirectoryManager {
    const val defaultReceivers = "router_directory/recievers.yml"

    val schemas get() = schemaStore
    val receivers get() = receiversStore
    private val mapper = ObjectMapper(YAMLFactory()).registerModule(KotlinModule())

    data class ReceiversList(
        val receivers: List<Receiver>
    )

    fun loadReceiversList(receiversStream: InputStream? = null) {
        val loadingStream = receiversStream ?: File(defaultReceivers).inputStream()
        val receiversList = mapper.readValue<ReceiversList>(loadingStream)
        loadReceivers(receiversList.receivers)
    }

    fun loadReceivers(receivers: List<Receiver>) {
        receiversStore.clear()
        receiversStore.putAll(receivers.map { it.name to it }.toMap())
    }

    private val schemaStore: MutableMap<String, Schema> = HashMap<String, Schema>()
    private val receiversStore: MutableMap<String, Receiver> = HashMap<String, Receiver>()
}