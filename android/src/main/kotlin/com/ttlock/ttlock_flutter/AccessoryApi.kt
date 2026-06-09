package com.ttlock.ttlock_flutter

import android.bluetooth.BluetoothManager
import android.content.Context
import com.ttlock.bl.sdk.device.Remote
import com.ttlock.bl.sdk.device.WirelessDoorSensor
import com.ttlock.bl.sdk.device.WirelessKeypad
import com.ttlock.bl.sdk.electricmeter.api.ElectricMeterClient
import com.ttlock.bl.sdk.electricmeter.model.ElectricMeterError
import com.ttlock.bl.sdk.keypad.WirelessKeypadClient
import com.ttlock.bl.sdk.keypad.model.KeypadError
import com.ttlock.bl.sdk.mulfunkeypad.api.MultifunctionalKeypadClient
import com.ttlock.bl.sdk.mulfunkeypad.model.MultifunctionalKeypadError
import com.ttlock.bl.sdk.remote.api.RemoteClient
import com.ttlock.bl.sdk.remote.model.RemoteError
import com.ttlock.bl.sdk.standalonedoorsensor.api.StandaloneDoorSensorClient
import com.ttlock.bl.sdk.standalonedoorsensor.model.StandaloneDoorSensorConfigInfo
import com.ttlock.bl.sdk.standalonedoorsensor.model.StandaloneDoorSensorError
import com.ttlock.bl.sdk.util.FeatureValueUtil
import com.ttlock.bl.sdk.watermeter.api.WaterMeterClient
import com.ttlock.bl.sdk.watermeter.model.WaterMeterError
import com.ttlock.bl.sdk.wirelessdoorsensor.WirelessDoorSensorClient
import com.ttlock.bl.sdk.wirelessdoorsensor.model.DoorSensorError
import io.flutter.plugin.common.BinaryMessenger

class AccessoryApi : TTAccessoryHostApi {
    var context: Context

    constructor(context: Context, messenger: BinaryMessenger) {
        this.context = context
        TTAccessoryHostApi.setUp(messenger, this)
    }

    override fun setAccessoryAddKeypadFingerprintParam(param: TTKeypadCredentialEventParam) {
        KeypadStreamParams.addFingerprint.apply(param)
    }

    override fun setAccessoryAddKeypadCardParam(param: TTKeypadCredentialEventParam) {
        KeypadStreamParams.addCard.apply(param)
    }

    private fun remoteErrorToFlutterError(error: RemoteError): FlutterError {
        return FlutterError(
            code = remoteErrorRevert(error).raw.toString(),
            message = error.description,
            details = error.description
        )
    }

    private fun keypadErrorToFlutterError(error: KeypadError): FlutterError {
        return FlutterError(
            code = keypadErrorRevert(error).raw.toString(),
            message = error.description,
            details = error.description
        )
    }

    private fun doorSensorErrorToFlutterError(error: DoorSensorError): FlutterError {
        return FlutterError(
            code = doorSensorErrorRevert(error).raw.toString(),
            message = error.description,
            details = error.description
        )
    }

    private fun waterMeterErrorToFlutterError(error: WaterMeterError): FlutterError {
        return FlutterError(
            code = waterMeterErrorRevert(error).raw.toString(),
            message = error.description,
            details = error.description
        )
    }

    private fun electricMeterErrorToFlutterError(error: ElectricMeterError): FlutterError {
        return FlutterError(
            code = electricMeterErrorRevert(error).raw.toString(),
            message = error.description,
            details = error.description
        )
    }

    private fun multifunctionalKeypadErrorToFlutterError(error: MultifunctionalKeypadError): FlutterError {
        return FlutterError(
            code = multifunctionalKeypadErrorRevert(error).raw.toString(),
            message = error.description,
            details = error.description
        )
    }

    private fun standaloneDoorSensorErrorToFlutterError(error: StandaloneDoorSensorError): FlutterError {
        return FlutterError(
            code = error.name,
            message = error.errorMsg,
            details = error.errorCode.toString()
        )
    }

