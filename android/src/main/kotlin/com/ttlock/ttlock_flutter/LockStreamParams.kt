package com.ttlock.ttlock_flutter

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.ttlock.bl.sdk.entity.CyclicConfig
import com.ttlock.bl.sdk.entity.ValidityInfo

/**
 * 各锁相关 EventChannel 的专用参数槽；由对应 [TTLockHostApi.set*Param] 写入。
 */
object LockStreamParams {
    private val gson = Gson()

    @Volatile
    var scanWifiLockData: String? = null

    val addCard = CredentialSlot()
    val addFingerprint = CredentialSlot()
    val addFace = CredentialSlot()

    class CredentialSlot {
        @Volatile
        var lockData: String? = null

        @Volatile
        var cycleList: List<TTCycleModel>? = null

        @Volatile
        var startDate: Long = 0L

        @Volatile
        var endDate: Long = 0L

        fun apply(param: TTLockCredentialEventParam) {
            lockData = param.lockData
            cycleList = param.cycleList
            startDate = param.startDate
            endDate = param.endDate
        }

        fun requireLockData(operation: String): String {
            val data = lockData
            if (data.isNullOrEmpty()) {
                throw IllegalStateException("请先通过 set${operation}Param 设置参数后再订阅对应 EventChannel")
            }
            return data
        }

        fun buildValidityInfo(): ValidityInfo {
            val validityInfo = ValidityInfo()
            validityInfo.startDate = startDate
            validityInfo.endDate = endDate
            val list = cycleList
            if (list.isNullOrEmpty()) {
                validityInfo.modeType = ValidityInfo.TIMED
                return validityInfo
            }
            validityInfo.modeType = ValidityInfo.CYCLIC
            val type = object : TypeToken<List<CyclicConfig>>() {}
            val cyclicConfigs: List<CyclicConfig> = gson.fromJson(gson.toJson(list), type.type)
            validityInfo.cyclicConfigs = cyclicConfigs
            return validityInfo
        }
    }
}

object GatewayStreamParams {
    @Volatile
    var nearbyWifiGatewayMac: String? = null
}

object KeypadStreamParams {
    val addFingerprint = KeypadCredentialSlot()
    val addCard = KeypadCredentialSlot()

    class KeypadCredentialSlot {
        @Volatile
        var keypadMac: String? = null

        @Volatile
        var lockData: String? = null

        @Volatile
        var isMultifunctional: Boolean = false

        @Volatile
        var cycleList: List<TTCycleModel>? = null

        @Volatile
        var startDate: Long = 0L

        @Volatile
        var endDate: Long = 0L

        fun apply(param: TTKeypadCredentialEventParam) {
            keypadMac = param.keypadMac
            lockData = param.lockData
            isMultifunctional = param.isMultifunctional
            cycleList = param.cycleList
            startDate = param.startDate
            endDate = param.endDate
        }

        fun buildValidityInfo(): ValidityInfo {
            val validityInfo = ValidityInfo()
            validityInfo.startDate = startDate
            validityInfo.endDate = endDate
            val list = cycleList
            if (list.isNullOrEmpty()) {
                validityInfo.modeType = ValidityInfo.TIMED
                return validityInfo
            }
            validityInfo.modeType = ValidityInfo.CYCLIC
            val gson = Gson()
            val type = object : TypeToken<List<CyclicConfig>>() {}
            val cyclicConfigs: List<CyclicConfig> = gson.fromJson(gson.toJson(list), type.type)
            validityInfo.cyclicConfigs = cyclicConfigs
            return validityInfo
        }
    }
}
