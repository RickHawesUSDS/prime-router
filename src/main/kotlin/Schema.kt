package gov.cdc.prime.router

// A schema defines
data class Schema(
    val name: String, // Name should include version
    val elements: List<Element> = emptyList(),
    val extends: String? = null,
    val parent: Schema? = null, // fixup after loading into manager
) {
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

    val allElements: List<Element>
        get() {
            return if (parent == null) elements else parent.allElements + elements
        }

    fun findElement(name: String): Element? {
        return elements.find { it.name == name } ?: parent?.findElement(name)
    }

    fun unionElements(otherSchema: Schema): List<Element> {
        return elements + otherSchema.diffElements(this)
    }

    fun diffElements(otherSchema: Schema): List<Element> {
        // Kind of brute force, but simple
        return elements.filter { otherSchema.findElement(it.name) == null }
    }
}