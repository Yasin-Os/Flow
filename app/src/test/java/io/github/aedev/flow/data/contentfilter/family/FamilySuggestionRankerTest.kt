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
                FamilyChannel(channelId = "UCtour", handle = "@tour", name = "Tour", category = "tour"),
                FamilyChannel(channelId = "UCtourbd", handle = "@tourbd", name = "Tour BD", category = "tour", isBangladeshi = true),
                FamilyChannel(channelId = "UCcartoon", handle = "@cartoon", name = "Cartoons", category = "cartoons"),
                FamilyChannel(channelId = "UCplain", handle = "@plain", name = "Plain"),
                FamilyChannel(channelId = "UCwazbd", handle = "@wazbd", name = "Waz BD", category = "waz", isBangladeshi = true),
                FamilyChannel(
                    channelId = "UCtilawatbd",
                    handle = "@tilawatbd",
                    name = "Tilawat BD",
                    category = "tilawat",
                    isBangladeshi = true,
                ),
                FamilyChannel(channelId = "UCnewsbd", handle = "@newsbd", name = "News BD", category = "news", isBangladeshi = true),
                FamilyChannel(
                    channelId = "UCcartoonbd",
                    handle = "@cartoonbd",
                    name = "Cartoons BD",
                    category = "cartoons",
                    isBangladeshi = true,
                ),
                FamilyChannel(channelId = "UCplainbd", handle = "@plainbd", name = "Plain BD", isBangladeshi = true),
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
        assertThat(ranked.map { it.id })
            .containsExactly(
                "ghazal-new",
                "waz-new",
                "tilawat",
                "waz-old",
                "cartoon-old",
                "news-new",
            ).inOrder()
    }

    @Test
    fun `tour ranks with the other categories after the religious core and before news`() {
        val ranked =
            homeFeed(
                video("news", "UCnews", "Daily News", timestamp = 9_500L),
                video("cartoon", "UCcartoon", "Islamic Cartoon", timestamp = 9_000L),
                video("tour", "UCtour", "Cox's Bazar Tour", timestamp = 8_000L),
                video("tour-bd", "UCtourbd", "Sajek Tour", timestamp = 1_000L),
                video("waz", "UCwaz", "Waz Mahfil", timestamp = 1_000L),
            )
        assertThat(ranked.map { it.id })
            .containsExactly(
                "waz",
                "tour-bd",
                "cartoon",
                "tour",
                "news",
            ).inOrder()
    }

    @Test
    fun `bangladeshi tour still ranks after foreign waz`() {
        // Bangladeshi-first applies inside a tier, it never lifts tour above waz.
        val ranked =
            homeFeed(
                video("bd-tour", "UCtourbd", "Sajek Tour", timestamp = 9_000L),
                video("foreign-waz", "UCwaz", "Waz Mahfil", timestamp = 1_000L),
            )
        assertThat(ranked.map { it.id }).containsExactly("foreign-waz", "bd-tour").inOrder()
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
    fun `bangladeshi waz ranks before a newer foreign waz`() {
        val ranked =
            homeFeed(
                video("foreign-waz", "UCwaz", "Waz Mahfil", timestamp = 9_000L),
                video("bd-waz", "UCwazbd", "Bangladeshi Waz Mahfil", timestamp = 1_000L),
            )
        assertThat(ranked.map { it.id }).containsExactly("bd-waz", "foreign-waz").inOrder()
    }

    @Test
    fun `bangladeshi news still ranks after foreign waz`() {
        // Bangladeshi-first applies inside a tier, it never lifts news above waz.
        val ranked =
            homeFeed(
                video("bd-news", "UCnewsbd", "Bangladesh News", timestamp = 9_000L),
                video("foreign-waz", "UCwaz", "Waz Mahfil", timestamp = 1_000L),
            )
        assertThat(ranked.map { it.id }).containsExactly("foreign-waz", "bd-news").inOrder()
    }

    @Test
    fun `bangladeshi ranks first inside the other categories and inside news`() {
        val ranked =
            homeFeed(
                video("news", "UCnews", "Daily News", timestamp = 9_000L),
                video("cartoon-bd", "UCcartoonbd", "Bangladeshi Islamic Cartoon", timestamp = 1_000L),
                video("news-bd", "UCnewsbd", "Bangladesh News", timestamp = 1_000L),
                video("cartoon", "UCcartoon", "Islamic Cartoon", timestamp = 9_000L),
            )
        assertThat(ranked.map { it.id })
            .containsExactly(
                "cartoon-bd",
                "cartoon",
                "news-bd",
                "news",
            ).inOrder()
    }

    @Test
    fun `a channel without the marker ranks as foreign`() {
        val ranked =
            homeFeed(
                video("plain", "UCplain", "Islamic Reminder", timestamp = 9_000L),
                video("plain-bd", "UCplainbd", "Bangladeshi Reminder", timestamp = 1_000L),
            )
        assertThat(ranked.map { it.id }).containsExactly("plain-bd", "plain").inOrder()
    }

    @Test
    fun `related puts bangladeshi first inside the playing video's own category`() {
        val playing = video("playing", "UCtilawat", "Surah Yasin")
        val related =
            FamilySuggestionRanker.suggestRelated(
                playing = playing,
                cachedFeed =
                    listOf(
                        playing,
                        video("tilawat", "UCtilawat", "Surah Al-Mulk", timestamp = 9_000L),
                        video("tilawat-bd", "UCtilawatbd", "Bangladeshi Tilawat", timestamp = 1_000L),
                        video("waz", "UCwaz", "Waz Mahfil", timestamp = 8_000L),
                    ),
                channels = channels,
                text = text,
                watchedVideoIds = emptySet(),
                hidesFromRecommendations = { false },
            )
        assertThat(related.map { it.id }).containsExactly("tilawat-bd", "tilawat", "waz").inOrder()
    }

    @Test
    fun `rank orders bangladeshi before foreign in the home feed pool`() {
        val ranked =
            FamilySuggestionRanker.rank(
                pool =
                    listOf(
                        video("foreign-waz", "UCwaz", "Waz Mahfil", timestamp = 9_000L),
                        video("bd-waz", "UCwazbd", "Bangladeshi Waz Mahfil", timestamp = 1_000L),
                    ),
                channels = channels,
                text = text,
            )
        assertThat(ranked.map { it.id }).containsExactly("bd-waz", "foreign-waz").inOrder()
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
    fun `a tour channel music title is dropped but a clean tour title is kept`() {
        val ranked =
            homeFeed(
                video("tour-song", "UCtour", "Bollywood Song Tour"),
                video("tour-clean", "UCtour", "Sajek Tour Vlog"),
            )
        assertThat(ranked.map { it.id }).containsExactly("tour-clean")
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

    @Test
    fun `rank windowed scan returns exactly the full-scan list on a realistic pool`() {
        val pool = realisticPool()
        for (limit in listOf(5, FamilySuggestionRanker.HOME_SUGGESTIONS_MAX)) {
            assertThat(FamilySuggestionRanker.rank(pool, channels, text, limit))
                .containsExactlyElementsIn(fullScanRank(pool, limit))
                .inOrder()
        }
    }

    @Test
    fun `rank windowed scan matches the full scan when fewer videos pass than the limit`() {
        val pool =
            listOf(
                video("blocked-top", "UCwazbd", "Pornography in Society", timestamp = 99_000L),
                video("good-1", "UCwazbd", "Bangladeshi Waz Mahfil", timestamp = 90_000L),
                video("stranger", "UCunknown", "নতুন ওয়াজ মাহফিল", timestamp = 89_000L),
                video("", "UCwaz", "Waz Mahfil", timestamp = 88_000L),
                video("good-2", "UCtilawat", "Surah Al-Mulk", timestamp = 80_000L),
            )
        assertThat(FamilySuggestionRanker.rank(pool, channels, text))
            .containsExactlyElementsIn(fullScanRank(pool, FamilySuggestionRanker.HOME_SUGGESTIONS_MAX))
            .inOrder()
    }

    /**
     * A realistic home pool: 69 videos across every tier, Bangladeshi and foreign — with
     * hard-tier titles sitting at the very top of the ranking (so the window must scan past
     * them, not stop at them), a stranger channel, a blank id, a blocked news title, and
     * equal timestamps (stable-sort ties). 64 videos pass, so a limit of 20 stops the
     * window after ~22 regex scans instead of 69.
     */
    private fun realisticPool(): List<Video> {
        val pool = mutableListOf<Video>()
        // Blocked videos outrank everything clean: the window must skip them, not fill with them.
        pool += video("blocked-bd-waz-1", "UCwazbd", "Pornography in Society", timestamp = 99_000L)
        pool += video("blocked-bd-waz-2", "UCwazbd", "Pornography in Society", timestamp = 98_000L)
        pool += video("stranger", "UCunknown", "নতুন ওয়াজ মাহফিল", timestamp = 97_000L)
        pool += video("", "UCwazbd", "Bangladeshi Waz Mahfil", timestamp = 96_000L)
        for (i in 1..12) {
            pool += video("bd-waz-$i", "UCwazbd", "Bangladeshi Waz Mahfil $i", timestamp = 90_000L - i * 1_000L)
        }
        for (i in 1..8) {
            pool += video("waz-$i", "UCwaz", "Waz Mahfil $i", timestamp = 89_000L - i * 1_000L)
        }
        // Equal timestamps: the two Bangladeshi ties keep their pool order ahead of the foreign tie.
        pool += video("bd-waz-tie-a", "UCwazbd", "Bangladeshi Waz Mahfil Tie A", timestamp = 75_000L)
        pool += video("bd-waz-tie-b", "UCwazbd", "Bangladeshi Waz Mahfil Tie B", timestamp = 75_000L)
        pool += video("waz-tie", "UCwaz", "Waz Mahfil Tie", timestamp = 75_000L)
        for (i in 1..6) {
            pool += video("bd-tilawat-$i", "UCtilawatbd", "Bangladeshi Tilawat $i", timestamp = 71_000L - i * 1_000L)
            pool += video("tilawat-$i", "UCtilawat", "Surah Tilawat $i", timestamp = 70_000L - i * 1_000L)
        }
        for (i in 1..8) {
            // No Bangladeshi ghazal channel in this matcher: all eight are foreign ghazal.
            pool += video("ghazal-$i", "UCghazal", "Nasheed $i", timestamp = 62_000L - i * 1_000L)
        }
        for (i in 1..5) {
            pool += video("bd-cartoon-$i", "UCcartoonbd", "Bangladeshi Islamic Cartoon $i", timestamp = 51_000L - i * 1_000L)
            pool += video("cartoon-$i", "UCcartoon", "Islamic Cartoon $i", timestamp = 50_000L - i * 1_000L)
        }
        pool += video("plain-bd-1", "UCplainbd", "Bangladeshi Reminder 1", timestamp = 41_000L)
        pool += video("plain-1", "UCplain", "Islamic Reminder 1", timestamp = 40_000L)
        pool += video("plain-bd-2", "UCplainbd", "Bangladeshi Reminder 2", timestamp = 39_000L)
        pool += video("plain-2", "UCplain", "Islamic Reminder 2", timestamp = 38_000L)
        pool += video("plain-3", "UCplain", "Islamic Reminder 3", timestamp = 37_000L)
        // News ranks last whatever its timestamp: even the newest blocked news title is
        // never scanned once the window has filled from the higher tiers.
        pool += video("blocked-news", "UCnews", "Bollywood Song News", timestamp = 99_500L)
        for (i in 1..3) {
            pool += video("bd-news-$i", "UCnewsbd", "Bangladesh News $i", timestamp = 30_000L - i * 1_000L)
            pool += video("news-$i", "UCnews", "Daily News $i", timestamp = 31_000L - i * 1_000L)
        }
        return pool
    }

    /**
     * The pre-windowed implementation of [FamilySuggestionRanker.rank]: filter the whole
     * pool first, then sort, then take. The windowed scan must return exactly this list.
     */
    private fun fullScanRank(
        pool: List<Video>,
        limit: Int,
    ): List<Video> {
        val rule = FamilySearchRule(channels, text)
        return pool
            .filter { video ->
                video.id.isNotBlank() &&
                    channels.allows(video.channelId, video.channelHandle) &&
                    rule.keepsVideo(video)
            }.sortedWith(
                compareBy(
                    { tierOf(channels.categoryOf(it.channelId, it.channelHandle)) },
                    { if (channels.isBangladeshi(it.channelId, it.channelHandle)) 0 else 1 },
                    { -it.timestamp },
                ),
            ).take(limit)
    }

    private fun tierOf(category: String): Int =
        when (category) {
            "waz", "tilawat", "ghazal" -> 0
            "tour" -> 1
            "news" -> 2
            else -> 1
        }
}