    override fun initRemoteKey(
        mac: String,
        lockData: String,
        callback: (Result<TTLockSystemModel>) -> Unit
    ) {
        val bluetoothManager =
            context.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        val bluetoothAdapter = bluetoothManager.adapter
        val device = bluetoothAdapter.getRemoteDevice(mac)
        val remote = Remote(device)
        RemoteClient.getDefault().initialize(remote, lockData, object : com.ttlock.bl.sdk.remote.callback.InitRemoteCallback {
            override fun onInitSuccess(result: com.ttlock.bl.sdk.remote.model.InitRemoteResult) {
                val systemInfo = result.systemInfo
                callback(
                    Result.success(
                        TTLockSystemModel(
                            modelNum = systemInfo.modelNum,
                            hardwareRevision = systemInfo.hardwareRevision,
                            firmwareRevision = systemInfo.firmwareRevision,
                            electricQuantity = result.batteryLevel.toLong()
                        )
                    )
                )
            }

            override fun onFail(error: RemoteError) {
                callback(Result.failure(remoteErrorToFlutterError(error)))
            }
        })
    }

    override fun initRemoteKeypad(
        mac: String,
        lockMac: String,
        callback: (Result<RemoteKeypadInitResult>) -> Unit
    ) {
        val bluetoothManager =
            context.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        val bluetoothAdapter = bluetoothManager.adapter
        val device = bluetoothAdapter.getRemoteDevice(mac)
        val keypad = WirelessKeypad(device)
        WirelessKeypadClient.getDefault().initializeKeypad(
            keypad,
            lockMac,
            object : com.ttlock.bl.sdk.keypad.InitKeypadCallback {
                override fun onInitKeypadSuccess(result: com.ttlock.bl.sdk.keypad.model.InitKeypadResult) {
                    callback(
                        Result.success(
                            RemoteKeypadInitResult(
                                electricQuantity = result.batteryLevel.toLong(),
                                wirelessKeypadFeatureValue = result.featureValue
                            )
                        )
                    )
                }

                override fun onFail(error: KeypadError) {
                    callback(Result.failure(keypadErrorToFlutterError(error)))
                }
            }
        )
    }

    override fun initMultifunctionalKeypad(
        mac: String,
        lockData: String,
        callback: (Result<MultifunctionalKeypadInitResult>) -> Unit
    ) {
        MultifunctionalKeypadClient.getDefault().initializeMultifunctionalKeypad(
            mac,
            lockData,
            object : com.ttlock.bl.sdk.mulfunkeypad.callback.InitKeypadCallback {
                override fun onInitSuccess(result: com.ttlock.bl.sdk.mulfunkeypad.model.InitMultifunctionalKeypadResult) {
                    callback(
                        Result.success(
                            MultifunctionalKeypadInitResult(
                                electricQuantity = result.batteryLevel.toLong(),
                                wirelessKeypadFeatureValue = result.keypadFeatureValue,
                                slotNumber = result.slotNumber.toLong(),
                                slotLimit = result.slotLimit.toLong(),
                                modelNum = result.firmwareInfo.modelNum,
                                hardwareRevision = result.firmwareInfo.hardwareRevision,
                                firmwareRevision = result.firmwareInfo.firmwareRevision
                            )
                        )
                    )
                }

                override fun onLockFail(lockError: com.ttlock.bl.sdk.entity.LockError) {
                    callback(Result.failure(lockErrorToFlutterError(lockError)))
                }

                override fun onKeypadFail(keypadError: MultifunctionalKeypadError) {
                    callback(Result.failure(multifunctionalKeypadErrorToFlutterError(keypadError)))
                }
            }
        )
    }

    override fun getStoredLocks(mac: String, callback: (Result<List<String>>) -> Unit) {
        callback(Result.failure(FlutterError("NOT_IMPLEMENTED", "getStoredLocks is not implemented", null)))
    }

