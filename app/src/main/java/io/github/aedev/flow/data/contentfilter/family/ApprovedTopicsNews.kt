package io.github.aedev.flow.data.contentfilter.family

/**
 * News keywords.
 *
 * Approved-topic keywords for the family app's open search, supplied by Gemini
 * (Google AI Studio, khansaheba4325@gmail.com, chat "Filtering Content for
 * Islamic App", 2026-11-24, Reply 3). Terms are Gemini's verbatim. A video from a
 * channel that is not whitelisted is kept only when its title, description,
 * channel name or snippet matches one of these topics (see [FamilyTopicMatcher]).
 *
 * Generic words alone do NOT match a topic: "lecture", "cartoon", "comedy",
 * "drama", "song", "film", "story" and "kids" need Islamic anchoring ("islamic
 * cartoon", "হালাল কমেডি"). News words match alone; distinctive Islamic words
 * (waz, ghazal, tilawat, quran, allah, masjid, azan, ramadan, ...) match alone.
 */

val NEWS_AR =
    setOf(
        "أخبار",
        "اخبار",
        "خبر عاجل",
        "عاجل",
        "نشرة الأخبار",
        "آخر الأخبار",
        "أخبار اليوم",
        "أخبار العالم",
        "بث مباشر أخبار",
        "الجزيرة",
        "العربية",
        "بي بي سي عربي",
        "تي آر تي عربي",
        "سكاي نيوز عربية",
        "تقرير إخباري",
        "مؤتمر صحفي",
    )

val NEWS_BN =
    setOf(
        "খবর",
        "সংবাদ",
        "তাজা খবর",
        "সর্বশেষ খবর",
        "সর্বশেষ সংবাদ",
        "ব্রেকিং নিউজ",
        "আজকের খবর",
        "আজকের সংবাদ",
        "নিউজ",
        "লাইভ নিউজ",
        "বিশ্ব সংবাদ",
        "আন্তর্জাতিক সংবাদ",
        "বাংলাদেশের খবর",
        "দেশের খবর",
        "সংবাদ শিরোনাম",
        "সংবাদ সম্মেলন",
        "সমসাময়িক",
        "সময় টিভি",
        "সময় সংবাদ",
        "যমুনা টিভি",
        "যমুনা নিউজ",
        "ইন্ডিপেন্ডেন্ট টিভি",
        "একাত্তর টিভি",
        "চ্যানেল ২৪",
        "চ্যানেল 24",
        "ডিবিসি নিউজ",
        "এখন টিভি",
        "দেশ টিভি",
        "প্রথম আলো",
        "কালবেলা",
        "যুগান্তর",
        "ইত্তেফাক",
        "বিবিসি বাংলা",
        "আল জাজিরা",
        "ajker khobor",
        "bangla news",
        "bd khobor",
        "shongbad",
        "songbad",
    )

val NEWS_EN =
    setOf(
        "news",
        "breaking news",
        "latest news",
        "live news",
        "world news",
        "bangladesh news",
        "bd news",
        "dhaka news",
        "international news",
        "top news",
        "news bulletin",
        "news update",
        "news today",
        "headlines",
        "press briefing",
        "press conference",
        "current affairs",
        "geopolitics",
        "aljazeera",
        "al jazeera",
        "bbc news",
        "bbc bangla",
        "reuters",
        "ap news",
        "trt world",
        "middle east eye",
        "somoy tv",
        "somoy news",
        "jamuna tv",
        "jamuna news",
        "independent tv",
        "ekattor tv",
        "channel 24",
        "dbc news",
        "ekhon tv",
        "desh tv",
        "prothom alo",
        "daily star",
        "bdnews24",
        "jugantor",
        "ittefaq",
        "kalbela",
        "ntv news",
        "rtv news",
        "atn news",
        "channel i news",
        "geo news",
        "ary news",
        "dawn news",
    )

val NEWS_HI_UR_TRANSLIT =
    setOf(
        "taza khabar",
        "taaza khabar",
        "aaj ki khabar",
        "khabrein",
        "samachar",
        "mukhya samachar",
        "world khabar",
        "duniya ki khabar",
    )

val NEWS_UR =
    setOf(
        "خبریں",
        "تازہ ترین خبریں",
        "بریکنگ نیوز",
        "آج کی خبریں",
        "نیوز بلیٹن",
        "ہیڈلائنز",
        "جیو نیوز",
        "اے آر وائی نیوز",
        "ڈان نیوز",
        "دنیا نیوز",
        "سماء نیوز",
        "بی بی سی اردو",
        "الجزیرہ",
    )
