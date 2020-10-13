package gov.cdc.prime.router

data class Element(
    val name: String,
    val type: Type = Type.TEXT,
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
    }

    enum class CodeSystem {
        NONE,
        LOINC,
        SNOMED
    }
}