    override fun deleteStoredLock(
        mac: String,
        slotNumber: Long,
        callback: (Result<Unit>) -> Unit
    ) {
        MultifunctionalKeypadClient.getDefault().deleteLockAtSpecifiedSlot(
            mac,
            slotNumber.toInt(),
            object : com.ttlock.bl.sdk.mulfunkeypad.callback.DeleteLockCallback {
                override fun onDeleteLockSuccess() {
                    callback(Result.success(Unit))
                }

                override fun onKeypadFail(keypadError: MultifunctionalKeypadError) {
                    callback(Result.failure(multifunctionalKeypadErrorToFlutterError(keypadError)))
                }
            }
        )
    }

    override fun initDoorSensor(
        mac: String,
        lockData: String,
        callback: (Result<TTLockSystemModel>) -> Unit
    ) {
        val bluetoothManager =
            context.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        val bluetoothAdapter = bluetoothManager.adapter
        val device = bluetoothAdapter.getRemoteDevice(mac)
        val doorSensor = WirelessDoorSensor(device)
        WirelessDoorSensorClient.getDefault().initialize(
            doorSensor,
            lockData,
            object : com.ttlock.bl.sdk.wirelessdoorsensor.callback.InitDoorSensorCallback {
                override fun onInitSuccess(result: com.ttlock.bl.sdk.wirelessdoorsensor.model.InitDoorSensorResult) {
                    callback(
                        Result.success(
                            TTLockSystemModel(
                                modelNum = result.firmwareInfo.modelNum,
                                hardwareRevision = result.firmwareInfo.hardwareRevision,
                                firmwareRevision = result.firmwareInfo.firmwareRevision,
                                electricQuantity = result.batteryLevel.toLong()
                            )
                        )
                    )
                }

                override fun onFail(error: DoorSensorError) {
                    callback(Result.failure(doorSensorErrorToFlutterError(error)))
                }
            }
        )
    }

    override fun standaloneDoorSensorInit(
        mac: String,
        info: Map<String, Any?>,
        callback: (Result<TTStandaloneDoorSensorInfo>) -> Unit
    ) {
        val config = StandaloneDoorSensorConfigInfo()
        config.mac = mac
        config.wifiName = info["SSID"] as? String ?: ""
        config.wifiPassword = info["wifiPwd"] as? String ?: ""
        config.serverAddress = info["serverAddress"] as? String ?: ""
        config.portNumber = (info["portNumber"] as? Number)?.toInt() ?: 0

        StandaloneDoorSensorClient.getDefault().init(
            config,
            object : com.ttlock.bl.sdk.standalonedoorsensor.callback.InitCallback {
                override fun onInitSuccess(result: com.ttlock.bl.sdk.standalonedoorsensor.model.InitModel) {
                    val deviceInfo = result.deviceInfo
                    callback(
                        Result.success(
                            TTStandaloneDoorSensorInfo(
                                doorSensorData = result.doorSensorData,
                                electricQuantity = deviceInfo.electricQuantity.toLong(),
                                featureValue = deviceInfo.featureValue,
                                wifiMac = deviceInfo.wifiMac,
                                modelNum = deviceInfo.modelNum,
                                hardwareRevision = deviceInfo.hardwareRevision,
                                firmwareRevision = deviceInfo.firmwareRevision
                            )
                        )
                    )
                }

                override fun onFail(error: StandaloneDoorSensorError) {
                    callback(Result.failure(standaloneDoorSensorErrorToFlutterError(error)))
                }
            }
        )
    }

    override fun standaloneDoorSensorReadFeatureValue(
        mac: String,
        callback: (Result<String>) -> Unit
    ) {
        StandaloneDoorSensorClient.getDefault().getDeviceInfo(
            mac,
            object : com.ttlock.bl.sdk.standalonedoorsensor.callback.GetDeviceInfoCallback {
                override fun onGetDeviceInfoSuccess(deviceInfo: com.ttlock.bl.sdk.standalonedoorsensor.model.DeviceInfo) {
                    callback(Result.success(deviceInfo.featureValue ?: ""))
                }

                override fun onFail(error: StandaloneDoorSensorError) {
                    callback(Result.failure(standaloneDoorSensorErrorToFlutterError(error)))
                }
            }
        )
    }

