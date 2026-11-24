package io.github.aedev.flow.data.paging

import com.google.common.truth.Truth.assertThat
import io.github.aedev.flow.data.contentfilter.family.FamilyChannel
import io.github.aedev.flow.data.contentfilter.family.FamilyChannelMatcher
import io.github.aedev.flow.data.contentfilter.family.FamilySearchRule
import io.github.aedev.flow.data.contentfilter.family.FamilyTextMatcher
import io.github.aedev.flow.data.model.Channel
import io.github.aedev.flow.data.model.Playlist
import io.github.aedev.flow.data.model.Video
import io.github.aedev.flow.data.recommendation.FeedExclusions
import org.junit.Test

/**
 * The family search rule applied to loaded search results: a non-whitelisted video survives
 * when it is about an approved topic and passes the blocklist, a channel card survives when
 * its name or handle matches a topic, and playlists still go.
 */
class SearchPagingSourceFamilyTest {
    private fun familyExclusions(): FeedExclusions {
        val channels =
            FamilyChannelMatcher.of(
                listOf(
                    FamilyChannel(channelId = "UCghazal", handle = "@holytune", name = "Holy Tune"),
                ),
            )
        return FeedExclusions(
            family = channels,
            familyText = FamilyTextMatcher.DEFAULT,
            familySearch = FamilySearchRule(channels, FamilyTextMatcher.DEFAULT),
        )
    }

    private fun video(channelId: String) =
        Video(
            id = "v1",
            title = "t",
            channelName = "c",
            channelId = channelId,
            thumbnailUrl = "",
            duration = 60,
            viewCount = 1,
            uploadDate = "",
        )

    private fun channel(
        id: String,
        name: String,
    ) = Channel(id = id, name = name, thumbnailUrl = "", subscriberCount = 0L)

    @Test
    fun `family search keeps a waz video and drops a music video`() {
        val waz = SearchResultItem.VideoResult(video("UCunknown").copy(id = "w1", title = "নতুন ওয়াজ মাহফিল"))
        val music = SearchResultItem.VideoResult(video("UCunknown").copy(id = "m1", title = "Top Songs 2024"))

        assertThat(listOf(waz, music).withoutHidden(familyExclusions())).containsExactly(waz)
    }

    @Test
    fun `family search keeps a whitelisted video and drops a playlist`() {
        val kept = SearchResultItem.VideoResult(video("UCghazal").copy(id = "v1", title = "Bangla Islamic Song"))
        val playlist =
            SearchResultItem.PlaylistResult(
                Playlist(id = "p1", name = "Mix", thumbnailUrl = "", videoCount = 3),
            )

        assertThat(listOf(kept, playlist).withoutHidden(familyExclusions())).containsExactly(kept)
    }

    @Test
    fun `family search keeps a channel card whose name matches a topic`() {
        val card = SearchResultItem.ChannelResult(channel("UCunknown", "Holy Tune"))
        val other = SearchResultItem.ChannelResult(channel("UCother", "Random Vlogs"))

        assertThat(listOf(card, other).withoutHidden(familyExclusions())).containsExactly(card)
    }

    @Test
    fun `family search filters a kept channel card's latest videos`() {
        val waz = video("UCunknown").copy(id = "w1", title = "নতুন ওয়াজ মাহফিল")
        val music = video("UCunknown").copy(id = "m1", title = "Top Songs 2024")
        val card =
            SearchResultItem.ChannelResult(
                channel("UCunknown", "Holy Tune"),
                latestVideos = listOf(waz, music),
            )

        val kept = listOf(card).withoutHidden(familyExclusions()).single() as SearchResultItem.ChannelResult

        assertThat(kept.latestVideos).containsExactly(waz)
    }

    @Test
    fun `family search drops a shelf left with nothing`() {
        val shelf =
            SearchResultItem.ShelfResult(
                id = "s1",
                title = "Latest",
                kind = SearchShelfKind.VIDEOS,
                videos = listOf(video("UCunknown").copy(id = "m1", title = "Top Songs 2024")),
            )

        assertThat(listOf(shelf).withoutHidden(familyExclusions())).isEmpty()
    }

    @Test
    fun `family search keeps a topicless video only when the query matches a topic`() {
        val vlog = SearchResultItem.VideoResult(video("UCunknown").copy(id = "v1", title = "Daily vlog"))

        assertThat(listOf(vlog).withoutHidden(familyExclusions(), queryMatchesTopic = false)).isEmpty()
        assertThat(listOf(vlog).withoutHidden(familyExclusions(), queryMatchesTopic = true)).containsExactly(vlog)
    }

    @Test
    fun `without a family search rule the whitelist still hides every outsider`() {
        val channels =
            FamilyChannelMatcher.of(
                listOf(
                    FamilyChannel(channelId = "UCghazal", handle = "@holytune", name = "Holy Tune"),
                ),
            )
        val outsider =
            SearchResultItem.VideoResult(video("UCunknown").copy(id = "w1", title = "নতুন ওয়াজ মাহফিল"))

        assertThat(
            listOf(outsider).withoutHidden(
                FeedExclusions(family = channels, familyText = FamilyTextMatcher.DEFAULT),
            ),
        ).isEmpty()
    }
}
