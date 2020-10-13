package gov.cdc.prime.router

data class Receiver(
    val name: String,
    val description: String,
    val address: String,
    val topics: List<Topic>,
) {
    data class Topic(
        val patterns: Map<String, String>,
        val transforms: Map<String, String>,
        val format: TopicFormat
    ) {

    }

    enum class TopicFormat {
        CSV,
        HL7,
        FHIR
    }
}