    override fun standaloneDoorSensorIsSupportFunction(
        featureValue: String,
        lockFunction: Long
    ): Boolean {
        return FeatureValueUtil.isSupportFeatureValue(featureValue, lockFunction.toInt())
    }

    override fun waterMeterConfigServer(
        url: String,
        clientId: String,
        accessToken: String
    ) {
        WaterMeterClient.getDefault().setClientParam(url, clientId, accessToken)
    }

    override fun waterMeterConnect(
        mac: String,
        callback: (Result<Unit>) -> Unit
    ) {
        WaterMeterClient.getDefault().connect(mac, object : com.ttlock.bl.sdk.watermeter.callback.ConnectCallback {
            override fun onConnectSuccess(waterMeter: com.ttlock.bl.sdk.watermeter.model.WaterMeter) {
                callback(Result.success(Unit))
            }

            override fun onFail(error: WaterMeterError) {
                callback(Result.failure(waterMeterErrorToFlutterError(error)))
            }
        })
    }

    override fun waterMeterDisconnect(mac: String) {
        WaterMeterClient.getDefault().disconnect()
    }

    override fun waterMeterInit(
        params: TTWaterMeterInitParam,
        callback: (Result<TTWaterMeterInitResult>) -> Unit
    ) {
        val map = HashMap<String, String>()
        map["mac"] = params.mac
        map["number"] = params.name
        map["payMode"] = (if (params.payMode == TTMeterPayMode.POSTPAID) 0 else 1).toString()
        map["price"] = params.price.toString()
        WaterMeterClient.getDefault().add(map, object : com.ttlock.bl.sdk.watermeter.callback.AddCallback {
            override fun onAddSuccess(info: com.ttlock.bl.sdk.watermeter.model.WaterMeterInfo) {
                callback(Result.success(TTWaterMeterInitResult(waterMeterId = info.waterMeterId.toLong(), featureValue = info.featureValue)))
            }

            override fun onFail(error: WaterMeterError) {
                callback(Result.failure(waterMeterErrorToFlutterError(error)))
            }
        })
    }

    override fun waterMeterDelete(
        mac: String,
        callback: (Result<Unit>) -> Unit
    ) {
        WaterMeterClient.getDefault().delete(mac, object : com.ttlock.bl.sdk.watermeter.callback.DeleteCallback {
            override fun onDeleteSuccess() {
                callback(Result.success(Unit))
            }

            override fun onFail(error: WaterMeterError) {
                callback(Result.failure(waterMeterErrorToFlutterError(error)))
            }
        })
    }

    override fun waterMeterSetPowerOnOff(
        mac: String,
        isOn: Boolean,
        callback: (Result<Unit>) -> Unit
    ) {
        WaterMeterClient.getDefault().setWaterOnOff(mac, isOn, object : com.ttlock.bl.sdk.watermeter.callback.SetWaterOnOffCallback {
            override fun onSetSuccess() {
                callback(Result.success(Unit))
            }

            override fun onFail(error: WaterMeterError) {
                callback(Result.failure(waterMeterErrorToFlutterError(error)))
            }
        })
    }

    override fun waterMeterSetRemainderM3(
        mac: String,
        remainderM3: Double,
        callback: (Result<Unit>) -> Unit
    ) {
        WaterMeterClient.getDefault().setRemainingWater(mac, remainderM3.toString(), object : com.ttlock.bl.sdk.watermeter.callback.SetRemainingWaterCallback {
            override fun onSetSuccess() {
                callback(Result.success(Unit))
            }

            override fun onFail(error: WaterMeterError) {
                callback(Result.failure(waterMeterErrorToFlutterError(error)))
            }
        })
    }

