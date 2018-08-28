package com.apurebase.arkenv

import org.amshove.kluent.shouldBe
import org.amshove.kluent.shouldBeEqualTo
import org.amshove.kluent.shouldEqual
import org.amshove.kluent.shouldThrow
import org.junit.jupiter.api.Test

class EnvTest : ArkenvTest() {

    @Test fun `parse from env`() {
        val expectedMainString = "important com.apurebase.arkenv.main value"
        MockSystem(
            "COUNTRY" to "dk",
            "EXECUTE" to ""
        )

        TestArgs(arrayOf(expectedMainString)).let {
            println(it)
            it.help shouldBe false
            it.bool shouldBe true
            it.country shouldBeEqualTo "dk"
            it.mainString shouldBeEqualTo expectedMainString
            it.nullInt shouldBe null
        }
    }

    @Test fun `main arg by env should not work`() {
        val expected = "test"
        MockSystem(MainArg::mainArg.name to expected)
        MainArg("").mainArg shouldBeEqualTo ""
    }

    @Test fun `only parse -- arguments`() {
        MockSystem(
            "COUNTRY" to "DK",
            "NI" to "5",
            "-ni" to "5"
        )
        TestArgs(arrayOf("Hello World")).let {
            it.mainString shouldEqual "Hello World"
            it.country shouldEqual "DK"
            it.nullInt shouldEqual null
        }
    }

    override fun testNullable(): Nullable {
        MockSystem(
            "INT" to expectedInt.toString(),
            "STR" to expectedStr
        )
        return Nullable(arrayOf())
    }

    override fun testArkuments(): Arkuments {
        MockSystem(
            "CONFIG" to expectedConfigPath,
            "MANUAL_AUTH" to "",
            "REFRESH" to "",
            "HELP" to ""
        )
        return Arkuments(arrayOf())
    }

    // Value picking tests
    @Test fun `not defined`() {
        TestArgs(arrayOf()).description shouldEqual null
    }

    @Test fun `last argument`() {
        MockSystem("DESCRIPTION" to "text")
        TestArgs(arrayOf()).description shouldEqual "text"
    }

    @Test fun `envVariable usage`() {
        MockSystem("DESCRIPTION" to "text", "DESC" to "SOME MORE DESC")
        TestArgs(arrayOf()).description shouldEqual "SOME MORE DESC"
    }

    @Test fun `everything and cli argument`() {
        MockSystem("DESCRIPTION" to "text", "DESC" to "SOME MORE DESC")
        TestArgs(arrayOf("-d", "main desc")).description shouldEqual "main desc"
    }

    @Test fun `custom env name should parse`() {
        val envName = "ENV_NAME"
        val expected = "result"

        class CustomEnv(args: Array<String>) : Arkenv(args) {
            val arg: String by argument("-a") {
                envVariable = envName
            }
        }

        { CustomEnv(arrayOf()).arg } shouldThrow IllegalArgumentException::class // nothing passed
        CustomEnv(arrayOf("-a", expected)).arg shouldBeEqualTo expected // via arg

        MockSystem(envName to expected)
        CustomEnv(arrayOf()).arg shouldBeEqualTo expected // via env
    }

}
