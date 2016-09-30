package mobi.inthepocket.android.beacons.ibeaconscanner.interfaces;

import java.util.UUID;

/**
 * Created by eliaslecomte on 23/09/2016.
 */

public interface Region
{
    UUID getUUID();
    int getMajor();
    int getMinor();
}
