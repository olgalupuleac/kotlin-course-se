package ru.hse.spb

import org.junit.Assert.assertEquals
import org.junit.Test
import kotlin.test.assertFailsWith

class TestSource {
    @Test
    fun simpleDoc() {
        val expected = """|\documentclass{beamer}
        |\begin{document}
        |\end{document}
""".trimMargin()
        val actual = document {
            documentClass("beamer")
        }.toString()
        assertEquals(expected, actual)
    }

    @Test
    fun simpleDocWithOptions() {
        val expected = """|\documentclass[option1,option2]{beamer}
        |\begin{document}
        |\end{document}
""".trimMargin()
        val actual = document {
            documentClass("beamer", "option1", "option2")
        }.toString()
        assertEquals(expected, actual)
    }

    @Test
    fun docWithPackages() {
        val expected = """|\documentclass[option1,option2]{beamer}
        |\usepackage{color}
        |\usepackage[big]{layaureo}
        |\begin{document}
        |\end{document}
        """.trimMargin()
        val actual = document {
            documentClass("beamer", "option1", "option2")
            usePackage("color")
            usePackage("layaureo", "big")
        }.toString()
        assertEquals(expected, actual)
    }

    @Test
    fun docWithText() {
        val expected = """|\documentclass[option1,option2]{beamer}
        |\usepackage{color}
        |\begin{document}
        |  text1
        |  text2
        |\end{document}
        """.trimMargin()
        val actual = document {
            documentClass("beamer", "option1", "option2")
            usePackage("color")
            +"text1"
            +"text2"
        }.toString()
        assertEquals(expected, actual)
    }

    @Test
    fun docWithBlocksInside() {
        val expected = """|\documentclass[option1,option2]{beamer}
        |\usepackage{color}
        |\begin{document}
        |  text1
        |  \begin{math}
        |    x^2 + y^2 = z^2
        |    \begin{center}
        |      \frac{1}{2}
        |    \end{center}
        |  \end{math}
        |  text2
        |  \begin{flushright}
        |    text3
        |  \end{flushright}
        |\end{document}
        """.trimMargin()
        val actual = document {
            documentClass("beamer", "option1", "option2")
            usePackage("color")
            +"text1"
            math {
                +"x^2 + y^2 = z^2"
                center {
                    +"\\frac{1}{2}"
                }
            }
            +"text2"
            flushRight {
                +"text3"
            }
        }.toString()
        assertEquals(expected, actual)
    }

    @Test
    fun docWithItems() {
        val expected = """|\documentclass[option1,option2]{beamer}
        |\begin{document}
        |  \begin{itemize}
        |    \item[1]
        |      i1
        |    \item[2]
        |      i2
        |  \end{itemize}
        |\end{document}
        """.trimMargin()
        val actual = document {
            documentClass("beamer", "option1", "option2")
            itemize {
                item("1") {
                    +"i1"
                }
                item("2") {
                    +"i2"
                }
            }
        }.toString()
        assertEquals(expected, actual)
    }


    @Test
    fun docEnumerateWithOptions() {
        val expected = """|\documentclass[option1,option2]{beamer}
        |\begin{document}
        |  \begin{enumerate}[label={\alph*)},font={\color{red!50!black}\bfseries}]
        |    \item
        |      i1
        |    \item
        |      i2
        |  \end{enumerate}
        |\end{document}
        """.trimMargin()
        val actual = document {
            documentClass("beamer", "option1", "option2")
            enumerate("label={\\alph*)}", "font={\\color{red!50!black}\\bfseries}") {
                item {
                    +"i1"
                }
                item {
                    +"i2"
                }
            }
        }.toString()
        assertEquals(expected, actual)
    }

    @Test
    fun docWithNoDocumentClass() {
        assertFailsWith(
                exceptionClass = TeXDslException::class,
                block = {
                    document {
                    }.toString()
                },
                message = "Document class is not specified"
        )
    }

    @Test
    fun docWithTwoDocumentClasses() {
        assertFailsWith(
                exceptionClass = TeXDslException::class,
                block = {
                    document {
                        documentClass("1")
                        documentClass("2")
                    }.toString()
                },
                message = "Document class is specified more than once"
        )
    }

    @Test
    fun packageInsideBlock() {
        assertFailsWith(
                exceptionClass = TeXDslException::class,
                block = {
                    document {
                        documentClass("1")
                        +"text"
                        usePackage("package")
                    }.toString()
                },
                message = "Package declaration can be only in preamble"
        )
    }

    @Test
    fun framesAndCustomTags() {
        val expected = """|\documentclass{beamer}
|\usepackage[russian]{babel}
|\begin{document}
|  \begin{frame}[arg1=arg2]
|    \begin{itemize}
|      \item
|        1 text
|      \item
|        2 text
|    \end{itemize}
|    \begin{pyglist}[language=kotlin]
|      a = 1
|    \end{pyglist}
|  \end{frame}
|\end{document}""".trimMargin()

        val actual = document {
            documentClass("beamer")
            usePackage("babel", "russian" /* varargs */)
            frame("frametitle", "arg1" to "arg2") {
                itemize {
                    for (row in listOf("1", "2")) {
                        item { +"$row text" }
                    }
                }
                customTag("pyglist", "language" to "kotlin") {
                    +"a = 1"
                }
            }
        }.toString()
        assertEquals(expected, actual)
    }
}