    override fun waterMeterClearRemainderM3(
        mac: String,
        callback: (Result<Unit>) -> Unit
    ) {
        WaterMeterClient.getDefault().clearRemainingWater(mac, object : com.ttlock.bl.sdk.watermeter.callback.ClearRemainingWaterCallback {
            override fun onClearSuccess() {
                callback(Result.success(Unit))
            }

            override fun onFail(error: WaterMeterError) {
                callback(Result.failure(waterMeterErrorToFlutterError(error)))
            }
        })
    }

    override fun waterMeterReadData(
        mac: String,
        callback: (Result<Unit>) -> Unit
    ) {
        WaterMeterClient.getDefault().readData(mac, object : com.ttlock.bl.sdk.watermeter.callback.ReadDataCallback {
            override fun onReadSuccess() {
                callback(Result.success(Unit))
            }

            override fun onFail(error: WaterMeterError) {
                callback(Result.failure(waterMeterErrorToFlutterError(error)))
            }
        })
    }

    override fun waterMeterSetPayMode(
        mac: String,
        payMode: TTMeterPayMode,
        price: Double,
        callback: (Result<Unit>) -> Unit
    ) {
        WaterMeterClient.getDefault().setWorkMode(mac, payModeConvert(payMode), price, object : com.ttlock.bl.sdk.watermeter.callback.SetWorkModeCallback {
            override fun onSetWorkModeSuccess() {
                callback(Result.success(Unit))
            }

            override fun onFail(error: WaterMeterError) {
                callback(Result.failure(waterMeterErrorToFlutterError(error)))
            }
        })
    }

    override fun waterMeterCharge(
        mac: String,
        amount: Double,
        m3: Double,
        callback: (Result<Unit>) -> Unit
    ) {
        WaterMeterClient.getDefault().recharge(mac, amount, m3, object : com.ttlock.bl.sdk.watermeter.callback.RechargeCallback {
            override fun onRechargeSuccess() {
                callback(Result.success(Unit))
            }

            override fun onFail(error: WaterMeterError) {
                callback(Result.failure(waterMeterErrorToFlutterError(error)))
            }
        })
    }

    override fun waterMeterSetTotalUsage(
        mac: String,
        totalM3: Double,
        callback: (Result<Unit>) -> Unit
    ) {
        WaterMeterClient.getDefault().setTotalUsage(mac, totalM3, object : com.ttlock.bl.sdk.watermeter.callback.SetTotalUsageCallback {
            override fun onSetSuccess() {
                callback(Result.success(Unit))
            }

            override fun onFail(error: WaterMeterError) {
                callback(Result.failure(waterMeterErrorToFlutterError(error)))
            }
        })
    }

    override fun waterMeterGetFeatureValue(
        mac: String,
        callback: (Result<String>) -> Unit
    ) {
        WaterMeterClient.getDefault().getFeatureValue(mac, object : com.ttlock.bl.sdk.watermeter.callback.GetFeatureValueCallback {
            override fun onGetFeatureValueSuccess() {
                callback(Result.success(""))
            }

            override fun onFail(error: WaterMeterError) {
                callback(Result.failure(waterMeterErrorToFlutterError(error)))
            }
        })
    }

    override fun waterMeterGetDeviceInfo(
        mac: String,
        callback: (Result<WaterMeterDeviceInfo>) -> Unit
    ) {
        WaterMeterClient.getDefault().getDeviceInfo(mac, object : com.ttlock.bl.sdk.watermeter.callback.GetDeviceInfoCallback {
            override fun onGetSuccess(deviceInfo: com.ttlock.bl.sdk.meter.model.DeviceInfo) {
                @Suppress("UNCHECKED_CAST")
                callback(Result.success(WaterMeterDeviceInfo(
                    catOneCardNumber = deviceInfo.catOneCardNumber,
                    catOneImsi = deviceInfo.catOneImsi,
                    catOneNodeId = deviceInfo.catOneNodeId,
                    catOneOperator = deviceInfo.catOneOperator,
                    catOneRssi = deviceInfo.catOneRssi.toLong()
                )))
            }

            override fun onFail(error: WaterMeterError) {
                callback(Result.failure(waterMeterErrorToFlutterError(error)))
            }
        })
    }

