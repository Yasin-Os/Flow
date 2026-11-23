package io.github.aedev.flow.data.contentfilter.family

import java.text.Normalizer
import java.util.Locale

/**
 * Two tiers of blocked words, matched after normalization:
 *
 * - **Hard tier** — explicit/sexual terms in English, Bangla, Hindi/Urdu (roman, Devanagari and
 *   Urdu script) and Arabic. Applies to every video, whitelisted channels included, and to
 *   Settings search queries.
 * - **Entertainment tier** — music, song, dance, movie, drama, DJ, remix, film and their
 *   translations. Applies only to videos from channels that are NOT whitelisted: the approved
 *   ghazal channels title their own uploads "Bangla Islamic Song" and "Ramjan Music Video", so
 *   these words must never block a whitelisted channel. Non-whitelisted videos are already
 *   hidden on ranked surfaces; this tier matters at the player and on channel pages.
 *
 * Deliberately NOT in either list: `গজল` (ghazal), `সুরা` (surah), `নিকাহ`/`نكاح` (marriage),
 * `চটি` (also "slipper" in Bangla), and `hot` (too generic) — and also `lust`, `breast`,
 * `seduce`, `زنا` (zina) and `دعارة` (prostitution): those are ordinary words in Islamic
 * lectures about controlling desire and avoiding sin, and the hard tier applies to whitelisted
 * channels too, so they would hide legitimate waz titles. A title like "গজল | Holy Tune"
 * or "সুরা আল-বাকারা" must pass untouched.
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
    hardTerms: List<String>,
    entertainmentTerms: List<String>,
    customTerms: Set<String>,
) {
    private val hardPattern = buildPattern(hardTerms)
    private val entertainmentPattern = buildPattern(entertainmentTerms + customTerms)

    /** True when no tier has any words, so nothing can match. */
    val isEmpty: Boolean get() = hardPattern == null && entertainmentPattern == null

    /** The hard tier only: explicit/sexual terms. */
    fun blocksHard(text: String): Boolean = hardPattern?.containsMatchIn(normalize(text)) == true

    /** The entertainment tier only: music/song/dance/movie/drama and translations. */
    fun blocksEntertainment(text: String): Boolean =
        entertainmentPattern?.containsMatchIn(normalize(text)) == true

    /**
     * Whether a video's title or description is blocked. The hard tier always applies; the
     * entertainment tier only when the channel is not whitelisted.
     */
    fun blocks(
        title: String,
        description: String,
        channelId: String,
        handle: String?,
        channelMatcher: FamilyChannelMatcher,
    ): Boolean {
        val text = "$title\n$description"
        if (blocksHard(text)) return true
        if (channelMatcher.allows(channelId, handle)) return false
        return blocksEntertainment(text)
    }

    /** Whether a Settings search query is refused outright: any tier matches, no results. */
    fun blocksQuery(query: String): Boolean = blocksHard(query) || blocksEntertainment(query)

    companion object {
        /** No words at all; only used where a matcher must exist before the repository loads. */
        val EMPTY = FamilyTextMatcher(emptyList(), emptyList(), emptySet())

        /** Explicit/sexual terms. */
        val DEFAULT_HARD_WORDS: List<String> =
            listOf(
                // English
                "sex", "sexy", "porn", "pornography", "nude", "nudity", "naked", "erotic", "erotica",
                "xxx", "pussy", "dick", "cock", "boobs", "tits", "ass", "fuck", "fucking", "bitch",
                "slut", "whore", "horny", "orgasm", "masturbate", "blowjob", "handjob", "anal",
                "vagina", "penis", "stripper", "escort", "prostitute",
                "hentai", "camgirl", "onlyfans", "sexting", "rape",
                // Bangla
                "চুদাচুদি", "চোদা", "চুদা", "সেক্স", "পর্ন", "নগ্ন", "উলঙ্গ", "যৌন", "ধর্ষণ",
                "ব্লু ফিল্ম", "ন্যাংটা", "গুদ", "ধোন", "বাঁড়া", "মাগি", "খানকি", "বেশ্যা",
                // Hindi/Urdu, roman + Devanagari + Urdu script
                "nanga", "नंगा", "ننگا", "चुदाई", "चोदना", "چودنا", "गांड", "گانڈ", "लंड", "لنڈ",
                "भोसड़ी", "रंडी", "رنڈی", "सेक्स", "سیکس", "पोर्न", "پورن",
                "فحش", "عریاں", "جنسی",
                // Arabic
                "جنس", "إباحية", "اباحي", "عاري", "عارية", "عري", "اغتصاب", "عاهرة",
                "قضيب", "فرج",
            )

        /** Entertainment terms. Never applied to a whitelisted channel's own videos. */
        val DEFAULT_ENTERTAINMENT_WORDS: List<String> =
            listOf(
                "music", "song", "songs", "dance", "dancing", "movie", "movies", "film", "films",
                "drama", "dramas", "dj", "remix", "remixes", "concert", "bollywood", "hollywood",
                "গান", "নাচ", "সিনেমা", "নাটক",
                "गाना", "नाच", "फिल्म", "ड्रामा",
                "ڈرامہ", "فلم", "گانا", "ناچ", "موسیقی", "موسيقى",
                "أغنية", "اغنية", "رقص", "مسلسل", "سينما",
            )

        val DEFAULT = FamilyTextMatcher(DEFAULT_HARD_WORDS, DEFAULT_ENTERTAINMENT_WORDS, emptySet())

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

        private fun buildPattern(terms: Collection<String>): Regex? {
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
