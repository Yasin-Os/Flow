package io.github.aedev.flow.data.contentfilter.family

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class FamilyTopicMatcherTest {
    private val topics = FamilyTopicMatcher.DEFAULT

    @Test
    fun `waz matches by its distinctive words`() {
        assertThat(topics.matchTopic("নতুন ওয়াজ মাহফিল")).isEqualTo(FamilyTopic.WAZ)
        assertThat(topics.matchTopic("Azhari waz 2024")).isEqualTo(FamilyTopic.WAZ)
        assertThat(topics.matchTopic("islamic lecture on faith")).isEqualTo(FamilyTopic.WAZ)
    }

    @Test
    fun `nasheed matches by its distinctive words`() {
        assertThat(topics.matchTopic("Beautiful Nasheed")).isEqualTo(FamilyTopic.NASHEED)
        assertThat(topics.matchTopic("ইসলামী সংগীত")).isEqualTo(FamilyTopic.NASHEED)
    }

    @Test
    fun `tilawat matches by its distinctive words`() {
        assertThat(topics.matchTopic("Tilawat of Surah Yasin")).isEqualTo(FamilyTopic.TILAWAT)
    }

    @Test
    fun `cartoons need islamic anchoring`() {
        assertThat(topics.matchTopic("Islamic Cartoon for Kids")).isEqualTo(FamilyTopic.CARTOONS)
    }

    @Test
    fun `halal comedy needs halal anchoring`() {
        assertThat(topics.matchTopic("হালাল কমেডি")).isEqualTo(FamilyTopic.HALAL_COMEDY)
        assertThat(topics.matchTopic("halal comedy show")).isEqualTo(FamilyTopic.HALAL_COMEDY)
    }

    @Test
    fun `the first topic in declaration order wins`() {
        assertThat(topics.matchTopic("Islamic Drama")).isEqualTo(FamilyTopic.HALAL_DRAMA)
        assertThat(topics.matchTopic("Islamic Cartoon")).isEqualTo(FamilyTopic.CARTOONS)
        assertThat(topics.matchTopic("Islamic Nasheed")).isEqualTo(FamilyTopic.NASHEED)
    }

    @Test
    fun `news matches alone`() {
        assertThat(topics.matchTopic("আজকের সংবাদ")).isEqualTo(FamilyTopic.NEWS)
        assertThat(topics.matchTopic("breaking news")).isEqualTo(FamilyTopic.NEWS)
        assertThat(topics.matchTopic("খবর")).isEqualTo(FamilyTopic.NEWS)
    }

    @Test
    fun `tour matches by its distinctive words`() {
        assertThat(topics.matchTopic("Sajek Tour")).isEqualTo(FamilyTopic.TOUR)
        assertThat(topics.matchTopic("Cox's Bazar travel vlog")).isEqualTo(FamilyTopic.TOUR)
        assertThat(topics.matchTopic("ভ্রমণ ব্লগ")).isEqualTo(FamilyTopic.TOUR)
        assertThat(topics.matchTopic("সাজেক ভ্রমণ")).isEqualTo(FamilyTopic.TOUR)
    }

    @Test
    fun `vlog trip and blog alone match nothing`() {
        assertThat(topics.matchTopic("vlog")).isNull()
        assertThat(topics.matchTopic("trip")).isNull()
        assertThat(topics.matchTopic("blog")).isNull()
    }

    @Test
    fun `generic words alone match nothing`() {
        assertThat(topics.matchTopic("cartoon")).isNull()
        assertThat(topics.matchTopic("comedy")).isNull()
        assertThat(topics.matchTopic("drama")).isNull()
        assertThat(topics.matchTopic("lecture")).isNull()
        assertThat(topics.matchTopic("song")).isNull()
        assertThat(topics.matchTopic("film")).isNull()
        assertThat(topics.matchTopic("cooking video")).isNull()
    }

    @Test
    fun `islamic alone falls through to the general topic`() {
        assertThat(topics.matchTopic("islamic")).isEqualTo(FamilyTopic.ISLAMIC_GENERAL)
        assertThat(topics.matchTopic("The Islamic way of life")).isEqualTo(FamilyTopic.ISLAMIC_GENERAL)
    }
}
