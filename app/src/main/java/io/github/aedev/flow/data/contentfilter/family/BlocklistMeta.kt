package io.github.aedev.flow.data.contentfilter.family

/**
 * Strip list, exemption markers, title regexes, emojis, channel substrings, categories.
 *
 * Blocked-word lists for the family app, supplied by Gemini (Google AI Studio,
 * khansaheba4325@gmail.com, chat "Filtering Content for Islamic App", 2026-11-24,
 * Reply 2). Terms are Gemini's verbatim, except the deviations noted in the file.
 *
 * Matching is whole-word with boundaries (see [FamilyTextMatcher]): a term never
 * matches inside another word, so short roots are safe here — "মদ" does not match
 * "আহমদ", "গান" does not match "বাগান", "dance" does not match "guidance".
 * The short roots that are unsafe even as whole words live in [TITLE_REGEXES].
 *
 * The machinery around Gemini's word lists: what is stripped before matching, what
 * exempts a vice-only lecture, the short-root regexes, and the category sets.
 */

val STRIP_BEFORE_MATCH =
    setOf(
        "no music",
        "without music",
        "non music",
        "non-music",
        "music free",
        "music-free",
        "vocals only",
        "vocal only",
        "মিউজিক ছাড়া",
        "বিনা মিউজিক",
        "বিনা মিউজিকে",
        "বাদ্যযন্ত্র ছাড়া",
        "ইসলামী সংগীত",
        "ইসলামিক সংগীত",
        "ইসলামী সঙ্গীত",
        "ইসলামিক সঙ্গীত",
        "ইসলামী গান",
        "ইসলামিক গান",
        "islami gaan",
        "islamic gaan",
        "islami sangeet",
        "islamic sangeet",
        "بدون موسيقى",
        "بلا موسيقى",
        "بغير موسيقى",
        "بغیر میوزک",
        "بغیر موسیقی",
    )

// Vice-Only Exemption Markers (Islamic Rulings/Lectures).
// Added to Gemini's list: "হারাম", "haram" (it had Arabic حرام only).
val ISLAMIC_EXEMPTION_MARKERS =
    setOf(
        "ওয়াজ",
        "তাফসীর",
        "তাফসির",
        "খুতবা",
        "ফতোয়া",
        "মাসআলা",
        "বয়ান",
        "হাদিস",
        "কুরআন",
        "গজল",
        "নাত",
        "হামদ",
        "তেলাওয়াত",
        "waz",
        "waaz",
        "tafsir",
        "tafseer",
        "khutbah",
        "khutba",
        "fatwa",
        "bayan",
        "hadith",
        "quran",
        "surah",
        "tilawat",
        "nasheed",
        "naat",
        "gojol",
        "حكم",
        "فتوى",
        "خطبة",
        "تفسير",
        "حرام",
        "بیان",
        "فتوی",
        "تفسیر",
        "تلاوت",
        "হারাম",
        "haram",
    )

// Title Regex Patterns. Regex 1 drops Gemini's "hot": deliberately excluded
// (too generic; whitelisted news titles like "Hot News" would be blocked).
val TITLE_REGEXES =
    listOf(
        Regex(
            """(?i)(?:^|[\s|,.\-\[\]()/#!?])(dj|ost|mv|dance|edm|rap|pop|beer|wine|rum|gin|pub|weed|kiss|cid""" +
                """|sex|gay|daru|jua|satta|gana)(?:$|[\s|,.\-\[\]()/#!?])""",
        ),
        Regex("""(?:^|[\s|,.\-\[\]()/#!?।؟])(গান|গানের|নাচ|মদ|گانا|گانے|ناچ|جوا|بيرة|عاري|خمر)(?:$|[\s|,.\-\[\]()/#!?।؟])"""),
        Regex("""(?i)\b(ft|feat)\.?\s+\p{L}+"""),
        Regex(
            """(?i)[\[(]\s*(official|full|new|latest|4k|hd)?\s*(music\s+)?(video|audio|mv|lyric|lyrical|song""" +
                """|trailer|teaser)\s*[\])]""",
        ),
        Regex("""(?i)\bs\d{1,2}\s*e\d{1,3}\b"""),
        Regex("""(?i)\b(natok|drama|serial)\b.*\b(ep|episode)\s*\.?\s*\d+"""),
    )

// Blocked Title Emojis (microphone U+1F3A4 intentionally excluded).
val BLOCKED_EMOJIS =
    setOf(
        "🎵",
        "🎶",
        "🎸",
        "🎷",
        "🎺",
        "🎻",
        "🥁",
        "🎹",
        "💃",
        "🕺",
        "🍺",
        "🍻",
        "🍷",
        "🍸",
        "🍹",
        "🍾",
        "🥂",
        "🎰",
        "🃏",
        "🔞",
        "👙",
        "💋",
        "🍆",
        "🍑",
    )

// Blocked Channel Name Substrings (channelName only, never whitelisted).
val BLOCKED_CHANNEL_SUBSTRINGS =
    setOf(
        "music",
        "records",
        "vevo",
        "films",
        "movies",
        "cinema",
        "entertainment",
        "natok",
        "dramas",
        "telefilm",
        "pictures",
        "cineplex",
        "jukebox",
        "lofi",
        "remix",
        "dj ",
        "- topic",
        "মিউজিক",
        "নাটক",
        "সিনেমা",
        "এন্টারটেইনমেন্ট",
        "فلم",
        "میوزک",
        "موسيقى",
        "أغاني",
        "اغاني",
        "أفلام",
        "افلام",
        "مسلسلات",
        "g-series",
        "g series",
        "cd choice",
        "eagle music",
        "sangeeta",
        "soundtek",
        "laser vision",
        "cmv",
        "dhruba music",
        "chorki",
        "hoichoi",
        "bongo",
        "rtv drama",
        "ntv natok",
        "channel i drama",
        "maasranga drama",
        "nagorik natok",
        "sultan entertainment",
        "cinematic",
        "bioscope",
        "t-series",
        "zee music",
        "sony music",
        "saregama",
        "tips official",
        "yrf",
        "shemaroo",
        "goldmines",
        "star jalsha",
        "zee bangla",
        "colors bangla",
        "sun bangla",
        "star plus",
        "colors tv",
        "sony sab",
        "set india",
        "hum tv",
        "ary digital",
        "har pal geo",
        "geo tv",
        "coke studio",
        "ullu",
        "altbalaji",
    )

// YouTube Categories
val BLOCKED_CATEGORIES_STRICT =
    setOf(
        "Music",
        "Film & Animation",
        "Movies",
        "Short Movies",
        "Drama",
        "Shows",
        "Trailers",
        "Anime/Animation",
        "Action/Adventure",
        "Horror",
        "Sci-Fi/Fantasy",
        "Thriller",
        "Classics",
    )

val BLOCKED_CATEGORIES_MODERATE =
    setOf(
        "Entertainment",
        "Comedy",
        "Gaming",
    )
