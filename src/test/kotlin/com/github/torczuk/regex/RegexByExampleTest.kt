package com.github.torczuk.regex

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class RegexByExampleTest {

    @Test
    fun `should match text, or hello world!`() {
        val text = "My phone number is +65 82001177"
        val regex = Regex("number")

        assertThat(regex.matches(text))
        val result = regex.find(text)
        assertThat(result!!.range).isEqualTo((9..14))
    }

    @Test
    fun `should match number, bad way`() {
        val text = "My phone number is +65 88801177"
        val regex = Regex("88")

        assertThat(regex.matches(text))
        val result = regex.find(text)
        assertThat(result!!.range).isEqualTo((23..24))
    }

    @Test
    fun `should match dial and phone number, better`() {
        val text = "My phone number is +65 82001177"
        val regex = Regex("[0-9]+")

        val result = regex.find(text)!!
        assertThat(result.value).isEqualTo("65")
        assertThat(result.next()!!.value).isEqualTo("82001177")
    }

    @Test
    fun `should match whole number`() {
        val text = "My phone number is +65 82001177"
        val regex = Regex("""\+[0-9]+\s[0-9]+""")  // '+' must be escaped

        val result = regex.find(text)!!
        assertThat(result.value).isEqualTo("+65 82001177")
    }

    @Test
    fun `should match whole number, the same as above but with digit matching`() {
        val text = "My phone number is +65 82001177"
        val regex = Regex("""\+\d+\s\d+""")  // \d is [0-9]

        val result = regex.find(text)!!
        assertThat(result.value).isEqualTo("+65 82001177")
    }

    @Test
    fun `should match phone number with different length`() {
        val text = "My phone number is +65 82001177 or +48826111010 when I am not in Singapore"
        val regex = Regex("""\+\d{2}\s?\d{8,9}""")  // d{2} is [0-9][0-9]
        // d{8,9} is [0-9][0-9][0-9]...[0-9]?
        val result = regex.find(text)!!
        assertThat(result.value).isEqualTo("+65 82001177")
        assertThat(result.next()!!.value).isEqualTo("+48826111010")
    }

    @Test
    fun `should match ip4 address, start with heuristic`() {
        val text = "My ip address is 168.255.100.001"
        val regex = Regex("""???""")

        val result = regex.find(text)!!
        assertThat(result.value).isEqualTo("168.255.100.001")
    }

    @Test
    fun `should match 32 bit`() {
        val text = "Thing about ranges, number should match 255..250 OR 249..200 OR 199-0 ;)"
        val regex = Regex("""25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?""")

        (0..255).forEach {
            assertThat(regex.matches(it.toString()))
        }
    }

    @Test
    fun `should decompose date based on groupings`() {
        val text = "US date format is mm/d/yy e.g. today is 12/18/19"
        val regex = Regex("""(\d{2})/(\d{2})/(\d{2})""") // or more specific (1[0-2]|0[1-9])/(3[0-1]|[0-2][1-9])/([0-9]{2})
                                                                // checking leap years and number of days in a single month ...

        val (month, day, year) = regex.find(text)!!.destructured
        assertThat(month).isEqualTo("12")
        assertThat(day).isEqualTo("18")
        assertThat(year).isEqualTo("19")
    }

    @Test
    fun `should convert us data to ISO 8601 yyyy-mm-dd`() {
        val text = "US date format is mm/d/yy e.g. today is 12/18/19"
        val regex = Regex("""(\d{2})/(\d{2})/(\d{2})""") // or more specific (1[0-2]|0[1-9])/(3[0-1]|[0-2][1-9])/([0-9]{2})
                                                                // not checking leap years and number of days in a single month ...

        val edited = text.replace(regex, "20$3-$1-$2")
        assertThat(edited).endsWith("today is 2019-12-18")
    }
}