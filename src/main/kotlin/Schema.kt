package gov.cdc.prime.router

data class Schema(
    val name: String,
    val elements: List<Element>,
    //val order: List<String>?, coming soon reorder the elements
    val extends: String? = null,
    val parent: Schema? = null, // fixup after loading into manager
 ) {
    val allElements: List<Element>
        get() { return if (parent == null) elements else parent.allElements + elements }

    fun findElement(name: String) : Element? {
        return elements.find { element -> element.name == name } ?: parent?.findElement(name)
    }

    fun unionElements(otherSchema: Schema) : List<Element> {
        return elements + otherSchema.diffElements(this)
    }

    fun diffElements(otherSchema: Schema) : List<Element> {
        // Kind of brute force, but simple
        return elements.filter { otherSchema.findElement(it.name) == null }
    }
}