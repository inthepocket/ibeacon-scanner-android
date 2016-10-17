# IBEACON SCANNER ANDROID #

Android library to scan for iBeacons.

## Download the library ##

You can download the library via Gradle:

```
#!java
dependencies {
    compile 'mobi.inthepocket.android:ibeaconscanner:1.0.0-SNAPSHOT'
}
```

## Setup ##

It depends if you want to scan for beacons in a service or in your app how to integrate the library:

### Service ###

```
#!java

public class MyService extends Service implements IBeaconScanner.Callback
{
    @Override
    public void onCreate()
    {
        super.OnCreate();

        // initialize
        IBeaconScanner.initialize(IBeaconScanner.newInitializer(this).build());
        IBeaconScanner.getInstance().setCallback(this);
    }
}
```

### Application ###

If you want to scan for beacons in an activity, first initialize the library in your application class:

```
#!java

public class MyApplication extends Application
{
    public void onCreate()
    {
        super.onCreate();

        // initialize
        IBeaconScanner.initialize(IBeaconScanner.newInitializer(this).build());
    }
}
```

## Get notified of iBeacon enters and exits ##

### Set Callback ###

You need to set your Callback in your Activity, Fragment or Service, by implementing this interface:


```
#!java

public interface Callback
{
    void didEnterBeacon(Beacon beacon);

    void didExitBeacon(Beacon beacon);

    void monitoringDidFail(Error error);
}
```


```
#!java

IBeaconScanner.getInstance().setCallback(this);
```

### Create a Beacon object ###


```
#!java

final Beacon beacon = new Beacon.Builder()
    .setUUID("84be19d4-797d-11e5-8bcf-feff819cdc9f")
    .setMajor(1)
    .setMinor(2)
    .build();
```

### Pass beacons you want to monitor ###

Pass one or more beacons to the library to start getting enter or exit notifies:

```
#!java

IBeaconScanner.getInstance().startMonitoring(beacon);
```

## License

ANDROID-IBEACON-SCANNER is freely distributable under the terms of the [MIT license](http://url/blob/master/LICENSE).

[license-image]: http://img.shields.io/badge/license-MIT-blue.svg?style=flat
[license-url]: LICENSE
