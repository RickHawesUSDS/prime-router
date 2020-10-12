package gov.cdc.prime.router

data class Message(
    val schema: Schema,
    val values: Map<String, String>
) {
    companion object {}

    fun isA(schema: Schema) : Boolean {
        return schema == this.schema;
    }

    fun validateValues() : Boolean {
        throw NotImplementedError()
    }
}