    override fun waterMeterIsSupportFunction(
        featureValue: String,
        lockFunction: TTWaterMeterFeature
    ): Boolean {
        return FeatureValueUtil.isSupportFeatureValue(featureValue, waterMeterFeatureConvert(lockFunction))
    }

    override fun waterMeterConfigApn(
        mac: String,
        apn: String,
        callback: (Result<Unit>) -> Unit
    ) {
        WaterMeterClient.getDefault().configApn(mac, apn, object : com.ttlock.bl.sdk.watermeter.callback.ConfigApnCallback {
            override fun onConfigSuccess() {
                callback(Result.success(Unit))
            }

            override fun onFail(p0: WaterMeterError) {
                callback(Result.failure(waterMeterErrorToFlutterError(p0)))
            }
        })
    }

    override fun waterMeterConfigMeterServer(
        mac: String,
        ip: String,
        port: String,
        callback: (Result<Unit>) -> Unit
    ) {
        WaterMeterClient.getDefault().configServer(mac, ip, port.toInt(), object : com.ttlock.bl.sdk.watermeter.callback.ConfigServerCallback {
            override fun onConfigSuccess() {
                callback(Result.success(Unit))
            }

            override fun onFail(error: WaterMeterError) {
                callback(Result.failure(waterMeterErrorToFlutterError(error)))
            }
        })
    }

    override fun waterMeterReset(
        mac: String,
        callback: (Result<Unit>) -> Unit
    ) {
        WaterMeterClient.getDefault().reset(mac, object : com.ttlock.bl.sdk.watermeter.callback.ResetCallback {
            override fun onResetSuccess() {
                callback(Result.success(Unit))
            }

            override fun onFail(error: WaterMeterError) {
                callback(Result.failure(waterMeterErrorToFlutterError(error)))
            }
        })
    }

    override fun electricMeterConfigServer(
        url: String,
        clientId: String,
        accessToken: String
    ) {
        ElectricMeterClient.getDefault().setClientParam(url, clientId, accessToken)
    }

    override fun electricMeterConnect(
        mac: String,
        callback: (Result<Unit>) -> Unit
    ) {
        ElectricMeterClient.getDefault().connect(mac, object : com.ttlock.bl.sdk.electricmeter.callback.ConnectCallback {
            override fun onConnectSuccess(electricMeter: com.ttlock.bl.sdk.electricmeter.model.ElectricMeter) {
                callback(Result.success(Unit))
            }

            override fun onFail(error: ElectricMeterError) {
                callback(Result.failure(electricMeterErrorToFlutterError(error)))
            }
        })
    }

    override fun electricMeterDisconnect(mac: String) {
        ElectricMeterClient.getDefault().disconnect()
    }

    override fun electricMeterInit(
        params: TTElectricMeterInitParam,
        callback: (Result<TTElectricMeterInitResult>) -> Unit
    ) {
        val map = HashMap<String, String>()
        map["mac"] = params.mac
        map["number"] = params.name
        map["payMode"] = (if (params.payMode == TTMeterPayMode.POSTPAID) 0 else 1).toString()
        map["price"] = params.price.toString()
        ElectricMeterClient.getDefault().add(map, object : com.ttlock.bl.sdk.electricmeter.callback.AddCallback {
            override fun onAddSuccess(info: com.ttlock.bl.sdk.electricmeter.model.ElectricMeterInfo) {
                callback(Result.success(TTElectricMeterInitResult(electricMeterId = info.electricMeterId.toLong(), featureValue = info.featureValue)))
            }

            override fun onFail(error: ElectricMeterError) {
                callback(Result.failure(electricMeterErrorToFlutterError(error)))
            }
        })
    }

