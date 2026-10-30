package com.ttlock.ttlock_flutter

import android.app.Activity
import android.content.Context
import com.ttlock.bl.sdk.api.TTLockClient
import com.ttlock.bl.sdk.gateway.api.GatewayClient
import com.ttlock.bl.sdk.keypad.WirelessKeypadClient
import com.ttlock.bl.sdk.mulfunkeypad.api.MultifunctionalKeypadClient
import com.ttlock.bl.sdk.remote.api.RemoteClient
import com.ttlock.bl.sdk.util.LogUtil
import com.ttlock.bl.sdk.wirelessdoorsensor.WirelessDoorSensorClient
import io.flutter.embedding.engine.plugins.FlutterPlugin
import io.flutter.embedding.engine.plugins.activity.ActivityAware
import io.flutter.embedding.engine.plugins.activity.ActivityPluginBinding
import io.flutter.plugin.common.BinaryMessenger


/** TtlockPremiseFlutterPlugin */
class TtlockFlutterPlugin: FlutterPlugin, ActivityAware {
  private var applicationContext: Context? = null
  /** Prefer Activity when available — aligns with premise plugin / SDK prepareBTService. */
  private var activity: Activity? = null
  private var messenger: BinaryMessenger? = null
  /// This local reference serves to register the plugin with the Flutter Engine and unregister it
  /// when the Flutter Engine is detached from the Activity
  private var scanLockImpl: ScanLockImpl? = null
  private var scanLockWifiImpl: ScanLockWifiImpl? = null
  private var addLockCardImpl: AddLockCardImpl? = null
  private var addLockFingerprintImpl: AddLockFingerprintImpl? = null
  private var addLockFaceImpl: AddLockFaceImpl? = null
  private var addLockPalmVeinImpl: AddLockPalmVeinImpl? = null
  private var scanGatewayImpl: ScanGatewayImpl? = null
  private var scanGatewayWiFiImpl: ScanGatewayWiFiImpl? = null
  private var scanRemoteKeyImpl: ScanRemoteKeyImpl? = null
  private var scanRemoteKeypadImpl: ScanRemoteKeypadImpl? = null
  private var addKeypadFingerprintImpl: AddKeypadFingerprintImpl? = null
  private var addKeypadCardImpl: AddKeypadCardImpl? = null
  private var scanDoorSensorImpl: ScanDoorSensorImpl? = null
  private var scanStandaloneDoorSensorImpl: ScanStandaloneDoorSensorImpl? = null
  private var scanWaterMeterImpl: ScanWaterMeterImpl? = null
  private var scanElectricMeterImpl: ScanElectricMeterImpl? = null
  private var lockApi: LockApi? = null
  private var gatewayApi: GatewayApi? = null
  private var accessoryApi: AccessoryApi? = null

  private fun activeContext(): Context? = activity ?: applicationContext

  override fun onAttachedToEngine(flutterPluginBinding: FlutterPlugin.FlutterPluginBinding) {
    this.applicationContext = flutterPluginBinding.applicationContext
    this.messenger = flutterPluginBinding.binaryMessenger
    LogUtil.setDBG(true)
    initSdk()
    initModules()
  }

  private fun initSdk() {
    val ctx = activeContext() ?: return
    TTLockClient.getDefault().prepareBTService(ctx)
    GatewayClient.getDefault().prepareBTService(ctx)
    RemoteClient.getDefault().prepareBTService(ctx)
    WirelessKeypadClient.getDefault().prepareBTService(ctx)
    MultifunctionalKeypadClient.getDefault().prepareBTService(ctx)
    WirelessDoorSensorClient.getDefault().prepareBTService(ctx)
  }

  private fun updateModuleContexts(ctx: Context) {
    scanLockImpl?.context = ctx
    scanLockWifiImpl?.context = ctx
    addLockCardImpl?.context = ctx
    addLockFingerprintImpl?.context = ctx
    addLockFaceImpl?.context = ctx
    addLockPalmVeinImpl?.context = ctx
    scanGatewayImpl?.context = ctx
    scanGatewayWiFiImpl?.context = ctx
    scanRemoteKeyImpl?.context = ctx
    scanRemoteKeypadImpl?.context = ctx
    addKeypadFingerprintImpl?.context = ctx
    addKeypadCardImpl?.context = ctx
    scanDoorSensorImpl?.context = ctx
    scanStandaloneDoorSensorImpl?.context = ctx
    scanWaterMeterImpl?.context = ctx
    scanElectricMeterImpl?.context = ctx
    lockApi?.context = ctx
    gatewayApi?.context = ctx
    accessoryApi?.context = ctx
  }
  
  private fun initModules() {
    val ctx = activeContext() ?: return
    val messenger = this.messenger ?: return
    scanLockImpl = ScanLockImpl(ctx, messenger)
    scanLockWifiImpl = ScanLockWifiImpl(ctx, messenger)
    addLockCardImpl = AddLockCardImpl(ctx, messenger)
    addLockFingerprintImpl = AddLockFingerprintImpl(ctx, messenger)
    addLockFaceImpl = AddLockFaceImpl(ctx, messenger)
    addLockPalmVeinImpl = AddLockPalmVeinImpl(ctx, messenger)
    scanGatewayImpl = ScanGatewayImpl(ctx, messenger)
    scanGatewayWiFiImpl = ScanGatewayWiFiImpl(ctx, messenger)
    scanRemoteKeyImpl = ScanRemoteKeyImpl(ctx, messenger)
    scanRemoteKeypadImpl = ScanRemoteKeypadImpl(ctx, messenger)
    addKeypadFingerprintImpl = AddKeypadFingerprintImpl(ctx, messenger)
    addKeypadCardImpl = AddKeypadCardImpl(ctx, messenger)
    scanDoorSensorImpl = ScanDoorSensorImpl(ctx, messenger)
    scanStandaloneDoorSensorImpl = ScanStandaloneDoorSensorImpl(ctx, messenger)
    scanWaterMeterImpl = ScanWaterMeterImpl(ctx, messenger)
    scanElectricMeterImpl = ScanElectricMeterImpl(ctx, messenger)
    lockApi = LockApi(ctx, messenger)
    gatewayApi = GatewayApi(ctx, messenger)
    accessoryApi = AccessoryApi(ctx, messenger)
  }

  override fun onDetachedFromEngine(binding: FlutterPlugin.FlutterPluginBinding) {
    applicationContext = null
    messenger = null
  }

  override fun onAttachedToActivity(binding: ActivityPluginBinding) {
    activity = binding.activity
    updateModuleContexts(binding.activity)
    // premise: prepare with Activity once it is available
    initSdk()
  }

  override fun onDetachedFromActivityForConfigChanges() {
    activity = null
  }

  override fun onReattachedToActivityForConfigChanges(binding: ActivityPluginBinding) {
    activity = binding.activity
    updateModuleContexts(binding.activity)
    initSdk()
  }

  override fun onDetachedFromActivity() {
    activity = null
    applicationContext?.let { updateModuleContexts(it) }
  }

}