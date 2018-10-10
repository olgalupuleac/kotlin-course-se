package ru.hse.spb

import java.io.OutputStream

@DslMarker
annotation class TeXMarker

interface DslBuffer {
    fun append(string: String)
}

class DslStringBuffer : DslBuffer {
    private val stringBuilder = StringBuilder()
    override fun append(string: String) {
        stringBuilder.append(string)
    }

    override fun toString(): String {
        return stringBuilder.toString()
    }
}

class TeXDslException(msg: String) : Exception(msg)

class DslStreamBuffer(private val outputStream: OutputStream) : DslBuffer {
    override fun append(string: String) {
        outputStream.write(string.toByteArray())
    }
}

/**
 * Interface which represents an element in TeX.
 */
interface Element {
    /**
     * The default implementation, the inheritors have to override toString method.
     */
    fun render(buffer: DslBuffer, indent: String) {
        buffer.append("$indent${toString()}")
    }

    /**
     * Presents an options in TeX format: [option1, option2, option3]
     * or returns an empty string.
     */
    fun optionsToString(vararg options: String): String {
        return if (options.isNotEmpty()) {
            options.joinToString(prefix = "[", postfix = "]", separator = ",")
        } else {
            ""
        }
    }
}

abstract class DeclarationElement(private val type: String, private val name: String, private vararg val options: String) : Element {
    override fun toString(): String {
        return "\\$type${optionsToString(*options)}{$name}\n"
    }
}

class DocumentClass(name: String, vararg options: String)
    : DeclarationElement("documentclass", name, *options)

class Package(name: String, vararg options: String)
    : DeclarationElement("usepackage", name, *options)

class TextElement(private val text: String) : Element {
    override fun toString(): String {
        return "$text\n"
    }
}

@TeXMarker
abstract class ElementWithText() : Element {
    val children = arrayListOf<Element>()
    operator fun String.unaryPlus() {
        children.add(TextElement(this))
    }

    override fun render(buffer: DslBuffer, indent: String) {
        for (c in children) {
            c.render(buffer, indent + "  ")
        }
    }
}

abstract class Block(private val name: String, private val isInner: Boolean = true, private vararg val options: String) : ElementWithText() {
    companion object {
        const val BEGIN = "\\begin"
        const val END = "\\end"

        fun pairOfOptionsToString(vararg options: Pair<String, String>): String {
            return options.joinToString(separator = ",", transform = { "${it.first}=${it.second}" })
        }
    }

    protected fun <T : Element> initElement(element: T, init: T.() -> Unit): T {
        element.init()
        children.add(element)
        return element
    }

    override fun render(buffer: DslBuffer, indent: String) {
        buffer.append("$indent$BEGIN{$name}${optionsToString(*options)}\n")
        super.render(buffer, indent)
        buffer.append("$indent$END{$name}")
        if (isInner) {
            buffer.append("\n")
        }
    }

    fun itemize(vararg options: String, init: Itemize.() -> Unit): Itemize = initElement(Itemize(*options), init)

    fun enumerate(vararg options: String, init: Enumerate.() -> Unit): Enumerate = initElement(Enumerate(*options), init)

    fun math(init: Math.() -> Unit): Math = initElement(Math(), init)

    fun flushLeft(init: LeftAlignment.() -> Unit): LeftAlignment = initElement(LeftAlignment(), init)

    fun flushRight(init: RightAlignment.() -> Unit): RightAlignment = initElement(RightAlignment(), init)

    fun center(init: CenterAlignment.() -> Unit): CenterAlignment = initElement(CenterAlignment(), init)

    fun customTag(
            name: String, vararg options: String, init: CustomTag.() -> Unit
    ) = initElement(CustomTag(name, *options), init)

    fun customTag(
            name: String, vararg options: Pair<String, String>, init: CustomTag.() -> Unit
    ) = initElement(CustomTag(name, pairOfOptionsToString(*options)), init)

    fun frame(frameTitle: String? = null, vararg options: String, init: Frame.() -> Unit
    ) = initElement(Frame(frameTitle,  *options), init)

    fun frame(frameTitle: String? = null, vararg options: Pair<String, String>, init: Frame.() -> Unit
    ) = initElement(Frame(frameTitle,  pairOfOptionsToString(*options)), init)
}

abstract class List(name: String, vararg options: String) : Block(name, true, *options) {
    fun item(vararg options: String, init: Item.() -> Unit): Item = initElement(Item(*options), init)
}

class Itemize(vararg options: String) : List("itemize", *options)

class Enumerate(vararg options: String) : List("enumerate", *options)

class Frame(val frameTitle: String?, vararg options: String) : Block("frame", true, *options) {
    override fun render(buffer: DslBuffer, indent: String) {
        frameTitle ?: children.add(FrameTitle(frameTitle!!))
        super.render(buffer, indent)
    }
}

class FrameTitle(title: String) : DeclarationElement("frametitle", title)

class CustomTag(name: String, vararg options: String) : Block(name, true, *options)

class Item(private vararg val options: String) : ElementWithText() {
    override fun render(buffer: DslBuffer, indent: String) {
        buffer.append("$indent\\item${optionsToString(*options)}\n")
        super.render(buffer, indent)
    }
}

class Math : Block("math")

class LeftAlignment : Block("flushleft")

class RightAlignment : Block("flushright")

class CenterAlignment : Block("center")

class Document : Block("document", false) {
    private var documentClass: DocumentClass? = null
    private var packages: MutableList<Package> = mutableListOf<Package>()
    fun documentClass(name: String, vararg options: String) {
        if (documentClass != null) {
            throw TeXDslException("Document class is specified more than once")
        }
        documentClass = DocumentClass(name, *options)
    }

    override fun render(buffer: DslBuffer, indent: String) {
        documentClass ?: throw TeXDslException("Document class is not specified")
        documentClass!!.render(buffer, indent)
        for (pack in packages) {
            pack.render(buffer, indent)
        }
        super.render(buffer, indent)
    }

    fun usePackage(name: String, vararg options: String) {
        if (children.isNotEmpty()) {
            throw TeXDslException("Package declaration can be only in preamble")
        }
        packages.add(Package(name, *options))
    }

    fun toOutputStream(out: OutputStream) {
        render(DslStreamBuffer(out), "")
    }

    override fun toString(): String {
        val buffer = DslStringBuffer()
        render(buffer, "")
        return buffer.toString()
    }
}

fun document(init: Document.() -> Unit): Document {
    val document = Document()
    document.init()
    return document
}