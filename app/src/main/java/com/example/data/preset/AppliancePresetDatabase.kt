package com.example.data.preset

import com.example.domain.model.ApplianceProfile
import com.example.domain.model.ApplianceType
import com.example.domain.model.IrCommandType
import com.example.domain.model.IrSignalPattern

object AppliancePresetDatabase {

    fun getAllPresets(): List<ApplianceProfile> {
        return listOf(
            // Fans
            createFanProfile(
                id = "fan_keruilai",
                brand = "Keruilai",
                model = "Universal Air Cooler",
                powerCmd = 0x00FF00FF,
                speedCmd = 0x00FF807F,
                swingCmd = 0x00FF40BF,
                timerCmd = 0x00FF20DF,
                modeCmd = 0x00FFA05F
            ),
            createFanProfile(
                id = "fan_kennede",
                brand = "KENNEDE",
                model = "KN-5996HRB Stand Fan",
                powerCmd = 0x00FE01FE,
                speedCmd = 0x00FE817E,
                swingCmd = 0x00FE41BE,
                timerCmd = 0x00FE21DE,
                modeCmd = 0x00FEA15E
            ),
            createFanProfile(
                id = "fan_xiaomi",
                brand = "Xiaomi / Smartmi",
                model = "Standing Fan 2/3 (DC Inverter)",
                powerCmd = 0x00FF00FF,
                speedCmd = 0x00FF807F,
                swingCmd = 0x00FF40BF,
                timerCmd = 0x00FF20DF,
                modeCmd = 0x00FFA05F
            ),
            createFanProfile(
                id = "fan_hatari",
                brand = "Hatari",
                model = "HT-T16M4 / Remote Series",
                powerCmd = 0x00FE01FE,
                speedCmd = 0x00FE817E,
                swingCmd = 0x00FE41BE,
                timerCmd = 0x00FE21DE,
                modeCmd = 0x00FEA15E
            ),
            createFanProfile(
                id = "fan_midea",
                brand = "Midea",
                model = "FS40 Stand Fan (NEC)",
                powerCmd = 0x01FE02FD,
                speedCmd = 0x01FE827D,
                swingCmd = 0x01FE42BD,
                timerCmd = 0x01FE22DD,
                modeCmd = 0x01FEA25D
            ),
            createFanProfile(
                id = "fan_kdk",
                brand = "KDK / Panasonic",
                model = "Ceiling & Stand Fan",
                powerCmd = 0x02FD04FB,
                speedCmd = 0x02FD847B,
                swingCmd = 0x02FD44BB,
                timerCmd = 0x02FD24DB,
                modeCmd = 0x02FDA45B
            ),

            // Televisions
            createTvProfile(
                id = "tv_xiaomi",
                brand = "Xiaomi",
                model = "Mi TV 4A/4S / Redmi TV",
                powerCmd = 0x00FF12ED,
                volUp = 0x00FF1AE5,
                volDown = 0x00FF1EE1,
                mute = 0x00FF1CE3,
                home = 0x00FF15EA,
                back = 0x00FF11EE,
                up = 0x00FF06F9,
                down = 0x00FF07F8,
                left = 0x00FF08F7,
                right = 0x00FF09F6,
                ok = 0x00FF0AF5
            ),
            createTvProfile(
                id = "tv_samsung",
                brand = "Samsung",
                model = "Crystal UHD / Neo QLED (NEC)",
                powerCmd = 0xE0E040BF.toInt(),
                volUp = 0xE0E0E01F.toInt(),
                volDown = 0xE0E0D02F.toInt(),
                mute = 0xE0E0F00F.toInt(),
                home = 0xE0E058A7.toInt(),
                back = 0xE0E01AE5.toInt(),
                up = 0xE0E006F9.toInt(),
                down = 0xE0E08679.toInt(),
                left = 0xE0E0A659.toInt(),
                right = 0xE0E046B9.toInt(),
                ok = 0xE0E016E9.toInt()
            ),
            createTvProfile(
                id = "tv_lg",
                brand = "LG",
                model = "webOS Smart TV (NEC)",
                powerCmd = 0x20DF10EF.toInt(),
                volUp = 0x20DF40BF.toInt(),
                volDown = 0x20DFC03F.toInt(),
                mute = 0x20DF906F.toInt(),
                home = 0x20DF3EC1.toInt(),
                back = 0x20DF14EB.toInt(),
                up = 0x20DF02FD.toInt(),
                down = 0x20DF827D.toInt(),
                left = 0x20DFE01F.toInt(),
                right = 0x20DF609F.toInt(),
                ok = 0x20DF22DD.toInt()
            ),
            createTvProfile(
                id = "tv_tcl",
                brand = "TCL",
                model = "Android & Google TV (NEC)",
                powerCmd = 0x00FF38C7,
                volUp = 0x00FF708F,
                volDown = 0x00FFF00F,
                mute = 0x00FF50AF,
                home = 0x00FF30CF,
                back = 0x00FF7887,
                up = 0x00FF02FD,
                down = 0x00FF827D,
                left = 0x00FF42BD,
                right = 0x00FFC23D,
                ok = 0x00FF22DD
            ),

            // Air Conditioners
            createAcProfile(
                id = "ac_gree",
                brand = "Gree",
                model = "Fairy / Lomo Inverter",
                power = 0x00FF18E7,
                tempUp = 0x00FF58A7,
                tempDown = 0x00FF9867,
                mode = 0x00FFD827,
                speed = 0x00FF38C7,
                swing = 0x00FFB847
            ),
            createAcProfile(
                id = "ac_daikin",
                brand = "Daikin",
                model = "FTKQ Inverter Series",
                power = 0x11EE18E7,
                tempUp = 0x11EE58A7,
                tempDown = 0x11EE9867,
                mode = 0x11EED827,
                speed = 0x11EE38C7,
                swing = 0x11EEB847
            ),
            createAcProfile(
                id = "ac_midea",
                brand = "Midea",
                model = "Mission / All Easy Pro",
                power = 0x22DD18E7,
                tempUp = 0x22DD58A7,
                tempDown = 0x22DD9867,
                mode = 0x22DDD827,
                speed = 0x22DD38C7,
                swing = 0x22DDB847
            )
        )
    }

