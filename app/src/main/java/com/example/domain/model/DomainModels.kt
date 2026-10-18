package com.example.domain.model

enum class ApplianceType(val displayNameMy: String, val displayNameEn: String) {
    FAN("ပန်ကာ (Fan)", "Fan"),
    TV("တီဗီ (Smart TV)", "Television"),
    AIR_CONDITIONER("အဲကွန်း (Air Conditioner)", "Air Conditioner"),
    PROJECTOR("ပရိုဂျက်တာ (Projector)", "Projector"),
    TV_BOX("TV Box / Setup Box", "TV Box"),
    AUDIO("အသံချဲ့စက် (Audio / Soundbar)", "Audio Receiver"),
    CUSTOM("စိတ်ကြိုက် (Custom)", "Custom Remote")
}

enum class IrCommandType(val labelMy: String, val labelEn: String) {
    POWER("ပါဝါ", "Power"),
    MUTE("အသံပိတ်", "Mute"),
    VOL_UP("အသံတိုး", "Vol +"),
    VOL_DOWN("အသံလျှော့", "Vol -"),
    CH_UP("လိုင်းတက်", "CH +"),
    CH_DOWN("လိုင်းဆင်း", "CH -"),
    TEMP_UP("အပူချိန်တိုး", "Temp +"),
    TEMP_DOWN("အပူချိန်လျှော့", "Temp -"),
    FAN_SPEED("လေအား", "Speed"),
    SWING("လည်ပတ်", "Swing"),
    TIMER("အချိန်ကိုက်", "Timer"),
    MODE("စနစ်", "Mode"),
    INPUT("Input", "Source"),
    UP("အပေါ်", "Up"),
    DOWN("အောက်", "Down"),
    LEFT("ဘယ်", "Left"),
    RIGHT("ညာ", "Right"),
    OK("OK", "OK"),
    BACK("နောက်သို့", "Back"),
    HOME("ပင်မ", "Home")
}

data class IrSignalPattern(
    val commandType: IrCommandType,
    val frequencyHz: Int = 38000,
    val patternMicroseconds: IntArray,
    val hexCode: String = "",
    val protocolName: String = "NEC",
    /**
     * True if this code was synthesized by AI (unverified).
     * False if from verified preset database.
     * UI should warn user before transmitting unverified codes.
     */
    val isAiSynthesized: Boolean = false
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as IrSignalPattern

        if (commandType != other.commandType) return false
        if (frequencyHz != other.frequencyHz) return false
        if (!patternMicroseconds.contentEquals(other.patternMicroseconds)) return false
        if (hexCode != other.hexCode) return false
        if (protocolName != other.protocolName) return false
        if (isAiSynthesized != other.isAiSynthesized) return false

        return true
    }

    override fun hashCode(): Int {
        var result = commandType.hashCode()
        result = 31 * result + frequencyHz
        result = 31 * result + patternMicroseconds.contentHashCode()
        result = 31 * result + hexCode.hashCode()
        result = 31 * result + protocolName.hashCode()
        result = 31 * result + isAiSynthesized.hashCode()
        return result
    }
}

data class ApplianceProfile(
    val id: String,
    val brand: String,
    val model: String,
    val type: ApplianceType,
    val commands: Map<IrCommandType, IrSignalPattern> = emptyMap(),
    val isCustom: Boolean = false
)

data class HardwareState(
    val hasIrEmitter: Boolean = false,
    val carrierFrequencies: List<Pair<Int, Int>> = emptyList(),
    val isRedmiOrXiaomi: Boolean = false,
    val isTransmitting: Boolean = false,
    val lastTransmittedCommand: String? = null,
    val lastError: String? = null
)
