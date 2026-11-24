# Family YouTube — approved channel whitelist

The family app shows videos **only** from the channels below. Nothing else gets
access: the home feed, search, Shorts, and the player all filter by this list, and
the player blocks any video whose channel is not on it. Every entry is keyed by its
verified YouTube channel id (`UC…`); the handle is a fallback only. A blank handle
means no handle could be confirmed — the id is the match key.

**Counts (129 channels): waz 48 · ghazal 44 · tilawat 22 · cartoons 8 · comedy 5 · drama 2**

The shipped seed list is `app/src/main/java/io/github/aedev/flow/data/contentfilter/family/FamilySeedChannels.kt`.
Existing installs gain new channels through the seed merge (`SEED_VERSION = 2`); a
channel the PIN holder removed is never re-added.

## Waz — 48

| Channel | Handle | Channel id |
|---|---|---|
| Mizanur Rahman Azhari | @drmizanurrahmanazharichannel | `UCxStLx7yb96MGBfIMo20x7Q` |
| Ahmadullah | @sheikhahmadullahofficial | `UCuxth2BimHUigZ344JhcFPw` |
| Abdul Hi Muhammad Saifullah | @abdulhisaifullahofficial | `UCqyY3LIJtTMaeoI3Tok_9Kg` |
| Abu Taha Muhammad Adnan — Taw Haa TV | @tawhaatv | `UCVhUNnaQ0S7ywSApsc1Hx0Q` |
| Mufti Amir Hamza | — | `UCNCSr94zdcuzzRF33HT81QA` |
| Maulana Rafiqul Islam Madani | — | `UCBWaQNUrOaMq7J5G5MYQ8dA` |
| Jamia Talimia Madrasa Dhaka (Hafizur Rahman Siddiqui's madrasa) | — | `UCk1k1nrRAnGh7zsV0aZrPHQ` |
| Hefazat Media (organisation channel) | — | `UC4sW8VpBP25iDVXabbHla4Q` |
| Dr. Abdullah Jahangir | @SunnahTrust | `UCWuvzUF7ZcRCAsWswzjNLbw` |
| Maulana Tariq Jamil | @TariqJamilOfficial | `UCct5L-DaVYK8UtmqGU8gJtw` |
| Mufti Menk | @muftimenkofficial | `UCNB_OaI4524fASt8h0IL8dw` |
| Nouman Ali Khan — Bayyinah | @bayyinah | `UCRtiU-lpcBSi-ipFKyfIkug` |
| Yasir Qadhi | @yasirqadhi | `UClUa7-iHJNKEM2e_zWYSPQg` |
| Omar Suleiman Personal | @OmarSuleimanPersonal | `UCtm8rtofLSnaIBi3noB0INg` |
| Yaqeen Institute | @yaqeeninstituteofficial | `UC3vHW2h22WE-pNi5WJtRIjg` |
| Dr Zakir Naik | @drzakirchannel | `UC3YmP7nqf514I1zh1eVbzrA` |
| Mufti Tariq Masood | @muftitariqmasood | `UCC3pnIpWMVail5J8mvYEiZA` |
| Mufti Tariq Masood Speeches | @muftitariqmasoodspeeches | `UCumT1HMTXegYzESyPKH3pVg` |
| Anisur Rahman Ashrafi | @anisurrahmanashrafiofficial | `UCxBsm13Dkj4oIeCiVGxUtPw` |
| Bilal Philips | @aabphilips | `UCk1-R7Mmzd7iikof378dcuA` |
| Ajmal Raza Qadri | @muhammdajmalrazaqadriofficial | `UCxhWp1FeSSx6qbb2f4ohoBg` |
| Assim Al-Hakeem | @assimalhakeem | `UCWsdcrre0WbCWML_PnuzoAg` |
| DarulIftaaUK | @DarulIftaaUK | `UCYfp61fUZLuAZyO4MgrJZ_Q` |
| Zaytuna College | @zaytunacollege | `UC88lF82aS_VW3oJe7GV5zsA` |
| ZamZam Academy | @ZamZamAcademy | `UC__HMOrvqZ6NwPuDG_2Fmow` |
| Ask Mufti Tariq Masood | @askmuftitariqmasood | `UCnAsQQqfuBi1NJ8r4wcpr3A` |
| Mufti Tariq Masood Special | @muftitariqmasoodspecial | `UC-Vsetv02QQ8ALtwoZTwdDQ` |
| OnePath Network | @onepathnetwork | `UCQHRLH8RQIrdGWMhf5heWiA` |
| Digital Mimbar | @khalifahklothing | `UCyxM7MgZkDe5stZpb6CiOcA` |
| Jannah Institute | @jannahinstitute | `UCx1Zr8zUKUHUtHEVq09b5vg` |
| FreeQuranEducation | @freequraneducation | `UCNdUFOtzSx3FS71pHkQLxhQ` |
| Saad Tasleem | @saadtasleem | `UCGzdb458mefm-8w-kV_qv6g` |
| MercifulServant | @themercifulservant | `UCHGAqdQBKTVON_FUCIYCh3Q` |
| AlMaghrib Institute | @almaghribinstitutetv | `UC1B7kOnMRdPuP0rxW6mS7_A` |
| One Message Foundation | @onemessagefoundation | `UCvJyEIx_it2jFYP5M1OzGng` |
| Safina Society | @safinasociety | `UC2cX3SmsdWsrRS8t_5zvzEw` |
| Sabeel Ahmed | @sabeelislam | `UClx9GdAkydj7Lgu9DbFmY6w` |
| Sapience Institute | @sapienceinstitute | `UCeZBhrU8xHcik0ZgtDwjsdA` |
| One Islam Productions | @oneislamproductions | `UCTX8ZbNDi_HBoyjTWRw9fAg` |
| Huda TV | @hudatvchannel | `UCIVSdtXb4pR57BDqKKNqsTg` |
| Muslim Central | @muslimcentral | `UCz4AXmFeSbi-0vPXI102Q5w` |
| Majed Mahmoud | @majedmahmoud | `UCtsN0omQRQ3_XgnLNioqxPQ` |
| TheDeenShowTV | @thedeenshowtv | `UCXHz5brnR9qwqvQvF3VJdgQ` |
| Qalam Institute | @qalaminstitute | `UCVp5Ze24V2iFiUY1y0WtRaA` |
| Miftaah Institute | @miftaahinstitute | `UCYr_FKFkc_pJq_sMKuaktnw` |
| The Muslim Lantern | @themuslimlantern | `UCeCAQhKbU2ETNWxWxB94HgA` |
| Dr Farhat Hashmi Official | @drfarhathashmiofficial | `UCFnsCaT_2VKBg_SoGjhO1nA` |
| Belal Assaad | @belal.assaad | `UC34dxKHa9n9ggxz4m7bztfA` |

## Ghazal / nasheed — 44

Kalarab has no separate channel — its YouTube presence is Holy Tune, already listed.

| Channel | Handle | Channel id |
|---|---|---|
| Holy Tune | @holytunebdofficial | `UCUNkaUjhRArzU4Tm_TPSDlg` |
| Arafah Records | @arafahrecord | `UCdbosF4YCI_CR_ZLmXO9aQQ` |
| Omar Esa (vocals-only) | @OmarEsa | `UC4d8OKjmN_ggVVbo00DM_3Q` |
| Siedd (vocals-only) | @sieddofficial | `UC9Jrzve0dhPoo_LexakSFMw` |
| Ilyas Mao (vocals-only) | @ilyasmao | `UCpigf8GfwA6lU8f9VRbCv8g` |
| Firas (vocals-only) | @firasvocals | `UC8-_GbgsWcUmZCpWGsC3QvA` |
| Safe Adam (vocals-only) | @SafeAdam | `UCOP4D0JslF9wFvc7x4rqh3A` |
| Labbayk (UK, acappella) | @labbayk | `UC7oYY3yXJKw7ZU7ORlNOF2A` |
| Muad (vocals-only) | @muadmusic | `UCJZS2p3jZ_T8TiUMVE6bvVQ` |
| Ahmed Bukhatir | @ahmedbukhatir | `UC-2pqFUafqKCnACEfQasZGg` |
| Baraa Masoud | @baraamasoudofficial | `UCoGJqzgYwpWoc_unl6UJNIg` |
| Mikhaael Mala | @MikhaaelMala | `UCTC4dW158I7bmI-N-Oh2jEA` |
| Ehsaan Tahmid | @ehsaantahmid | `UC0OQYbUkGVfH1Idvvzq73xg` |
| Sintesa | @sintesaofficial | `UCsDrzNjBfjkyiicDskWp_mg` |
| Maher Zain | @maherzain | `UCMBXjVFU51CJ047U1Wp3JKQ` |
| Zain Bhikha | @zainbhikhaofficial | `UCTV8sHvXq5VUiTGfZv9JWjg` |
| Sami Yusuf | @samiyusuf | `UCMBNjDVgy1lc_FyePVg60_Q` |
| Harris J Official | @harrisjofficial | `UCkZCizaUsLNC-T_osXPiElw` |
| Humood AlKhudher | @humoodalkhudher | `UCB9Iev_eM4rQBlaaVr0qVxw` |
| Milad Raza Qadri | @miladrazaqadri | `UCumITGFd8ACCnmbNp9G3GlA` |
| Castillo Nasheeds | @castillonasheedsofficial | `UCIqEQQFrIuK4uLIa3VCNeug` |
| Native Deen | @nativedeen | `UCU0ngFlg5tmFfLYs_dAdMaQ` |
| Mesut Kurtis | @officialmesutkurtis | `UCzVyRGnKoxIXPxnx31ZfiOQ` |
| Raef | @raefmusic | `UCa706fvxuVPvs-4ZgI95_MQ` |
| Owais Raza Qadri | — | `UCyvdo5fpPSnidSsM-c7F9wg` |
| Junaid Jamshed | — | `UCNA2F7_fAFF3ypRqybx25bg` |
| UNIC | @unic2002 | `UC0Fq9qaJ50wxu8XLmN-i-NQ` |
| Inteam Digital | @inteamdigital | `UCOrEH-SzmP_Nmipsk0yfzMg` |
| Iqra Shilpigosthi | @iqrashilpigosthi | `UCdAan1vkVv8kBP53CNtQFTQ` |
| Sayed Ahmad (Kalarab director) | — | `UCS_J2_PgM1491XV6RUj-OlQ` |
| Tawhid Jamil | @tawhidjamil | `UC2gW2ZBhwsv7hZuGIl8kblA` |
| Abu Rayhan (vocals-only) | @aburayhanofficial | `UCSJa7i2vPy1oNuz7PIVx8Rw` |
| Iqbal Mahmud | @iqbalmahmud | `UCSnPKY9GivUoJ8Db8jcSPzw` |
| Mahfuzul Alam | — | `UCALq8gzLNJ0keV5S8UkemJQ` |
| Jannat TV | @jannattvbd | `UCB2v_yFJyk0b5xeJ-Y7sz5w` |
| Rajiya Risha | @rajiyarisha | `UCpt14g-5tVBZnC-Lqhf7tIg` |
| RUH Nasheeds (vocals-only) | @ruhnasheeds | `UCzi8ADHa6hjKY3t1vAwp4zg` |
| Labbayk Studio (Bangladesh) | @labbaykstudio | `UCTKOmWP9SK2tZeS6L5eVcWg` |
| Sur Torongo | @surtorongoshilpigosthibd | `UCSZEM7_r4Qf4zYreAMrskEg` |
| Wafaa Madiha | @madihaofficial | `UCa76Ff6mM0diVuBBzMyE3AQ` |
| MELODIANS | @melodiansglobal | `UCzVHaUNH10ZjAbOVAlR-UnQ` |
| Rupali Kontho | @rupalikonthoofficial | `UCqXSt1wn-B4yiRqP6LP7DnQ` |
| Tune Hut | @tunehutbd | `UCH8-OZqsPn3EE9F0RvWETMw` |
| Heaven Tune | @heaventuneofficial | `UC25qUhs_TrcUsJ5E5UeWtsg` |

## Tilawat — 22

| Channel | Handle | Channel id |
|---|---|---|
| Alafasy | @alafasy | `UCmMcOjsVehVlEOteyrhjI2Q` |
| Islam Sobhi | @islam_sobhi | `UCzU3jMTL1mFb3Ql9g-pRn9A` |
| Saad Al Ghamdi | @sheikhsaadalghamdi | `UCL8fnPgqDPPBbUphwdv99og` |
| Maher Al Muaiqly | @maheralmuaiqly | `UCfimgqx1TcWuLkgcrm2Rkdw` |
| Yasser Al-Dossari | @yasseraldosry | `UCf5vl3vN4zU2NSdcL-3Z5Nw` |
| Abdul Basit Abdus Samad | @abdulbaasitabdussamad | `UCoD5-Dk95rFMtk5iCXqak6w` |
| Muhammad Al-Luhaidan | @Alluhaydan | `UCXjC4G6_ixHS8bs9Elt4rIQ` |
| Ahmed ibn Ali al-Ajamy | — | `UCh6grRJSt69baw6TFGZWD6g` |
| Fares Abbad | @FaresAbbad | `UCAEW5NoDJ_Ex3pqPuSqWqDw` |
| Mohamed Siddiq El-Minshawi | @ElMenshawiOfficial | `UCSdyoKDGT4Y8p6PFeeoCNnw` |
| Raad Muhammad Al Kurdi | @radmohammad | `UCTH6OW7ZlH7LfKJtcWMOGAg` |
| Omar Hisham Al Arabi | @omarhishamalarabi | `UCvv7gvo7ZtNd0FTUGNdKs1w` |
| KSA Qur'an TV (Makkah) | — | `UCos52azQNBgW63_9uDJoPDA` |
| Sunnah TV (Madinah) | — | `UCROKYPep-UuODNwyipe6JMw` |
| Fatih Seferagic | @fatihseferagic | `UCvuxp1MHJWW-_yGwsNKWSJQ` |
| Tareq Mohammed | @TareqMohammadOfficial | `UCxVnLXkwgnnCx_kKuyiLbbw` |
| Qari Abu Rayhan Official | @qariaburayhanofficial | `UCl-OL9HhHHpp_jt_WkeUGUA` |
| Nasser Al Qatami | @alqtami | `UC-AuvzIPDgBmvoDMlLhBuFQ` |
| Sherif Mostafa | @sherifmostafaofficial | `UCBDiJhE-ZivhzLKT5iWLpIg` |
| Abdul Wali Al-Arkani | @walialarkani | `UCYiRbuBnCTBctL1XzA6UrsQ` |
| Rayan Al-Kurdy | — | `UCAPBipTFl6k_O90DEOJLvFw` |
| Shamsul Haque | @shamsul_haque | `UC4XAyY0HcNirlm78BlGLiLA` |

## Islamic cartoons — 8

| Channel | Handle | Channel id |
|---|---|---|
| Omar & Hana | @omarhanaislamicsongsforkids | `UC178EmfQAV3OT-UpuO6WUMg` |
| One4kids — Zaky & Friends | @one4kids-zaky | `UC5vfWTTPnKdFp-8s0KbqJhw` |
| IQRA Cartoon | @iqracartoon | `UCSL4Dn-FQlvHZiSKipQZMqw` |
| IQRA Cartoon Bangla | @iqracartoonbangla | `UCuAyZMb3xHNyfKe4qEDo4kg` |
| Nobider Kahini | @nobiderkahini-209 | `UCeR1Of2EdepL0-b88e8cJaQ` |
| Noor Kids | @NoorKids | `UC6bOkPFbfnDNa5v3Kz8Xcbw` |
| LittleMuslim | @LittleMuslim | `UC4lFkKHO5QEktDNBMhBQ25A` |
| Learn with Zakaria | @learnwithzakaria | `UCtlcIZVBdFPSAtCoNZsTusg` |

## Halal comedy — 5

| Channel | Handle | Channel id |
|---|---|---|
| Smile 2 Jannah | @smiletojannah | `UCeccrNGLzk6d0M_vaomD3LQ` |
| DawoodSAVAGE | @dawoodsavager | `UCs5GRyu3Sz1KVnBEWB43s7g` |
| Zuhaib Syed | — | `UCXtmVI108qG8GyWBswI7H7A` |
| Deccani Diaries | @deccanidiaries | `UC6uOx4sVqGNGrmJBUOh0zBg` |
| Smile2jannah Urdu & Hindi | — | `UCoIRbHvNK8qmjCMdQMbpQCg` |

## Halal drama / natok — 2

| Channel | Handle | Channel id |
|---|---|---|
| Eagle Islamic Natok | @eagleislamicnatok | `UCd5A8poxhdxProarD8AU-bw` |
| The path of islam | — | `UC816Kbi-g_VtbJH0xRTPN2A` |

## Content notes — channels on the list with a caveat

These channels are whitelisted, but the PIN holder should know what else they carry:

- **Waz:** Assim Al-Hakeem, Huda TV, Mufti Tariq Masood Special — fatwa Q&A that
  sometimes covers sensitive fiqh topics. OnePath Network, MercifulServant,
  Muslim Central, One Islam Productions — also documentaries, news/current-affairs
  or end-times content, not waz only. One Message Foundation, The Muslim Lantern —
  confrontational street-dawah/debate style. Sapience Institute — academic
  apologetics, not traditional waz. Safina Society, Sabeel Ahmed, TheDeenShowTV —
  interfaith/talk-show material with non-Muslim guests on camera.
- **Ghazal:** Maher Zain, Zain Bhikha, Sami Yusuf, Harris J, Humood AlKhudher,
  Milad Raza Qadri, Mesut Kurtis, Raef, UNIC, Inteam Digital, Native Deen, Rajiya
  Risha, Tune Hut, Heaven Tune, Rupali Kontho — full instrumental production on
  many tracks (vocals-only versions also exist on several of these channels).
  Junaid Jamshed's channel also carries non-naat uploads (pop song, qawwali).
  Jannat TV is mostly waz with some gojol. Qari Abu Rayhan and Shamsul Haque mix
  tilawat with nasheed and other videos. RUH Nasheeds features a female vocalist
  on some tracks. Wafaa Madiha also carries family vlogs.
- **Cartoons:** Omar & Hana episodes use music, but many "[NO INSTRUMENTS]"
  versions exist on the channel. Learn with Zakaria — some newer songs may have
  music; spot-check. LittleMuslim's handle is unconfirmed (the id is confirmed).
- **Comedy:** DawoodSAVAGE's "Muslim Rap Battle" series uses music/rap beats.
  Deccani Diaries — general Hyderabadi Muslim comedy, not explicitly halal-branded;
  background music may appear in some videos. Smile2jannah Urdu & Hindi is
  apologetics/current-affairs reaction with humour, not comedy skits.
- **Drama:** Eagle Islamic Natok — mixed-gender family dramas, background music is
  standard in the genre. The path of islam — animated moral/cartoon stories.
- **Waz, low-content entries:** Mufti Amir Hamza's official channel is dormant
  (1 video, 5 years ago) — his active waz lives on media channels, not whitelisted.
  Jamia Talimia Madrasa Dhaka is Hafizur Rahman Siddiqui's madrasa channel, the
  closest thing to an official channel for him — he has no personal one.
  Hefazat Media is an organisation media channel, not one speaker's channel.

## Explicitly NOT whitelisted

- **Ummah Network / Tamim Al Adnani — EXCLUDED, decision needed.** The user asked
  for this channel, but research found it is identified by the Jamestown Foundation
  (Feb 2020) as a channel of Tamim al-Adnani, described as spiritual head of
  ABT/Ansar al-Islam, the official al-Qaeda in the Indian Subcontinent (AQIS)
  branch; SecDev KIRON (Oct 2024) calls Ummah Network "AQIS's most popular Bangla
  channel". Channel id on record (do NOT use): `UCdC58Fno7uiux5-fDWb3F4Q`,
  handle @UmmahNetwork. Do not add without the user being told this first.
- **News channels — NOT added yet.** The user is still deciding the scope; no news
  channel is whitelisted in this round.
- **MiniMuslims** (`UCIDYe6rgdROl77DDevNIcPA`) and **Muslim Kids TV**
  (`UCDz4xz5CW-9n4NJ5mrn7RZA`) — music-heavy nursery-rhyme cartoons, excluded.
- **The Daily Reminder** (`UCIuUOdkiW3uUD3ITS5PCK_A`) — some old videos feature
  Anwar Al-Awlaki; excluded.
- **Khaled Siddiq** — UK nasheed rapper ("halal drill"); videos include urban/gritty
  visuals and nightlife flashbacks with alcohol visible; excluded.
- **The Halal Ham** — a clip contains profanity; excluded.
- **ImranSAVAGE** — prank/roast/vlog, not reliably halal; excluded.

## Could not be verified — not whitelisted

Most famous Bangladeshi waz speakers have **no personal official YouTube channel**;
their waz lives only on third-party media/fan channels, which are not whitelisted
as official channels. The names below could not be added for that reason (or the
channel id could not be confirmed). A clearly-labelled "trusted media channels"
tier would be needed to include them — that is a decision for the user.

- **Waz, no official channel found:** Mamunul Haque, Tariq Monowar, Mufti Syed
  Faizul Karim / Charmonai (incl. Syed Rezaul Karim), Delwar Hossain Sayeedi,
  Mufti Harun Izhar, Junaid Al Habib, Habibur Rahman Misbah, Mufti Hasan Jamil
  (waz speaker — not the Kalarab singer), Abdur Razzaq Bin Yousuf, Abdullah Bin
  Abdur Razzak, Khaled Saifullah Ayubi, Nurul Islam Olipuri, Mufti Abdul Malek
  (does not allow videos — audio only), Dr. Lutfur Rahman (deceased), Mufti Arif
  Bin Habib, Sadikur Rahman Al Azhari, Ali Hasan Osama, Jamshed Majumdar,
  Mufti Mizanur Rahman Saeed, Mufti Mustakunnabi Kasemi, Mufti Nazrul Islam
  Kasemi, Mufti Rezaul Karim Abrar (official channel exists, id unconfirmed),
  Mufti Shahidur Rahman Mahmudabadi, Mufti Abdur Rahim Al Madani, Mufti
  Sharifuzzaman Rajibpuri, Mufti Nasir Uddin Ansari, Mufti Junaid Al Habib,
  Allama Junaid Babunagari, Suhaib Webb, Imam Siraj Wahhaj, Kamal El Mekki,
  Dr Israr Ahmed (official channel id unconfirmed), Waleed Basyouni (official
  channel exists, id unconfirmed), Yasmin Mogahed (id unconfirmed),
  Taimiyyah Zubair, Abdul Nasir Jangda (lectures on Qalam Institute, listed),
  Kazi Ibrahim (Kazi Islamic TV exists, id unconfirmed), Charmonai Darpan
  (exists, id unconfirmed), Islami Andolan Bangladesh (no official channel),
  Peace TV Bangla (no YouTube channel).
- **Tilawat, no official channel or id unconfirmed:** Mahmoud Khalil Al-Husary
  (official channel exists, managed by Diwan Videos — id unconfirmed), Hani Ar
  Rifai (official channel exists — id unconfirmed), Idris Abkar, Abdullah Al
  Matrood, Salah Al Budair, Ali Al Hudhaify (both covered in practice by Sunnah
  TV, listed), Hazza Al Balushi, Khalid Al-Jaleel, Ali Jaber, Khalifa Al Tunaiji,
  Mansour Al Salimi, Haitham Al Dukhan, Abdallah Kamel (official channel exists,
  id unconfirmed), Quraan FM (id unconfirmed, inactive since 2020). AL HERA
  ISLAMIC CENTER — id confirmed (`UCWgry3lrl5Cf4d1Vn06l3xA`) but mixed content
  (waz/tafsir/politics), not tilawat-only; excluded.
- **Ghazal, no official channel or id unconfirmed:** Muhammad Al Muqit, Irfan
  Makki, Abdul Rauf Rufi, Muhammad Badruzzaman and Hujaifa Islam (both on Holy
  Tune, listed), Abdullah Al-Sharif (the id found earlier opens as "Mokhtar
  Edits", a fan channel — not official), Nazeel Azami, Raihan, Hijjaz (uploads
  on Inteam Digital, listed), Hamza Robertson, Mishary Al Arada (died 2018),
  Sadman Sakib (on Tune Hut, listed), Safwan, Dawud Wharnsby (handle confirmed,
  id unconfirmed), Deen Squad (id unconfirmed), Alif Nasheed (id unknown).
- **Cartoons, id unconfirmed:** Ali and Sumaya (exists, official), Little Muslim
  Bangla (many copycats), Omar & Hana Malay main channel. Noor Kids TV Bangla —
  mixed with general rhymes/fairy tales. Adam – Islamic Animation — adult prayer
  tutorials, not kids. Huda TV — general Islamic TV, not kids.
- **Comedy / drama, id unconfirmed:** Sheikh Raptor (best remaining comedy
  candidate — clean content, id not found), Fahad Islamic TV, SMF Entertainment
  Bd, Tajalli Productions, Bangla Islamic Video, Sihab Halal Toons, FamilyTVBD,
  Deep Taqwa, NOBIR GULAM SS, Mohammad Nuruddin, Voice of Sahed / Voice of
  Mousum, Tamim&Salma 02, DOEL, koli media, Islamic Epoch, Freshy. Islamic Video
  Bangla (`UCyDuRfPrmB9gYF8Vmu20mnA`, id confirmed) is Islamic history
  documentaries, not comedy/drama — held for a future category.
