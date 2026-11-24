package io.github.aedev.flow.data.contentfilter.family

/**
 * The approved topics for open search. A video from a channel that is not whitelisted is kept
 * only when its title, description, channel name or snippet matches one of these topics (see
 * [FamilySearchRule]); the query itself matching a topic relaxes the result-side topic gate
 * to blocklist-only.
 *
 * The keyword sets come from Gemini's ApprovedTopicsList (Reply 3), verbatim. Generic words
 * alone do NOT match a topic: "lecture", "cartoon", "comedy", "drama", "song", "film", "story"
 * and "kids" appear in the lists only inside anchored phrases ("islamic cartoon", "হালাল
 * কমেডি"). Two exceptions: news words ("news", "সংবাদ", "খবর") match alone, and distinctive
 * Islamic words (waz/ওয়াজ, mahfil, tafsir, ghazal/গজল, nasheed, naat, tilawat, quran, qari,
 * hadith, islamic, allah, masjid, azan, ramadan, ...) match alone.
 *
 * [matchTopic] returns the first matching topic in declaration order, with [ISLAMIC_GENERAL]
 * last — so "Islamic Drama" matches [HALAL_DRAMA], "Islamic Cartoon" matches [CARTOONS] and
 * "Islamic Nasheed" matches [NASHEED], not the general topic. The text is normalized but NOT
 * stripped: "ইসলামী সংগীত" still matches the NASHEED topic (the blocklist strips the phrase
 * before its own matching; the topic matcher must still see it).
 */
class FamilyTopicMatcher {
    private val topicPatterns: List<Pair<FamilyTopic, Regex>> =
        FamilyTopic.entries.mapNotNull { topic ->
            val terms = TOPIC_TERMS[topic].orEmpty()
            FamilyTextMatcher.buildPattern(terms)?.let { topic to it }
        }

    /** The first matching topic in declaration order, or null when no topic matches. */
    fun matchTopic(text: String): FamilyTopic? {
        val normalized = FamilyTextMatcher.normalize(text)
        return topicPatterns
            .firstOrNull { (_, pattern) ->
                pattern.containsMatchIn(normalized)
            }?.first
    }

    /** True when the text matches any approved topic. */
    fun matchesTopic(text: String): Boolean = matchTopic(text) != null

    companion object {
        val DEFAULT = FamilyTopicMatcher()

        private val TOPIC_TERMS: Map<FamilyTopic, Set<String>> =
            mapOf(
                FamilyTopic.WAZ to (WAZ_EN + WAZ_BN + WAZ_AR + WAZ_UR + WAZ_HI_UR_TRANSLIT),
                FamilyTopic.NASHEED to
                    (NASHEED_EN + NASHEED_BN + NASHEED_AR + NASHEED_UR + NASHEED_HI_UR_TRANSLIT),
                FamilyTopic.TILAWAT to
                    (TILAWAT_EN + TILAWAT_BN + TILAWAT_AR + TILAWAT_UR + TILAWAT_HI_UR_TRANSLIT),
                FamilyTopic.CARTOONS to
                    (CARTOONS_EN + CARTOONS_BN + CARTOONS_AR + CARTOONS_UR + CARTOONS_HI_UR_TRANSLIT),
                FamilyTopic.HALAL_COMEDY to
                    (
                        HALAL_COMEDY_EN + HALAL_COMEDY_BN + HALAL_COMEDY_AR +
                            HALAL_COMEDY_UR + HALAL_COMEDY_HI_UR_TRANSLIT
                    ),
                FamilyTopic.HALAL_DRAMA to
                    (
                        HALAL_DRAMA_EN + HALAL_DRAMA_BN + HALAL_DRAMA_AR +
                            HALAL_DRAMA_UR + HALAL_DRAMA_HI_UR_TRANSLIT
                    ),
                FamilyTopic.NEWS to (NEWS_EN + NEWS_BN + NEWS_AR + NEWS_UR + NEWS_HI_UR_TRANSLIT),
                FamilyTopic.ISLAMIC_GENERAL to
                    (
                        ISLAMIC_GENERAL_EN + ISLAMIC_GENERAL_BN + ISLAMIC_GENERAL_AR +
                            ISLAMIC_GENERAL_UR + ISLAMIC_GENERAL_HI_UR_TRANSLIT
                    ),
            )
    }
}

/** The approved topics, in match order — [FamilyTopic.ISLAMIC_GENERAL] is always last. */
enum class FamilyTopic {
    WAZ,
    NASHEED,
    TILAWAT,
    CARTOONS,
    HALAL_COMEDY,
    HALAL_DRAMA,
    NEWS,
    ISLAMIC_GENERAL,
}
