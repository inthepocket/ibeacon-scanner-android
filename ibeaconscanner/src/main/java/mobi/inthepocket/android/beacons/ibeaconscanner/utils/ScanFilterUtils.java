package mobi.inthepocket.android.beacons.ibeaconscanner.utils;

import android.annotation.TargetApi;
import android.bluetooth.le.ScanFilter;
import android.os.Build;
import android.support.annotation.NonNull;

import mobi.inthepocket.android.beacons.ibeaconscanner.interfaces.BeaconInterface;

/**
 * Utils class to create {@link ScanFilter}s for a {@link BeaconInterface}.
 */

public final class ScanFilterUtils
{
    private static final int MANUFACTURER_ID = 76;

    private ScanFilterUtils()
    {
    }

    /**
     * Return a {@link ScanFilter} matching the given {@link BeaconInterface}.
     *
     * @param beacon to create {@link ScanFilter} for
     * @return {@link ScanFilter} that matches {@code beacon}
     */
    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public static ScanFilter getScanFilter(@NonNull final BeaconInterface beacon)
    {
        final ScanFilter.Builder builder = new ScanFilter.Builder();

        // the manufacturer data byte is the filter!
        final byte[] manufacturerData = new byte[]
        {
                0,0,

                // uuid
                0,0,0,0,
                0,0,
                0,0,
                0,0,0,0,0,0,0,0,

                // major
                0,0,

                // minor
                0,0,

                0
        };

        // the mask tells what bytes in the filter need to match, 1 if it has to match, 0 if not
        final byte[] manufacturerDataMask = new byte[]
        {
                0,0,

                // uuid
                1,1,1,1,
                1,1,
                1,1,
                1,1,1,1,1,1,1,1,

                // major
                1,1,

                // minor
                1,1,

                0
        };

        // copy UUID (with no dashes) into data array
        System.arraycopy(ConversionUtils.UuidToByteArray(beacon.getUUID()), 0, manufacturerData, 2, 16);

        // copy major into data array
        System.arraycopy(ConversionUtils.integerToByteArray(beacon.getMajor()), 0, manufacturerData, 18, 2);

        // copy minor into data array
        System.arraycopy(ConversionUtils.integerToByteArray(beacon.getMinor()), 0, manufacturerData, 20, 2);

        builder.setManufacturerData(
                MANUFACTURER_ID,
                manufacturerData,
                manufacturerDataMask);

        return builder.build();
    }
}
