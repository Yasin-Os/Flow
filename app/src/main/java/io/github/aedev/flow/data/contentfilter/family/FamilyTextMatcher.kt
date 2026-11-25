package io.github.aedev.flow.data.contentfilter.family

import java.text.Normalizer
import java.util.Locale

/**
 * Three tiers of blocked words, matched after normalization:
 *
 * - **Hard tier** — explicit/sexual terms in English, Bangla, Hindi/Urdu (roman, Devanagari and
 *   Urdu script) and Arabic. Applies to every video, whitelisted channels included, and to
 *   Settings search queries. Gemini's ADULT_ROMANCE sets merged into the built-in list.
 * - **Vice tier** — gambling, alcohol and drugs, in the same five language groups. Applies to
 *   every video too, but a lecture *about* the sin is exempt: when the text also carries an
 *   [ISLAMIC_EXEMPTION_MARKERS] term (waz, tafsir, hadith, quran, হারাম, ...), the vice match
 *   does not block. "জুয়া ও মদপানের ভয়াবহতা - নতুন ওয়াজ" passes; "জুয়া খেলার নিয়ম" is
 *   blocked. The vice exemption also skips the short-root title regexes (see below), so a
 *   lecture whose only regex hit is a vice root is not blocked by a regex either.
 * - **Entertainment tier** — music, song, dance, movie, drama, DJ, remix, film and their
 *   translations (Gemini's MUSIC/MOVIES/DRAMA/DANCE_DJ sets merged into the built-in list).
 *   Applies only to videos from channels that are NOT whitelisted: the approved ghazal
 *   channels title their own uploads "Bangla Islamic Song" and "Ramjan Music Video", so these
 *   words must never block a whitelisted channel. Non-whitelisted videos are already hidden
 *   on ranked surfaces; this tier matters at the player, on channel pages and in open search.
 *   Exception: whitelisted NEWS channels stay under this tier — they are approved for news,
 *   not for music/drama uploads, so a news channel's video titled with an entertainment word
 *   is still blocked (see [FamilyChannelMatcher.isNewsChannel]).
 *
 * Before any tier is checked, the halal negations in [STRIP_BEFORE_MATCH] are stripped from
 * the text ("No Music Nasheed", "ইসলামী সংগীত", "music free", ...), so a nasheed that says
 * it has no music is not blocked by the word "music". The strip applies to the blocklist
 * only — the topic matcher sees the unstripped text, so "ইসলামী সংগীত" still matches the
 * NASHEED topic.
 *
 * Title regexes ([TITLE_REGEXES]) join the entertainment-tier check, never the hard tier:
 * they catch short roots that are unsafe even as whole words ("গান" would match "বাগান" as a
 * substring, so it lives in a boundary-safe regex instead of a word list). Regex 6
 * (drama/serial + episode number) is skipped when the drama carve-out applies; regexes 1–2
 * (the short roots, including vice roots like beer/mad/jua) are skipped when the vice
 * exemption applies. Blocked emojis ([BLOCKED_EMOJIS]) and blocked channel-name substrings
 * ([BLOCKED_CHANNEL_SUBSTRINGS]) apply in the non-whitelisted branch; channel-name
 * substrings never apply to a whitelisted channel, and emojis never block a whitelisted
 * channel's own videos either (a whitelisted nasheed titled with 🎵 passes).
 *
 * Drama carve-out: when the matched topic is halal drama, the entertainment pattern drops
 * the drama sets and the generic film words ("movie", "film", "cinema", "short film", ...),
 * so "The Message Movie" and "islamic short film" pass. Studio/brand/genre words
 * (hollywood, bollywood, netflix, trailer, concert, song) still block.
 *
 * Deliberately NOT in any list: `গজল` (ghazal), `সুরা` (surah), `নিকাহ`/`نكاح` (marriage),
 * `চটি` (also "slipper" in Bangla), and `hot` (too generic — whitelisted news titles like
 * "Hot News" would be blocked) — and also `lust`, `breast`, `seduce`, `زنا` (zina) and
 * `دعارة` (prostitution): those are ordinary words in Islamic lectures about controlling
 * desire and avoiding sin, and the hard tier applies to whitelisted channels too, so they
 * would hide legitimate waz titles. Dropped from Gemini's lists for the same reason:
 * "seduction" and "seductive" (noun/adjective forms of "seduce") and "دعارة". A title like
 * "গজল | Holy Tune" or "সুরা আল-বাকারা" must pass untouched. Bare `মদ` is deliberately
 * not a vice word either: the whole-word boundary allows a following combining mark, so
 * it would match `মদিনা` — the vice roots that live only in the title regexes are
 * covered by the vice exemption ([VICE_ROOT_PATTERN]) instead.
 *
 * Known over-block edges, kept deliberately: "romance" is in the hard tier, so a waz titled
 * "Romance in Islam" would be blocked — the word is overwhelmingly romantic/entertainment
 * content. Regex 3 ("ft"/"feat") can hit a nasheed collaboration ("Gojol ft. ...") and
 * regex 1's "gay" can hit a lecture about homosexuality; both are entertainment-tier only,
 * so whitelisted channels are unaffected. "prostitut" and "masturbat" are dead stems Gemini
 * supplied (they never match under whole-word boundaries); the lists already carry
 * "prostitute" and "masturbate".
 *
 * Matching: the text is NFC-normalized, lowercased, stripped of Arabic diacritics and leet-folded
 * (`0→o, 1→i, 3→e, 4→a, 5→s, 7→t, 8→b, @→a, $→s`), so `s3x`, `s*x` and `s e x` all match
 * `sex`. A term matches only as a whole word: letters inside a term may be separated by
 * punctuation or spaces, and a letter may be replaced by punctuation (`s*x`), but the term must
 * be bounded by non-letters on both sides. The leading boundary also rejects a preceding
 * combining mark (the term would start mid-syllable); the trailing boundary allows a following
 * mark, so Bengali inflections like `গানের` still match `গান`, while `class` does not match
 * `ass`. Arabic terms also match with the definite article `ال` attached (`الجنس` matches
 * `جنس`) and with a feminine `ة` suffix (`رقصة` matches `رقص`).
 */
