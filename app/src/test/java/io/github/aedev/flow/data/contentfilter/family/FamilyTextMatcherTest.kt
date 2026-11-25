package io.github.aedev.flow.data.contentfilter.family

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class FamilyTextMatcherTest {
    private val text = FamilyTextMatcher.DEFAULT
    private val channels =
        FamilyChannelMatcher.of(
            listOf(
                FamilyChannel(channelId = "UCghazal", handle = "@holytune", name = "Holy Tune"),
                FamilyChannel(
                    channelId = "UCnews",
                    handle = "@somoytv",
                    name = "Somoy TV",
                    category = "news",
                ),
                FamilyChannel(
                    channelId = "UCtour",
                    handle = "@tourbd",
                    name = "Tour BD",
                    category = "tour",
                ),
            ),
        )

    private fun blocks(
        title: String,
        channelId: String = "UCunknown",
        handle: String? = null,
        channelName: String = "",
    ): Boolean = text.blocks(title, "", channelId, handle, channels, channelName)

    @Test
    fun `ahmad does not match mad`() {
        assertThat(text.blocksHard("শায়েখ আহমদউল্লাহ")).isFalse()
        assertThat(text.blocksEntertainment("শায়েখ আহমদউল্লাহ")).isFalse()
        assertThat(blocks("শায়েখ আহমদউল্লাহ")).isFalse()
    }

    @Test
    fun `bagan does not match gaan`() {
        assertThat(text.blocksEntertainment("বাগান")).isFalse()
        assertThat(blocks("বাগান")).isFalse()
    }

    @Test
    fun `gandhi does not match gaan`() {
        assertThat(text.blocksEntertainment("গান্ধী")).isFalse()
        assertThat(blocks("গান্ধী")).isFalse()
    }

    @Test
    fun `no music nasheed passes`() {
        assertThat(text.blocksEntertainment("No Music Nasheed")).isFalse()
        assertThat(text.blocksEntertainment("ইসলামী সংগীত")).isFalse()
        assertThat(text.blocksEntertainment("ইসলামিক গান")).isFalse()
        assertThat(blocks("No Music Nasheed")).isFalse()
    }

    @Test
    fun `a lecture about gambling passes through the vice exemption`() {
        assertThat(text.blocksVice("জুয়া ও মদপানের ভয়াবহতা - নতুন ওয়াজ")).isFalse()
        assertThat(blocks("জুয়া ও মদপানের ভয়াবহতা - নতুন ওয়াজ")).isFalse()
    }

    @Test
    fun `a how to gamble title is blocked`() {
        assertThat(text.blocksVice("জুয়া খেলার নিয়ম")).isTrue()
        assertThat(blocks("জুয়া খেলার নিয়ম")).isTrue()
    }

    @Test
    fun `haram exempts a vice title from the word lists and the short root regexes`() {
        assertThat(text.blocksVice("মদ হারাম")).isFalse()
        assertThat(text.blocksEntertainment("মদ হারাম")).isFalse()
        assertThat(text.blocksQuery("মদ হারাম")).isFalse()
        assertThat(text.blocksQuery("জুয়া হারাম")).isFalse()
        assertThat(text.blocksQuery("জুয়া")).isTrue()
    }

    @Test
    fun `an empty matcher reports empty`() {
        assertThat(FamilyTextMatcher.EMPTY.isEmpty).isTrue()
        assertThat(FamilyTextMatcher.DEFAULT.isEmpty).isFalse()
    }

    @Test
    fun `madina does not match mad`() {
        assertThat(text.blocksEntertainment("মদিনা")).isFalse()
        assertThat(text.blocksEntertainment("মদীনা")).isFalse()
        assertThat(blocks("মদিনা শরীফ")).isFalse()
        assertThat(text.blocksQuery("মদিনা")).isFalse()
    }

    @Test
    fun `a vice root without a marker still blocks, with one it passes`() {
        assertThat(text.blocksEntertainment("beer")).isTrue()
        assertThat(text.blocksEntertainment("beer হারাম")).isFalse()
        assertThat(text.blocksQuery("wine")).isTrue()
        assertThat(text.blocksQuery("wine হারাম")).isFalse()
    }

    @Test
    fun `a music title that merely mentions a lecture marker still blocks`() {
        assertThat(text.blocksEntertainment("গান শুনুন - ওয়াজ")).isTrue()
        assertThat(text.blocksEntertainment("pop শুনুন - ওয়াজ")).isTrue()
    }

    @Test
    fun `a whitelisted ghazal keeps its song title, an outsider does not`() {
        assertThat(blocks("Bangla Islamic Song", channelId = "UCghazal", handle = "@holytune")).isFalse()
        assertThat(blocks("Bangla Islamic Song")).isTrue()
    }

    @Test
    fun `the drama carve out exempts drama words and film words only`() {
        assertThat(text.blocksEntertainment("ইসলামিক নাটক", exemptDrama = true)).isFalse()
        assertThat(text.blocksEntertainment("ইসলামিক নাটক")).isTrue()
        assertThat(text.blocksEntertainment("The Message Movie", exemptDrama = true)).isFalse()
        assertThat(text.blocksEntertainment("The Message Movie")).isTrue()
        assertThat(text.blocksEntertainment("Bollywood Movie", exemptDrama = true)).isTrue()
        assertThat(text.blocksEntertainment("islamic short film", exemptDrama = true)).isFalse()
    }

    @Test
    fun `a blocked emoji blocks an outsider but not a whitelisted channel`() {
        assertThat(blocks("Nasheed 🎵")).isTrue()
        assertThat(blocks("Nasheed 🎵", channelId = "UCghazal", handle = "@holytune")).isFalse()
    }

    @Test
    fun `blocked channel name substrings match by substring`() {
        assertThat(text.isBlockedChannelName("Bongo Music")).isTrue()
        assertThat(text.isBlockedChannelName("Holy Tune Records")).isTrue()
        assertThat(text.isBlockedChannelName("Holy Tune")).isFalse()
        assertThat(text.isBlockedChannelName("Azhari Official")).isFalse()
    }

    @Test
    fun `a whitelisted news channel still loses its music upload`() {
        assertThat(blocks("Bollywood Song News", channelId = "UCnews", handle = "@somoytv")).isTrue()
        assertThat(blocks("Hot News Today", channelId = "UCnews", handle = "@somoytv")).isFalse()
    }

    @Test
    fun `a whitelisted tour channel still loses its music upload`() {
        assertThat(blocks("Bollywood Song Tour", channelId = "UCtour", handle = "@tourbd")).isTrue()
        assertThat(blocks("Sajek Tour Vlog", channelId = "UCtour", handle = "@tourbd")).isFalse()
    }

    @Test
    fun `blocksQuery stays strict, with no topic relaxation`() {
        assertThat(text.blocksQuery("music")).isTrue()
        assertThat(text.blocksQuery("islamic song")).isTrue()
        assertThat(text.blocksQuery("waz")).isFalse()
        assertThat(text.blocksQuery("sex")).isTrue()
    }
}
