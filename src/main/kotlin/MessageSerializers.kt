package gov.cdc.prime.router

object MessageSerializers {
    fun decodeCsv(schema: Schema, row: List<Map<String, String>>) : List<Message> {
        return throw NotImplementedError()
    }

    fun encodeCsv(messages: List<Message>) : List<Map<String,String>> {
        return throw NotImplementedError()
    }
}