    override fun electricMeterDelete(
        mac: String,
        callback: (Result<Unit>) -> Unit
    ) {
        ElectricMeterClient.getDefault().delete(mac, object : com.ttlock.bl.sdk.electricmeter.callback.DeleteCallback {
            override fun onDeleteSuccess() {
                callback(Result.success(Unit))
            }

            override fun onFail(error: ElectricMeterError) {
                callback(Result.failure(electricMeterErrorToFlutterError(error)))
            }
        })
    }

    override fun electricMeterSetPowerOnOff(
        mac: String,
        isOn: Boolean,
        callback: (Result<Unit>) -> Unit
    ) {
        ElectricMeterClient.getDefault().setPowerOnOff(mac, isOn, object : com.ttlock.bl.sdk.electricmeter.callback.SetPowerOnOffCallback {
            override fun onSetPowerOnOffSuccess() {
                callback(Result.success(Unit))
            }

            override fun onFail(error: ElectricMeterError) {
                callback(Result.failure(electricMeterErrorToFlutterError(error)))
            }
        })
    }

    override fun electricMeterSetRemainderKwh(
        mac: String,
        remainderKwh: Double,
        callback: (Result<Unit>) -> Unit
    ) {
        ElectricMeterClient.getDefault().setRemainingElectricity(mac, remainderKwh.toString(), object : com.ttlock.bl.sdk.electricmeter.callback.SetRemainingElectricityCallback {
            override fun onSetSuccess() {
                callback(Result.success(Unit))
            }

            override fun onFail(error: ElectricMeterError) {
                callback(Result.failure(electricMeterErrorToFlutterError(error)))
            }
        })
    }

    override fun electricMeterClearRemainderKwh(
        mac: String,
        callback: (Result<Unit>) -> Unit
    ) {
        ElectricMeterClient.getDefault().clearRemainingElectricity(mac, object : com.ttlock.bl.sdk.electricmeter.callback.ClearRemainingElectricityCallback {
            override fun onClearSuccess() {
                callback(Result.success(Unit))
            }

            override fun onFail(error: ElectricMeterError) {
                callback(Result.failure(electricMeterErrorToFlutterError(error)))
            }
        })
    }

    override fun electricMeterReadData(
        mac: String,
        callback: (Result<Unit>) -> Unit
    ) {
        ElectricMeterClient.getDefault().readData(mac, object : com.ttlock.bl.sdk.electricmeter.callback.ReadDataCallback {
            override fun onReadSuccess() {
                callback(Result.success(Unit))
            }

            override fun onFail(error: ElectricMeterError) {
                callback(Result.failure(electricMeterErrorToFlutterError(error)))
            }
        })
    }

    override fun electricMeterSetPayMode(
        mac: String,
        payMode: TTMeterPayMode,
        price: Double,
        callback: (Result<Unit>) -> Unit
    ) {
        ElectricMeterClient.getDefault().setWorkMode(mac, payModeConvert(payMode), price, object : com.ttlock.bl.sdk.electricmeter.callback.SetWorkModeCallback {
            override fun onSetWorkModeSuccess() {
                callback(Result.success(Unit))
            }

            override fun onFail(error: ElectricMeterError) {
                callback(Result.failure(electricMeterErrorToFlutterError(error)))
            }
        })
    }

    override fun electricMeterCharge(
        mac: String,
        amount: Double,
        kwh: Double,
        callback: (Result<Unit>) -> Unit
    ) {
        ElectricMeterClient.getDefault().recharge(mac, amount, kwh, object : com.ttlock.bl.sdk.electricmeter.callback.ChargeCallback {
            override fun onChargeSuccess() {
                callback(Result.success(Unit))
            }

            override fun onFail(error: ElectricMeterError) {
                callback(Result.failure(electricMeterErrorToFlutterError(error)))
            }
        })
    }

