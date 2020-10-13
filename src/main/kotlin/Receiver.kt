package gov.cdc.prime.router

data class Receiver(
    val name: String,
    val description: String = "",
    val address: String = "",
    val topics: List<Topic> = emptyList(),
) {
    data class Topic(
        val patterns: Map<String, String> = emptyMap(),
        val transforms: Map<String, String> = emptyMap(),
        val format: TopicFormat = TopicFormat.CSV
    )

    enum class TopicFormat {
        CSV,
        HL7,
        FHIR
    }
}