class FamilyTextMatcher(
    private val hardTerms: List<String>,
    private val entertainmentTerms: List<String>,
    private val customTerms: Set<String>,
    private val viceTerms: List<String> = emptyList(),
) {
    /**
     * Compiled lazily, not in the constructor: the merged lists hold ~2,300 terms and
     * each becomes a large alternation, so building these patterns at construction
     * stalls the main thread when the repository is injected at startup. The first
     * match runs on a background thread (feed load, search), and pays the one-time
     * compile there instead. `by lazy` is thread-safe, so concurrent first uses
     * compile once.
     */
    private val hardPattern by lazy { buildPattern(hardTerms) }
    private val entertainmentPattern by lazy { buildPattern(entertainmentTerms + customTerms) }
    private val entertainmentPatternExemptDrama by lazy {
        buildPattern(
            (entertainmentTerms + customTerms)
                .map { normalize(it) }
                .distinct()
                .filter { it !in DRAMA_AND_FILM_WORDS_NORMALIZED },
        )
    }
    private val vicePattern by lazy { buildPattern(viceTerms) }
    private val exemptionPattern by lazy { buildPattern(ISLAMIC_EXEMPTION_MARKERS) }
    private val stripPattern by lazy { buildPattern(STRIP_BEFORE_MATCH) }

    /**
     * True when no tier has any words, so nothing can match. Read from the term lists,
     * not the patterns, so asking it never triggers a compile: the feed's exclusions
     * check this on the launch path before any text is matched.
     */
    val isEmpty: Boolean
        get() =
            hardTerms.isEmpty() && entertainmentTerms.isEmpty() &&
                customTerms.isEmpty() && viceTerms.isEmpty()

    /** The hard tier only: explicit/sexual terms. */
    fun blocksHard(text: String): Boolean = hardPattern?.containsMatchIn(strippedNormalized(text)) == true

    /**
     * The vice tier only: gambling/alcohol/drugs, unless the text is a lecture about the sin
     * (it carries an [ISLAMIC_EXEMPTION_MARKERS] term).
     */
    fun blocksVice(text: String): Boolean {
        val stripped = strippedNormalized(text)
        return vicePattern?.containsMatchIn(stripped) == true &&
            exemptionPattern?.containsMatchIn(stripped) != true
    }

    /**
     * The entertainment tier only: music/song/dance/movie/drama and translations, plus the
     * title regexes. When [exemptDrama] is true (the matched topic is halal drama), the drama
     * sets and generic film words are dropped and the drama+episode regex is skipped. When
     * the text is a vice-exempt lecture, the short-root regexes are skipped too.
     */
    fun blocksEntertainment(
        text: String,
        exemptDrama: Boolean = false,
    ): Boolean = blocksEntertainmentNormalized(strippedNormalized(text), exemptDrama)

    /** True when the text carries a blocked emoji. */
    fun containsBlockedEmoji(text: String): Boolean = BLOCKED_EMOJIS.any { normalize(text).contains(it) }

    /** True when the (non-whitelisted) channel name carries a blocked substring. */
    fun isBlockedChannelName(channelName: String): Boolean {
        val normalized = normalize(channelName)
        return BLOCKED_CHANNEL_SUBSTRINGS.any { normalized.contains(it) }
    }

    /**
     * Whether a video's title or description is blocked. The hard tier always applies, then
     * the vice tier (with its lecture exemption); the entertainment tier, the emoji check and
     * the channel-name check apply when the channel is not whitelisted — and the entertainment
     * tier and the emoji check also when it is a whitelisted news channel, which is approved
     * for news, not for music/drama uploads.
     */
    fun blocks(
        title: String,
        description: String,
        channelId: String,
        handle: String?,
        channelMatcher: FamilyChannelMatcher,
        channelName: String = "",
        exemptDrama: Boolean = false,
    ): Boolean {
        val text = strippedNormalized("$title\n$description")
        if (hardPattern?.containsMatchIn(text) == true) return true
        val viceMatches = vicePattern?.containsMatchIn(text) == true
        val viceExempt = isViceExempt(text)
        if (viceMatches && !viceExempt) return true
        val whitelisted = channelMatcher.allows(channelId, handle)
        if (whitelisted && !channelMatcher.isNewsChannel(channelId, handle)) return false
        if (!whitelisted && channelName.isNotBlank() && isBlockedChannelName(channelName)) {
            return true
        }
        if (BLOCKED_EMOJIS.any { text.contains(it) }) return true
        val pattern = if (exemptDrama) entertainmentPatternExemptDrama else entertainmentPattern
        if (pattern?.containsMatchIn(text) == true) return true
        return matchesTitleRegexes(text, exemptDrama, viceExempt)
    }

    /**
     * Whether a Settings search query is refused outright: any tier matches, no results.
     * Strict — no topic relaxation here; the open-search gate in SearchViewModel relaxes the
     * entertainment and vice tiers when the query itself matches an approved topic.
     */
    fun blocksQuery(query: String): Boolean {
        val text = strippedNormalized(query)
        if (hardPattern?.containsMatchIn(text) == true) return true
        if (vicePattern?.containsMatchIn(text) == true &&
            exemptionPattern?.containsMatchIn(text) != true
        ) {
            return true
        }
        if (BLOCKED_EMOJIS.any { text.contains(it) }) return true
        return blocksEntertainmentNormalized(text, exemptDrama = false)
    }

    /** The halal negations stripped, then normalized — the text every tier matches against. */
    private fun strippedNormalized(text: String): String {
        val normalized = normalize(text)
        return stripPattern?.replace(normalized, "") ?: normalized
    }

    private fun blocksEntertainmentNormalized(
        text: String,
        exemptDrama: Boolean,
    ): Boolean {
        val pattern = if (exemptDrama) entertainmentPatternExemptDrama else entertainmentPattern
        if (pattern?.containsMatchIn(text) == true) return true
        return matchesTitleRegexes(text, exemptDrama, isViceExempt(text))
    }

    /**
     * True when the text is a lecture about a sin: it carries an
     * [ISLAMIC_EXEMPTION_MARKERS] term and matches a vice word or a vice root
     * that lives only in the short-root title regexes ("মদ", "beer", ...).
     * Those roots cannot join the vice word list: the whole-word boundary
     * allows a following combining mark, so bare "মদ" would also match
     * "মদিনা" — they are checked with the regexes' own explicit boundaries
     * ([VICE_ROOT_PATTERN]) instead.
     */
    private fun isViceExempt(text: String): Boolean =
        exemptionPattern?.containsMatchIn(text) == true &&
            (vicePattern?.containsMatchIn(text) == true || VICE_ROOT_PATTERN.containsMatchIn(text))

    /**
     * The six title regexes. Index 5 (drama/serial + episode number) is skipped under the
     * drama carve-out; indices 0–1 (the short roots, including vice roots) are skipped when
     * the text is a vice-exempt lecture.
     */
    private fun matchesTitleRegexes(
        text: String,
        exemptDrama: Boolean,
        skipShortRootRegexes: Boolean = false,
    ): Boolean {
        TITLE_REGEXES.forEachIndexed { index, regex ->
            if (index == 5 && exemptDrama) return@forEachIndexed
            if (index < 2 && skipShortRootRegexes) return@forEachIndexed
            if (regex.containsMatchIn(text)) return true
        }
        return false
    }

    companion object {
        /**
         * Leet substitutions. Declared first in this companion object on purpose: [normalize]
         * reads it, and [DEFAULT]'s construction calls [normalize] — a property initialized
         * before this map would see it as null and crash class initialization.
         */
        private val LEET_FOLD =
            mapOf(
                '0' to 'o',
                '1' to 'i',
                '3' to 'e',
                '4' to 'a',
                '5' to 's',
                '7' to 't',
                '8' to 'b',
                '@' to 'a',
                '$' to 's',
            )

        /** No words at all; only used where a matcher must exist before the repository loads. */
        val EMPTY = FamilyTextMatcher(emptyList(), emptyList(), emptySet())

        /** Explicit/sexual terms: the built-in list plus Gemini's ADULT_ROMANCE sets. */
        val DEFAULT_HARD_WORDS: List<String> =
            listOf(
                // English
                "sex",
                "sexy",
                "porn",
                "pornography",
                "nude",
                "nudity",
                "naked",
                "erotic",
                "erotica",
                "xxx",
                "pussy",
                "dick",
                "cock",
                "boobs",
                "tits",
                "ass",
                "fuck",
                "fucking",
                "bitch",
                "slut",
                "whore",
                "horny",
                "orgasm",
                "masturbate",
                "blowjob",
                "handjob",
                "anal",
                "vagina",
                "penis",
                "stripper",
                "escort",
                "prostitute",
                "hentai",
                "camgirl",
                "onlyfans",
                "sexting",
                "rape",
                // Bangla
                "চুদাচুদি",
                "চোদা",
                "চুদা",
                "সেক্স",
                "পর্ন",
                "নগ্ন",
                "উলঙ্গ",
                "যৌন",
                "ধর্ষণ",
                "ব্লু ফিল্ম",
                "ন্যাংটা",
                "গুদ",
                "ধোন",
                "বাঁড়া",
                "মাগি",
                "খানকি",
                "বেশ্যা",
                // Hindi/Urdu, roman + Devanagari + Urdu script
                "nanga",
                "नंगा",
                "ننگا",
                "चुदाई",
                "चोदना",
                "چودنا",
                "गांड",
                "گانڈ",
                "लंड",
                "لنڈ",
                "भोसड़ी",
                "रंडी",
                "رنڈی",
                "सेक्स",
                "سیکس",
                "पोर्न",
                "پورن",
                "فحش",
                "عریاں",
                "جنسی",
                // Arabic
                "جنس",
                "إباحية",
                "اباحي",
                "عاري",
                "عارية",
                "عري",
                "اغتصاب",
                "عاهرة",
                "قضيب",
                "فرج",
            ) + ADULT_ROMANCE_EN + ADULT_ROMANCE_BN + ADULT_ROMANCE_AR +
                ADULT_ROMANCE_UR + ADULT_ROMANCE_HI_UR_TRANSLIT

        /** Entertainment terms: the built-in list plus Gemini's MUSIC/MOVIES/DRAMA/DANCE_DJ sets. */
        val DEFAULT_ENTERTAINMENT_WORDS: List<String> =
            listOf(
                "music",
                "song",
                "songs",
                "dance",
                "dancing",
                "movie",
                "movies",
                "film",
                "films",
                "drama",
                "dramas",
                "dj",
                "remix",
                "remixes",
                "concert",
                "bollywood",
                "hollywood",
                "গান",
                "নাচ",
                "সিনেমা",
                "নাটক",
                "गाना",
                "नाच",
                "फिल्म",
                "ड्रामा",
                "ڈرامہ",
                "فلم",
                "گانا",
                "ناچ",
                "موسیقی",
                "موسيقى",
                "أغنية",
                "اغنية",
                "رقص",
                "مسلسل",
                "سينما",
            ) + MUSIC_EN + MUSIC_BN + MUSIC_AR + MUSIC_UR + MUSIC_HI_UR_TRANSLIT +
                MOVIES_EN + MOVIES_BN + MOVIES_AR + MOVIES_UR + MOVIES_HI_UR_TRANSLIT +
                DRAMA_EN + DRAMA_BN + DRAMA_AR + DRAMA_UR + DRAMA_HI_UR_TRANSLIT +
                DANCE_DJ_EN + DANCE_DJ_BN + DANCE_DJ_AR + DANCE_DJ_UR + DANCE_DJ_HI_UR_TRANSLIT

        /** Gambling/alcohol/drugs terms: Gemini's VICE sets. */
        val DEFAULT_VICE_WORDS: List<String> =
            listOf<String>() + VICE_EN + VICE_BN + VICE_AR + VICE_UR + VICE_HI_UR_TRANSLIT

        /**
         * The drama sets plus the generic film words. The drama carve-out drops exactly these
         * from the entertainment pattern; studio/brand/genre words (hollywood, bollywood,
         * netflix, trailer, concert, song) are NOT in here and still block.
         */
        private val DRAMA_AND_FILM_WORDS: Set<String> =
            DRAMA_EN + DRAMA_BN + DRAMA_AR + DRAMA_UR + DRAMA_HI_UR_TRANSLIT +
                setOf(
                    "movie",
                    "movies",
                    "film",
                    "films",
                    "cinema",
                    "telefilm",
                    "webfilm",
                    "web film",
                    "short film",
                    "সিনেমা",
                    "চলচ্চিত্র",
                    "ফিল্ম",
                    "फिल्म",
                    "सिनेमा",
                    "चलचित्र",
                    "فيلم",
                    "سينما",
                    "فلم",
                    "سینما",
                )

        private val DRAMA_AND_FILM_WORDS_NORMALIZED: Set<String> =
            DRAMA_AND_FILM_WORDS.map { normalize(it) }.toSet()

        val DEFAULT =
            FamilyTextMatcher(
                DEFAULT_HARD_WORDS,
                DEFAULT_ENTERTAINMENT_WORDS,
                emptySet(),
                DEFAULT_VICE_WORDS,
            )

        /** NFC, lowercase, no Arabic diacritics or tatweel, leet-folded. */
        fun normalize(text: String): String {
            val folded = StringBuilder(text.length)
            Normalizer.normalize(text, Normalizer.Form.NFC).lowercase(Locale.ROOT).forEach { char ->
                when {
                    char in '\u064B'..'\u0652' || char == '\u0670' || char == '\u0640' -> Unit
                    char in LEET_FOLD -> folded.append(LEET_FOLD.getValue(char))
                    else -> folded.append(char)
                }
            }
            return folded.toString()
        }

        internal fun buildPattern(terms: Collection<String>): Regex? {
            val alternatives =
                terms
                    .map { normalize(it) }
                    .filter { it.isNotBlank() }
                    .distinct()
                    // Longest first, so a longer term is tried before its own prefix
                    // ("fucking" before "fuck"): the trailing boundary would reject the prefix.
                    .sortedByDescending { it.length }
                    .map { term -> termToPattern(term) }
            if (alternatives.isEmpty()) return null
            // Leading boundary rejects a preceding letter, mark or digit; trailing boundary
            // allows a following combining mark (Bengali inflection), but no letter, digit or
            // virama — a virama means the word continues with another consonant ("গান্ধী" is
            // Gandhi, not "গান" + something).
            return Regex(
                "(?<![\\p{L}\\p{M}\\p{N}])(?:${alternatives.joinToString("|")})(?![\\p{L}\\p{N}\\u09CD\\u094D])",
            )
        }

        /**
         * One term as a regex. The first letter is literal; every later letter may be separated
         * from the previous one by punctuation or spaces, and may itself be replaced by
         * punctuation (`s*x`, `s e x`). The separators only ever match non-letters, so a term
         * cannot span across other words (`d xyz j` never matches `dj`).
         */
        private fun termToPattern(term: String): String {
            val isArabic = term.any { it in '\u0600'..'\u06FF' }
            val isBengali = term.any { it in '\u0980'..'\u09FF' }
            val isDevanagari = term.any { it in '\u0900'..'\u097F' }
            val words = term.split(' ').filter { it.isNotEmpty() }
            val joined =
                words.joinToString("[^\\p{L}\\p{M}\\p{N}]+") { word -> wordToPattern(word) }
            // Arabic words also match with the definite article attached (الجنس for جنس) and
            // with a feminine ة suffix (رقصة for رقص).
            val article = if (isArabic) "(?:ال)?" else ""
            val arabicSuffix = if (isArabic) "(?:ة)?" else ""
            // Bengali nouns take -টি/-টা/-গুলো/-গুলি/-খানা/-কে/-তে ("গানটি" is "the song");
            // Devanagari verb stems take -ना/-ने/-नो/-ता/-ते/-ती ("नाचना" is "to dance").
            val suffix =
                when {
                    isBengali -> "(?:টি|টা|গুলো|গুলি|খানা|কে|তে)?"
                    isDevanagari -> "(?:ना|ने|नो|ता|ते|ती)?"
                    else -> ""
                }
            return article + joined + arabicSuffix + suffix
        }

        private fun wordToPattern(word: String): String {
            val chars = word.map { Regex.escape(it.toString()) }
            if (chars.size == 1) return chars.first()
            val first = chars.first()
            val rest =
                chars.drop(1).joinToString("") { letter ->
                    "[^\\p{L}\\p{M}\\p{N}]*?(?:$letter|[\\p{P}\\p{S}])"
                }
            return first + rest
        }
    }
}