    override fun electricMeterSetMaxPower(
        mac: String,
        maxPower: Double,
        callback: (Result<Unit>) -> Unit
    ) {
        ElectricMeterClient.getDefault().setMaxPower(mac, maxPower.toInt(), object : com.ttlock.bl.sdk.electricmeter.callback.SetMaxPowerCallback {
            override fun onSetMaxPowerSuccess() {
                callback(Result.success(Unit))
            }

            override fun onFail(error: ElectricMeterError) {
                callback(Result.failure(electricMeterErrorToFlutterError(error)))
            }
        })
    }

    override fun electricMeterGetFeatureValue(
        mac: String,
        callback: (Result<String>) -> Unit
    ) {
        ElectricMeterClient.getDefault().getFeatureValue(mac, object : com.ttlock.bl.sdk.electricmeter.callback.GetFeatureValueCallback {
            override fun onGetFeatureValueSuccess() {
                callback(Result.success(""))
            }

            override fun onFail(error: ElectricMeterError) {
                callback(Result.failure(electricMeterErrorToFlutterError(error)))
            }
        })
    }

    override fun electricMeterIsSupportFunction(
        featureValue: String,
        lockFunction: TTElectricMeterFeature
    ): Boolean {
        return FeatureValueUtil.isSupportFeatureValue(featureValue, electricMeterFeatureConvert(lockFunction))
    }

    override fun electricMeterGetDeviceInfo(
        mac: String,
        callback: (Result<ElectricMeterDeviceInfo>) -> Unit
    ) {
        ElectricMeterClient.getDefault().getDeviceInfo(mac, object : com.ttlock.bl.sdk.electricmeter.callback.GetDeviceInfoCallback {
            override fun onGetSuccess(deviceInfo: com.ttlock.bl.sdk.meter.model.DeviceInfo) {
                callback(Result.success(ElectricMeterDeviceInfo(
                    catOneCardNumber = deviceInfo.catOneCardNumber,
                    catOneImsi = deviceInfo.catOneImsi,
                    catOneNodeId = deviceInfo.catOneNodeId,
                    catOneOperator = deviceInfo.catOneOperator,
                    catOneRssi = deviceInfo.catOneRssi.toLong()
                )))
            }

            override fun onFail(error: ElectricMeterError) {
                callback(Result.failure(electricMeterErrorToFlutterError(error)))
            }
        })
    }

    override fun electricMeterConfigApn(
        mac: String,
        apn: String,
        callback: (Result<Unit>) -> Unit
    ) {
        ElectricMeterClient.getDefault().configApn(mac, apn, object : com.ttlock.bl.sdk.electricmeter.callback.ConfigApnCallback {
            override fun onConfigSuccess() {
                callback(Result.success(Unit))
            }

            override fun onFail(error: ElectricMeterError) {
                callback(Result.failure(electricMeterErrorToFlutterError(error)))
            }
        })
    }

    override fun electricMeterConfigMeterServer(
        mac: String,
        ip: String,
        port: String,
        callback: (Result<Unit>) -> Unit
    ) {
        ElectricMeterClient.getDefault().configServer(mac, ip, port.toInt(), object : com.ttlock.bl.sdk.electricmeter.callback.ConfigServerCallback {
            override fun onConfigSuccess() {
                callback(Result.success(Unit))
            }

            override fun onFail(error: ElectricMeterError) {
                callback(Result.failure(electricMeterErrorToFlutterError(error)))
            }
        })
    }

    override fun electricMeterReset(
        mac: String,
        callback: (Result<Unit>) -> Unit
    ) {
        ElectricMeterClient.getDefault().reset(mac, object : com.ttlock.bl.sdk.electricmeter.callback.ResetCallback {
            override fun onResetSuccess() {
                callback(Result.success(Unit))
            }

            override fun onFail(error: ElectricMeterError) {
                callback(Result.failure(electricMeterErrorToFlutterError(error)))
            }
        })
    }

}