    private fun createFanProfile(
        id: String,
        brand: String,
        model: String,
        powerCmd: Int,
        speedCmd: Int,
        swingCmd: Int,
        timerCmd: Int,
        modeCmd: Int
    ): ApplianceProfile {
        val commands = mapOf(
            IrCommandType.POWER to NecProtocolEncoder.encode32BitWord(powerCmd, IrCommandType.POWER),
            IrCommandType.FAN_SPEED to NecProtocolEncoder.encode32BitWord(speedCmd, IrCommandType.FAN_SPEED),
            IrCommandType.SWING to NecProtocolEncoder.encode32BitWord(swingCmd, IrCommandType.SWING),
            IrCommandType.TIMER to NecProtocolEncoder.encode32BitWord(timerCmd, IrCommandType.TIMER),
            IrCommandType.MODE to NecProtocolEncoder.encode32BitWord(modeCmd, IrCommandType.MODE)
        )
        return ApplianceProfile(id, brand, model, ApplianceType.FAN, commands)
    }

    private fun createTvProfile(
        id: String,
        brand: String,
        model: String,
        powerCmd: Int,
        volUp: Int,
        volDown: Int,
        mute: Int,
        home: Int,
        back: Int,
        up: Int,
        down: Int,
        left: Int,
        right: Int,
        ok: Int
    ): ApplianceProfile {
        val commands = mapOf(
            IrCommandType.POWER to NecProtocolEncoder.encode32BitWord(powerCmd, IrCommandType.POWER),
            IrCommandType.VOL_UP to NecProtocolEncoder.encode32BitWord(volUp, IrCommandType.VOL_UP),
            IrCommandType.VOL_DOWN to NecProtocolEncoder.encode32BitWord(volDown, IrCommandType.VOL_DOWN),
            IrCommandType.MUTE to NecProtocolEncoder.encode32BitWord(mute, IrCommandType.MUTE),
            IrCommandType.HOME to NecProtocolEncoder.encode32BitWord(home, IrCommandType.HOME),
            IrCommandType.BACK to NecProtocolEncoder.encode32BitWord(back, IrCommandType.BACK),
            IrCommandType.UP to NecProtocolEncoder.encode32BitWord(up, IrCommandType.UP),
            IrCommandType.DOWN to NecProtocolEncoder.encode32BitWord(down, IrCommandType.DOWN),
            IrCommandType.LEFT to NecProtocolEncoder.encode32BitWord(left, IrCommandType.LEFT),
            IrCommandType.RIGHT to NecProtocolEncoder.encode32BitWord(right, IrCommandType.RIGHT),
            IrCommandType.OK to NecProtocolEncoder.encode32BitWord(ok, IrCommandType.OK)
        )
        return ApplianceProfile(id, brand, model, ApplianceType.TV, commands)
    }

    private fun createAcProfile(
        id: String,
        brand: String,
        model: String,
        power: Int,
        tempUp: Int,
        tempDown: Int,
        mode: Int,
        speed: Int,
        swing: Int
    ): ApplianceProfile {
        val commands = mapOf(
            IrCommandType.POWER to NecProtocolEncoder.encode32BitWord(power, IrCommandType.POWER),
            IrCommandType.TEMP_UP to NecProtocolEncoder.encode32BitWord(tempUp, IrCommandType.TEMP_UP),
            IrCommandType.TEMP_DOWN to NecProtocolEncoder.encode32BitWord(tempDown, IrCommandType.TEMP_DOWN),
            IrCommandType.MODE to NecProtocolEncoder.encode32BitWord(mode, IrCommandType.MODE),
            IrCommandType.FAN_SPEED to NecProtocolEncoder.encode32BitWord(speed, IrCommandType.FAN_SPEED),
            IrCommandType.SWING to NecProtocolEncoder.encode32BitWord(swing, IrCommandType.SWING)
        )
        return ApplianceProfile(id, brand, model, ApplianceType.AIR_CONDITIONER, commands)
    }
}
