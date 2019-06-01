package com.apurebase.arkenv.feature

import com.apurebase.arkenv.Arkenv
import com.apurebase.arkenv.argument
import com.apurebase.arkenv.mainArgument
import com.apurebase.arkenv.test.MockSystem
import com.apurebase.arkenv.test.expectThat
import com.apurebase.arkenv.test.parse
import org.amshove.kluent.shouldEqual
import org.amshove.kluent.shouldThrow
import org.junit.jupiter.api.Test
import strikt.assertions.isEqualTo

internal class MainArgumentTest {

    private class Ark : Arkenv() {
        val main: String by mainArgument()
        val extra: String by argument("-e", "--extra")
    }

    @Test fun `mixed main and normal`() {
        val args = Ark().parse("-e", "import", "abc")
        args.main shouldEqual "abc"
        args.extra shouldEqual "import"
    }

    @Test fun `mixed main and env`() {
        MockSystem("EXTRA" to "import")
        Ark()
            .parse("abc")
            .expectThat {
                get { main }.isEqualTo("abc")
                get { extra }.isEqualTo("import")
            }
    }

    @Test fun `env before main`() {
        val ark = object : Arkenv() {
            val extra: String by argument("-e", "--extra")
            val main: String by mainArgument {
                defaultValue = { "default" }
            }
        }
        MockSystem("EXTRA" to "import")
        ark
            .parse("abc")
            .expectThat {
                get { main }.isEqualTo("abc")
                get { extra }.isEqualTo("import")
            }
    }

    @Test fun `no main argument passed`() {
        { Ark().main } shouldThrow IllegalArgumentException::class
        { Ark().parse("-e", "import") } shouldThrow IllegalArgumentException::class
    }

    @Test fun `main should not eat unused args`() {
        val ark = object : Arkenv() {
            val main: Int by mainArgument()
        }
        ark.parse("-b", "99").expectThat {
            get { main }.isEqualTo(99)
        }
    }
}
