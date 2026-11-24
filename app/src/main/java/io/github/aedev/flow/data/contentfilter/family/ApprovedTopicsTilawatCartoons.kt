package io.github.aedev.flow.data.contentfilter.family

/**
 * Tilawat and Islamic cartoon keywords.
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

val TILAWAT_AR =
    setOf(
        "تلاوة",
        "تلاوة خاشعة",
        "تلاوة هادئة",
        "قرآن",
        "القرآن الكريم",
        "قرآن كريم",
        "قراءة",
        "تجويد",
        "سورة",
        "آية الكرسي",
        "جزء عم",
        "رقية شرعية",
        "الرقية الشرعية",
        "أذان",
        "اذان",
        "القارئ",
        "مشاري العفاسي",
        "السديس",
        "ماهر المعيقلي",
        "عبد الباسط",
        "ياسر الدوسري",
        "إسلام صبحي",
        "رعد الكردي",
        "سعد الغامدي",
        "ناصر القطامي",
    )

val TILAWAT_BN =
    setOf(
        "তেলাওয়াত",
        "তিলাওয়াত",
        "কুরআন",
        "কোরআন",
        "কুরআন তেলাওয়াত",
        "কোরআন তেলাওয়াত",
        "কিরাত",
        "কেরাত",
        "তাজবীদ",
        "তাজবিদ",
        "সূরা",
        "সুরা",
        "আয়াত",
        "আয়াতুল কুরসি",
        "আমপারা",
        "৩০ পারা",
        "রুকইয়াহ",
        "রুকিয়াহ",
        "আযান",
        "আজান",
        "ক্বারী",
        "কারী",
        "হাফেজ",
        "মিশারি",
        "সুদাইস",
        "আব্দুল বাসিত",
        "quran telawat",
        "koran telawat",
        "surah yaseen",
        "surah rahman",
        "surah mulk",
        "surah kahf",
        "surah baqarah",
        "surah waqiah",
    )

val TILAWAT_EN =
    setOf(
        "tilawat",
        "tilawah",
        "quran",
        "quraan",
        "koran",
        "holy quran",
        "quran recitation",
        "qiraat",
        "qirat",
        "tajweed",
        "tajwid",
        "surah",
        "ayah",
        "ayatul kursi",
        "ayat al kursi",
        "juz amma",
        "30 para",
        "sipara",
        "ampara",
        "ruqyah",
        "ruqya",
        "azan",
        "adhan",
        "adhaan",
        "qari",
        "hafez",
        "hafiz",
        "mishary",
        "alafasy",
        "sudais",
        "shuraim",
        "abdul basit",
        "maher al muaiqly",
        "yasser al dosari",
        "saad al ghamdi",
        "nasser al qatami",
        "idris abkar",
        "abdul rahman mossad",
        "islam sobhi",
        "omar hisham",
        "raad al kurdi",
        "fatih seferagic",
        "qari ahmad bin yusuf",
    )

val TILAWAT_HI_UR_TRANSLIT =
    setOf(
        "tilawat e quran",
        "quran sharif",
        "quran pak",
        "quran majeed",
        "surah fatiha",
        "surah ikhlas",
        "surah yasin",
        "surah ar rahman",
        "manzil dua",
        "ruqyah shariah",
        "beautiful azan",
        "fajr azan",
    )

val TILAWAT_UR =
    setOf(
        "تلاوت",
        "تلاوت قرآن",
        "قرآن مجید",
        "قرآن پاک",
        "قرآن کریم",
        "قرات",
        "قراءت",
        "تجوید",
        "سورۃ",
        "سورہ",
        "آیت الکرسی",
        "عم پارہ",
        "رقیہ شرعیہ",
        "اذان",
        "قاری",
        "حافظ",
    )

val CARTOONS_AR =
    setOf(
        "كرتون إسلامي",
        "كرتون اسلامي",
        "أفلام كرتون إسلامية",
        "قصص الأنبياء للأطفال",
        "قصص القرآن للأطفال",
        "قرآن للأطفال",
        "تعليم الأطفال الإسلام",
        "عمر وهنا",
        "عمر وهناء",
        "اناشيد اسلامية للاطفال",
        "أناشيد إسلامية للأطفال",
    )

val CARTOONS_BN =
    setOf(
        "ইসলামিক কার্টুন",
        "ইসলামী কার্টুন",
        "ইসলামিক এনিমেশন",
        "ইসলামিক গল্প",
        "ইসলামী গল্প",
        "নবীদের গল্প",
        "নবীদের কাহিনী",
        "সাহাবীদের গল্প",
        "ছোটদের ইসলামিক",
        "শিশুদের ইসলামিক",
        "ওমর এন্ড হানা",
        "নুসসা",
        "ইসলামিক কিডস",
        "bangla islamic cartoon",
        "islami cartoon",
        "islamic golpo",
        "nobider golpo",
        "sahabider golpo",
        "chotoder islamic",
    )

val CARTOONS_EN =
    setOf(
        "islamic cartoon",
        "islamic cartoons",
        "muslim cartoon",
        "muslim kids",
        "islamic kids",
        "islamic animation",
        "prophet stories cartoon",
        "quran stories for kids",
        "islamic stories for kids",
        "islamic bedtime stories",
        "omar and hana",
        "omar & hana",
        "omar hana",
        "nussa",
        "nussa and rara",
        "one4kids",
        "zaky",
        "mini muslims",
        "alif and sofia",
        "alif & sofia",
        "adam's world",
        "bilal cartoon",
        "ali and sumaya",
        "iqra cartoon",
        "learn islam for kids",
        "quran for kids",
        "dua for kids",
        "salah for kids",
        "ramadan for kids",
    )

val CARTOONS_HI_UR_TRANSLIT =
    setOf(
        "muslim kids cartoon",
        "bachon ke islami",
        "nabiyon ke qisse",
        "islami kahani",
        "islamic kahani",
        "umar aur hana",
    )

val CARTOONS_UR =
    setOf(
        "اسلامی کارٹون",
        "اسلامک کارٹون",
        "بچوں کے اسلامی",
        "انبیاء کے قصے",
        "عمر اور ہنا",
        "اسلامی کہانیاں",
        "بچوں کی دعائیں",
    )
