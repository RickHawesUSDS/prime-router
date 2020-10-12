package gov.cdc.prime.router

data class Element(
    val name: String,
    val type: Type = Type.TEXT,
    val codeSystem: CodeSystem = CodeSystem.NONE,
    val code: String = "",
    val nameMap: String = "",
    val valueMap: String = "",
    val optional: Boolean = false,
    val validation: String = "",
) {
    enum class Type {
        TEXT,
        NUMERIC,
        DATE,
        CODED, 
    }

    enum class CodeSystem {
        NONE,
        LOINC,
        SNOMED
    }
}