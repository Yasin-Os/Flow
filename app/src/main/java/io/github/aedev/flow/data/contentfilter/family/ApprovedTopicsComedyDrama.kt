package io.github.aedev.flow.data.contentfilter.family

/**
 * Halal comedy and halal drama keywords.
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

val HALAL_COMEDY_AR =
    setOf(
        "كوميديا حلال",
        "كوميديا إسلامية",
        "كوميديا اسلامية",
        "كوميديا هادفة",
        "طرائف إسلامية",
        "مقاطع مضحكة هادفة",
        "مسرحية هادفة",
        "مقالب حلال",
    )

val HALAL_COMEDY_BN =
    setOf(
        "হালাল কমেডি",
        "ইসলামিক কমেডি",
        "ইসলামী কমেডি",
        "ইসলামিক ফানি",
        "মাদ্রাসা ফানি",
        "ইসলামিক নাটিকা",
        "ইসলামী কৌতুক",
        "ইসলামিক কৌতুক",
        "হালাল বিনোদন",
        "ইসলামিক বিনোদন",
        "madrasa funny",
        "islami comedy",
        "islamic natika",
    )

val HALAL_COMEDY_EN =
    setOf(
        "halal comedy",
        "halal humor",
        "halal humour",
        "halal jokes",
        "halal skit",
        "halal funny",
        "islamic comedy",
        "islamic humor",
        "islamic skit",
        "islamic funny",
        "muslim comedy",
        "muslim humor",
        "muslim skit",
        "muslim funny",
        "clean comedy",
        "family friendly comedy",
        "halal standup",
        "muslim standup",
        "smile 2 jannah",
        "smile2jannah",
        "baba ali",
        "ramadan comedy",
        "eid comedy",
        "muslim dad jokes",
        "halal meme",
    )

val HALAL_COMEDY_HI_UR_TRANSLIT =
    setOf(
        "halal mazak",
        "islami mazak",
        "sabaq amoz comedy",
        "madrasa comedy",
        "halal funny video",
    )

val HALAL_COMEDY_UR =
    setOf(
        "حلال کامیڈی",
        "اسلامی کامیڈی",
        "اسلامک کامیڈی",
        "پاکیزہ مزاح",
        "اسلامی لطیفے",
        "سبق آموز مزاح",
    )

val HALAL_DRAMA_AR =
    setOf(
        "مسلسل إسلامي",
        "مسلسل اسلامي",
        "دراما إسلامية",
        "دراما اسلامية",
        "دراما هادفة",
        "فيلم إسلامي",
        "فيلم اسلامي",
        "مسلسل تاريخي إسلامي",
        "مسلسل عمر",
        "قيامة أرطغرل",
        "المؤسس عثمان",
        "صلاح الدين الأيوبي",
        "الإمام أحمد بن حنبل",
        "ألب أرسلان",
        "فيلم الرسالة",
        "مسلسل خالد بن الوليد",
    )

val HALAL_DRAMA_BN =
    setOf(
        "ইসলামিক নাটক",
        "ইসলামী নাটক",
        "হালাল নাটক",
        "ইসলামিক শর্ট ফিল্ম",
        "ইসলামী শর্ট ফিল্ম",
        "ইসলামিক ড্রামা",
        "ইসলামী চলচ্চিত্র",
        "ইসলামিক মুভি",
        "ইসলামিক সিরিজ",
        "শিক্ষণীয় নাটক",
        "দিরিলিস আরতুগ্রুল",
        "আরতুগ্রুল গাজী",
        "কুরুলুস ওসমান",
        "উসমান গাজী",
        "সালাহউদ্দিন আইয়ুবী",
        "আল্প আরসালান",
        "উমর সিরিজ",
        "ইউনুস এমরে",
        "bangla islamic natok",
        "shikhoniyo natok",
        "ertugrul bangla",
        "kurulus osman bangla",
        "salahuddin bangla",
    )

val HALAL_DRAMA_EN =
    setOf(
        "halal drama",
        "islamic drama",
        "islamic series",
        "islamic short film",
        "islamic movie",
        "islamic film",
        "islamic natok",
        "islami natok",
        "moral drama",
        "ertugrul",
        "dirilis ertugrul",
        "kurulus osman",
        "kuruluş osman",
        "salahuddin ayyubi",
        "salahuddin al ayyubi",
        "kudus fatihi",
        "alparslan",
        "uyanis buyuk selcuklu",
        "barbaroslar",
        "mehmed fetihler sultani",
        "omar series",
        "umar series",
        "imam ahmad bin hanbal",
        "yunus emre",
        "payitaht abdulhamid",
        "khalid bin walid series",
        "the message movie",
        "bilal movie",
        "journey to mecca",
    )

val HALAL_DRAMA_HI_UR_TRANSLIT =
    setOf(
        "islami drama",
        "sabaq amoz drama",
        "ertugrul ghazi",
        "ertugrul urdu",
        "kurulus osman urdu",
        "salahuddin ayyubi urdu",
        "alparslan urdu",
        "umar farooq series",
        "islami short film",
    )

val HALAL_DRAMA_UR =
    setOf(
        "اسلامی ڈرامہ",
        "اسلامک ڈرامہ",
        "سبق آموز ڈرامہ",
        "اسلامی سیریز",
        "اسلامی فلم",
        "ارطغرل غازی",
        "کرولس عثمان",
        "صلاح الدین ایوبی",
        "الپ ارسلان",
        "عمر سیریز",
        "یونس ایمرے",
    )
