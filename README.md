# iBeacon scanner android #

[![Build Status](https://travis-ci.org/inthepocket/ibeacon-scanner-android.svg?branch=master)](https://travis-ci.org/inthepocket/ibeacon-scanner-android)
[![MIT License][license-image]][license-url]

Android library to scan for iBeacons.

## Download the library ##

You can download the library via Gradle from the jCenter repository:

```gradle
repositories {
    jcenter()
}
```

By adding the dependency in your module level build.gradle:

```gradle
dependencies {
    compile 'mobi.inthepocket.android:ibeaconscanner:2.0.0'
}
```

## Setup ##

If you want to scan for beacons in an activity or service, first initialize the library in your application class:
You need to set a target service to receive beacon notifications. This API has changed since the introduction of Android 8, we can only start JobIntentServices in the background. All beacon intents will be handled in this JobIntentService.

```java
public class MyApplication extends Application
{
    public void onCreate()
    {
        super.onCreate();

        // initialize
        IBeaconScanner.initialize(IBeaconScanner.newInitializer(this)
            .setTargetService(BeaconActivityService.class)
            .build());
    }
}
```

## Get notified of iBeacon enters and exits ##

You need the JobIntentService (described above) to receive beacon enters and exits.


```java
public class BeaconActivityService extends JobIntentService
{
    protected void onHandleWork(@NonNull final Intent intent)
    {
        // This is the beacon object containing UUID, major and minor info
        final Beacon beacon = intent.getParcelableExtra(BluetoothScanBroadcastReceiver.IBEACON_SCAN_BEACON_DETECTION);
        
        // This flag will be true if it is an enter event that triggered this service
        final boolean enteredBeacon = intent.getBooleanExtra(BluetoothScanBroadcastReceiver.IBEACON_SCAN_BEACON_ENTERED, false);
        
        // This flag will be true if it is an exit event that triggered this service
        final boolean exitedBeacon = intent.getBooleanExtra(BluetoothScanBroadcastReceiver.IBEACON_SCAN_BEACON_EXITED, false);
        
        // Here you can do something with the beacon trigger
    }
}
```

### Create a Beacon object ###


```
final Beacon beacon = Beacon.newBuilder()
    .setUUID("84be19d4-797d-11e5-8bcf-feff819cdc9f")
    .setMajor(1)
    .setMinor(2)
    .build();
```

### Pass beacons you want to monitor ###

Pass one or more beacons to the library to start getting enter or exit notifies:

```
IBeaconScanner.getInstance().startMonitoring(beacon);
```

## License

IBEACON-SCANNER-ANDROID is freely distributable under the terms of the [MIT license](https://github.com/inthepocket/ibeacon-scanner-android/blob/master/LICENSE.md).

[license-image]: http://img.shields.io/badge/license-MIT-blue.svg?style=flat
[license-url]: https://github.com/inthepocket/ibeacon-scanner-android/blob/master/LICENSE.md
