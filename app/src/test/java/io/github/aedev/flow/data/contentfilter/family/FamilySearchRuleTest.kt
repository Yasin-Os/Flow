package io.github.aedev.flow.data.contentfilter.family

import com.google.common.truth.Truth.assertThat
import io.github.aedev.flow.data.model.Channel
import io.github.aedev.flow.data.model.Video
import org.junit.Test

class FamilySearchRuleTest {
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
            ),
        )
    private val rule = FamilySearchRule(channels, FamilyTextMatcher.DEFAULT)

    private fun video(
        id: String,
        channelId: String,
        title: String,
        channelName: String = channelId,
        description: String = "",
        snippet: String = "",
        category: String? = null,
    ) = Video(
        id = id,
        title = title,
        channelName = channelName,
        channelId = channelId,
        thumbnailUrl = "",
        duration = 600,
        viewCount = 1,
        uploadDate = "",
        description = description,
        category = category,
        snippet = snippet,
    )

    private fun channel(
        id: String,
        name: String,
        handle: String = "",
    ) = Channel(id = id, name = name, thumbnailUrl = "", subscriberCount = 0L, handle = handle)

    @Test
    fun `a whitelisted video is kept even when its title says song`() {
        assertThat(rule.keepsVideo(video("v1", "UCghazal", "Bangla Islamic Song"))).isTrue()
    }

    @Test
    fun `a whitelisted video is kept even when its category is music`() {
        assertThat(
            rule.keepsVideo(video("v1", "UCghazal", "New Nasheed", category = "Music")),
        ).isTrue()
    }

    @Test
    fun `a whitelisted news video loses its music upload but keeps its news`() {
        assertThat(rule.keepsVideo(video("v1", "UCnews", "Bollywood Song News"))).isFalse()
        assertThat(rule.keepsVideo(video("v2", "UCnews", "Hot News Today"))).isTrue()
    }

    @Test
    fun `a waz video from an unknown channel is kept`() {
        assertThat(rule.keepsVideo(video("v1", "UCunknown", "নতুন ওয়াজ মাহফিল"))).isTrue()
    }

    @Test
    fun `a music video from an unknown channel is dropped`() {
        assertThat(rule.keepsVideo(video("v1", "UCunknown", "Top Songs 2024"))).isFalse()
    }

    @Test
    fun `a halal drama keeps its drama words and episode numbers`() {
        assertThat(
            rule.keepsVideo(video("v1", "UCunknown", "Islamic Drama Episode 1")),
        ).isTrue()
        assertThat(rule.keepsVideo(video("v1", "UCunknown", "The Message Movie"))).isTrue()
    }

    @Test
    fun `a topicless video is dropped unless the query itself matches a topic`() {
        val vlog = video("v1", "UCunknown", "Daily vlog")
        assertThat(rule.keepsVideo(vlog, queryMatchesTopic = false)).isFalse()
        assertThat(rule.keepsVideo(vlog, queryMatchesTopic = true)).isTrue()
    }

    @Test
    fun `a topic match does not save a video the blocklist refuses`() {
        assertThat(rule.keepsVideo(video("v1", "UCunknown", "Waz Music Video"))).isFalse()
    }

    @Test
    fun `the category gate drops a music categorized tilawat from an unknown channel`() {
        assertThat(
            rule.keepsVideo(video("v1", "UCunknown", "Quran Tilawat", category = "Music")),
        ).isFalse()
        assertThat(
            rule.keepsVideo(video("v1", "UCunknown", "Quran Tilawat", category = "Education")),
        ).isTrue()
    }

    @Test
    fun `a channel card is kept when its name matches a topic`() {
        assertThat(rule.keepsChannel(channel("UCunknown", "Holy Tune"))).isTrue()
        assertThat(rule.keepsChannel(channel("UCunknown", "Al Jazeera"))).isTrue()
        assertThat(rule.keepsChannel(channel("UCunknown", "Random Vlogs"))).isFalse()
    }

    @Test
    fun `a channel card is dropped when its name carries a blocked substring`() {
        assertThat(rule.keepsChannel(channel("UCunknown", "Bongo Music"))).isFalse()
        assertThat(rule.keepsChannel(channel("UCunknown", "Holy Tune Records"))).isFalse()
    }

    @Test
    fun `a whitelisted channel card is always kept`() {
        assertThat(rule.keepsChannel(channel("UCghazal", "Holy Tune", "@holytune"))).isTrue()
    }
}
