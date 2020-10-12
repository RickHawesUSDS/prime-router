package gov.cdc.prime.router

data class Element(
    val name: String,
    val type: Type = Type.STRING,
    val codeSystem: CodeSystem = CodeSystem.NONE,
    val code: String = "",
    val nameMap: String = "",
    val valueMap: String = "",
    val optional: Boolean = false,
    val validation: String = "",
) {
    enum class Type {
        STRING,
        NUMBER,
        TIME,
        CNE, // Coded with no exceptions
        CWE, // Coded with exceptions
    }

    enum class CodeSystem {
        NONE,
        LOINC,
        SNOMED
    }
}