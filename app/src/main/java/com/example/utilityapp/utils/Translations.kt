package com.example.utilityapp.utils
//language translation module for Nepali, English, chinese and HIndi
object Translations {
    private val data = mapOf(
        "English" to mapOf(
            "search_hint" to "Search Country, City",
            "next_24_hours" to "Next 24 Hours",
            "smart_assistant" to "Smart Assistant",
            "settings" to "Settings",
            "temp_unit" to "Temperature Unit",
            "main_screen_details" to "Main Screen Details",
            "personalization" to "Personalization",
            "night_mode" to "Night Mode",
            "app_language" to "App Language",
            "feels_like" to "Feels Like",
            "humidity" to "Humidity",
            "wind_speed" to "Wind Speed",
            "weather" to "Weather",
            "clothing" to "Clothing",
            "water" to "Water",
            "travel" to "Travel"
        ),
        "Nepali" to mapOf(
            "search_hint" to "देश, शहर खोज्नुहोस्",
            "next_24_hours" to "अर्को २४ घण्टा",
            "smart_assistant" to "स्मार्ट सहायक",
            "settings" to "सेटिङहरू",
            "temp_unit" to "तापमान एकाइ",
            "main_screen_details" to "मुख्य स्क्रिन विवरणहरू",
            "personalization" to "व्यक्तिगतकरण",
            "night_mode" to "नाइट मोड",
            "app_language" to "अनुप्रयोग भाषा",
            "feels_like" to "जस्तो महसुस हुन्छ",
            "humidity" to "आर्द्रता",
            "wind_speed" to "हावाको गति",
            "weather" to "मौसम",
            "clothing" to "लुगा",
            "water" to "पानी",
            "travel" to "यात्रा"
        ),
        "Chinese" to mapOf(
            "search_hint" to "搜索国家、城市",
            "next_24_hours" to "未来 24 小时",
            "smart_assistant" to "智能助手",
            "settings" to "设置",
            "temp_unit" to "温度单位",
            "main_screen_details" to "主屏幕详情",
            "personalization" to "个性化",
            "night_mode" to "夜间模式",
            "app_language" to "应用语言",
            "feels_like" to "体感温度",
            "humidity" to "湿度",
            "wind_speed" to "风速",
            "weather" to "天气",
            "clothing" to "衣物",
            "water" to "饮水",
            "travel" to "出行"
        ),
        "Hindi" to mapOf(
            "search_hint" to "देश, शहर खोजें",
            "next_24_hours" to "अगले 24 घंटे",
            "smart_assistant" to "स्मार्ट सहायक",
            "settings" to "सेटिङ",
            "temp_unit" to "तापमान इकाई",
            "main_screen_details" to "मुख्य स्क्रीन विवरण",
            "personalization" to "निजीकरण",
            "night_mode" to "नाइट मोड",
            "app_language" to "ऐप की भाषा",
            "feels_like" to "ऐसा लगता है",
            "humidity" to "नमी",
            "wind_speed" to "हवा की गति",
            "weather" to "मौसम",
            "clothing" to "कपड़े",
            "water" to "पानी",
            "travel" to "यात्रा"
        )
    )

    fun getString(key: String, language: String): String {
        return data[language]?.get(key) ?: data["English"]?.get(key) ?: key
    }
}
