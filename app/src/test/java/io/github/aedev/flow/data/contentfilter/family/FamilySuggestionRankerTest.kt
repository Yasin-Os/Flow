package io.github.aedev.flow.data.contentfilter.family

import com.google.common.truth.Truth.assertThat
import io.github.aedev.flow.data.model.Video
import org.junit.Test

class FamilySuggestionRankerTest {
    private val channels =
        FamilyChannelMatcher.of(
            listOf(
                FamilyChannel(channelId = "UCwaz", handle = "@waz", name = "Waz", category = "waz"),
                FamilyChannel(channelId = "UCtilawat", handle = "@tilawat", name = "Tilawat", category = "tilawat"),
                FamilyChannel(channelId = "UCghazal", handle = "@ghazal", name = "Ghazal", category = "ghazal"),
                FamilyChannel(channelId = "UCnews", handle = "@news", name = "News", category = "news"),
                FamilyChannel(channelId = "UCcartoon", handle = "@cartoon", name = "Cartoons", category = "cartoons"),
                FamilyChannel(channelId = "UCplain", handle = "@plain", name = "Plain"),
            ),
        )
    private val text = FamilyTextMatcher.DEFAULT

    private fun video(
        id: String,
        channelId: String,
        title: String,
        timestamp: Long = 1_000L,
        isShort: Boolean = false,
        isUpcoming: Boolean = false,
    ) = Video(
        id = id,
        title = title,
        channelName = channelId,
        channelId = channelId,
        thumbnailUrl = "",
        duration = 600,
        viewCount = 1,
        uploadDate = "",
        timestamp = timestamp,
        isShort = isShort,
        isUpcoming = isUpcoming,
    )

    private fun homeFeed(vararg videos: Video) =
        FamilySuggestionRanker.suggestHome(
            cachedFeed = videos.toList(),
            channels = channels,
            text = text,
            watchedVideoIds = emptySet(),
            hidesFromRecommendations = { false },
        )

    @Test
    fun `waz tilawat and ghazal rank first newest first then other categories then news`() {
        val ranked =
            homeFeed(
                video("news-new", "UCnews", "Breaking News", timestamp = 9_000L),
                video("cartoon-old", "UCcartoon", "Islamic Cartoon", timestamp = 1_000L),
                video("waz-old", "UCwaz", "Jumar Khutba", timestamp = 2_000L),
                video("ghazal-new", "UCghazal", "New Nasheed", timestamp = 8_000L),
                video("tilawat", "UCtilawat", "Surah Al-Baqarah", timestamp = 3_000L),
                video("waz-new", "UCwaz", "Tafsir Mahfil", timestamp = 7_000L),
            )
        assertThat(ranked.map { it.id }).containsExactly(
            "ghazal-new",
            "waz-new",
            "tilawat",
            "waz-old",
            "cartoon-old",
            "news-new",
        ).inOrder()
    }

    @Test
    fun `an uncategorized approved channel ranks with the other categories before news`() {
        val ranked =
            homeFeed(
                video("news", "UCnews", "Daily News", timestamp = 9_000L),
                video("plain", "UCplain", "Islamic Reminder", timestamp = 1_000L),
            )
        assertThat(ranked.map { it.id }).containsExactly("plain", "news").inOrder()
    }

    @Test
    fun `a hard-tier title is dropped even from a whitelisted waz channel`() {
        val ranked = homeFeed(video("bad", "UCwaz", "Pornography in Society"))
        assertThat(ranked).isEmpty()
    }

    @Test
    fun `a news channel music title is dropped but a ghazal song title is kept`() {
        val ranked =
            homeFeed(
                video("news-song", "UCnews", "Bollywood Song News"),
                video("ghazal-song", "UCghazal", "Bangla Islamic Song"),
            )
        assertThat(ranked.map { it.id }).containsExactly("ghazal-song")
    }

    @Test
    fun `a video from a channel that is not whitelisted is never suggested`() {
        val ranked = homeFeed(video("stranger", "UCunknown", "নতুন ওয়াজ মাহফিল"))
        assertThat(ranked).isEmpty()
    }

    @Test
    fun `watched shorts and upcoming rows are never suggested`() {
        val ranked =
            FamilySuggestionRanker.suggestHome(
                cachedFeed =
                    listOf(
                        video("short", "UCwaz", "Short Waz", isShort = true),
                        video("upcoming", "UCwaz", "Upcoming Waz", isUpcoming = true),
                        video("good", "UCwaz", "Good Waz"),
                    ),
                channels = channels,
                text = text,
                watchedVideoIds = setOf("good"),
                hidesFromRecommendations = { false },
            )
        assertThat(ranked).isEmpty()
    }

    @Test
    fun `related puts the playing video's own category first and never the video itself`() {
        val playing = video("playing", "UCtilawat", "Surah Yasin")
        val related =
            FamilySuggestionRanker.suggestRelated(
                playing = playing,
                cachedFeed =
                    listOf(
                        playing,
                        video("waz", "UCwaz", "Waz Mahfil", timestamp = 9_000L),
                        video("tilawat-new", "UCtilawat", "Surah Al-Mulk", timestamp = 8_000L),
                        video("tilawat-old", "UCtilawat", "Surah Al-Kahf", timestamp = 1_000L),
                    ),
                channels = channels,
                text = text,
                watchedVideoIds = emptySet(),
                hidesFromRecommendations = { false },
            )
        assertThat(related.map { it.id }).containsExactly("tilawat-new", "tilawat-old", "waz").inOrder()
    }

    @Test
    fun `home suggestions are capped at twenty`() {
        val many = (1..25).map { video("v$it", "UCwaz", "Waz $it", timestamp = it.toLong()) }
        assertThat(homeFeed(*many.toTypedArray())).hasSize(FamilySuggestionRanker.HOME_SUGGESTIONS_MAX)
    }

    @Test
    fun `rank with a matcher that has not loaded yet sorts without dropping`() {
        val ranked =
            FamilySuggestionRanker.rank(
                pool =
                    listOf(
                        video("news", "UCnews", "Daily News", timestamp = 9_000L),
                        video("waz", "UCwaz", "Waz Mahfil", timestamp = 1_000L),
                    ),
                channels = FamilyChannelMatcher.EMPTY,
                text = text,
            )
        // Categories are unknown while the matcher is empty, so recency decides — but
        // nothing is dropped: an empty matcher must never empty the feed.
        assertThat(ranked.map { it.id }).containsExactly("news", "waz").inOrder()
    }
}
