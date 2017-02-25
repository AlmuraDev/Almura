/**
 * This file is part of Almura, All Rights Reserved.
 *
 * Copyright (c) 2014 - 2017 AlmuraDev <http://github.com/AlmuraDev/>
 */
package com.almuradev.almura.lang;

/**
 * All available language variants found from: http://minecraft.gamepedia.com/Language
 */
public enum Languages {
    /**
     * Afrikaans
     */
    AFRIKAANS("af_ZA"),
    /**
     * Arabic
     */
    ARABIC("ar_SA"),
    /**
     * Armenian
     */
    ARMENIAN("hy_AM"),
    /**
     * Basque
     */
    BASQUE("eu_ES"),
    /**
     * Bulgarian
     */
    BULGARIAN("bg_BG"),
    /**
     * Catalan
     */
    CATALAN("ca_ES"),
    /**
     * Chinese (Simplified)
     */
    CHINESE_SIMPLIFIED("zh_CH"),
    /**
     * Chinese (Traditional)
     */
    CHINESE_TRADITIONAL("zh_TW"),
    /**
     * Cornwall
     */
    CORNWALL("kw_GB"),
    /**
     * Czech
     */
    CZECH("cs_CZ"),
    /**
     * Danish
     */
    DANISH("da_DK"),
    /**
     * Dutch
     */
    DUTCH("nl_NL"),
    /**
     * American English
     */
    ENGLISH_AMERICAN("en_US"),
    /**
     * Australian English
     */
    ENGLISH_AUSTRALIAN("en_AU"),
    /**
     * British English
     */
    ENGLISH_BRITISH("en_GB"),
    /**
     * Canadian English
     */
    ENGLISH_CANADIAN("en_CA"),
    /**
     * Pirate English
     */
    ENGLISH_PIRATE("en_PT"),
    /**
     * Esperanto
     */
    ESPERANTO("eo_UY"),
    /**
     * Estonian
     */
    ESTONIAN("et_EE"),
    /**
     * Finnish
     */
    FINNISH("fi_FI"),
    /**
     * French
     */
    FRENCH("fr_FR"),
    /**
     * Canadian French
     */
    FRENCH_CANADIAN("fr_CA"),
    /**
     * Galician
     */
    GALICIAN("gl_ES"),
    /**
     * Georgian
     */
    GEORGIAN("ka_GE"),
    /**
     * German
     */
    GERMAN("de_DE"),
    /**
     * Greek
     */
    GREEK("el_GR"),
    /**
     * Hebrew
     */
    HEBREW("hr_HR"),
    /**
     * Hindi
     */
    HINDI("hi_IN"),
    /**
     * Hungarian
     */
    HUNGARIAN("hu_HU"),
    /**
     * Icelandic
     */
    ICELANDIC("is_IS"),
    /**
     * Indonesian
     */
    INDONESIAN("id_ID"),
    /**
     * Irish
     */
    IRISH("ga_IE"),
    /**
     * Italian
     */
    ITALIAN("it_IT"),
    /**
     * Japanese
     */
    JAPANESE("ja_JP"),
    /**
     * Klingon
     */
    KLINGON("tlh_AA"),
    /**
     * Korean
     */
    KOREAN("ko_KR"),
    /**
     * Latin
     */
    LATIN("la_VA"),
    /**
     * Latvian
     */
    LATVIAN("lv_LV"),
    /**
     * Lithuanian
     */
    LITHUANIAN("lt_LT"),
    /**
     * Luxembourgish
     */
    LUXEMBOURGISH("lb_LU"),
    /**
     * Malay
     */
    MALAY("ls_MY"),
    /**
     * Maltese
     */
    MALTESE("mt_MT"),
    /**
     * Norwegian (Norsk)
     */
    NORWEGIAN_NORSK("no_NO"),
    /**
     * Norwegian (Norsk Nynorsk)
     */
    NORWEGIAN_NORSK_NYNORSK("nb_NO"),
    /**
     * Occitan
     */
    OCCITAN("cc_CT"),
    /**
     * Polish
     */
    POLISH("pl_PL"),
    /**
     * Portuguese
     */
    PORTUGUESE("pt_PT"),
    /**
     * Brazilian Portuguese
     */
    PORTUGUESE_BRAZILIAN("pt_BR"),
    /**
     * Quenya (High Elvish)
     */
    QUENYA("qya_AA"),
    /**
     * Romanian
     */
    ROMANIAN("ro_RO"),
    /**
     * Russian
     */
    RUSSIAN("ru_RU"),
    /**
     * Serbian
     */
    SERBIAN("sr_RS"),
    /**
     * Slovak
     */
    SLOVAK("sk_SK"),
    /**
     * Slovenian
     */
    SLOVENIAN("sl_SL"),
    /**
     * Spanish
     */
    SPANISH("es_ES"),
    /**
     * Argentinian Spanish
     */
    SPANISH_ARGENTINIAN("es_AR"),
    /**
     * Mexican Spanish
     */
    SPANISH_MEXICAN("es_MX"),
    /**
     * Uruguayan Spanish
     */
    SPANISH_URUGUAYAN("es_UY"),
    /**
     * Venezuelan Spanish
     */
    SPANISH_VENEZUELAN("es_VE"),
    /**
     * Swedish
     */
    SWEDISH("sv_SE"),
    /**
     * Thai
     */
    THAI("th_TH"),
    /**
     * Turkish
     */
    TURKISH("tr_TR"),
    /**
     * Ukrainian
     */
    UKRAINIAN("uk_UA"),
    /**
     * Vietnamese
     */
    VIETNAMESE("vi_VN"),
    /**
     * Welsh
     */
    WELSH("cy_GB");
    private final String value;

    Languages(String value) {
        this.value = value;
    }

    /**
     * Returns the language code
     *
     * @return The string value
     */
    public String value() {
        return value;
    }
}
