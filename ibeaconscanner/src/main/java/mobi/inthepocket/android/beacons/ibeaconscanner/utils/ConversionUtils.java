package mobi.inthepocket.android.beacons.ibeaconscanner.utils;

import android.support.annotation.NonNull;

import java.util.UUID;

/**
 * Utils class to help with conversion of Bytes to Hex.
 */

public final class ConversionUtils
{
    private static final char[] HEX_ARRAY = "0123456789ABCDEF".toCharArray();

    private ConversionUtils()
    {
    }

    /**
     * Converts byte[] to an iBeacon {@link UUID}.
     * From http://stackoverflow.com/a/9855338.
     *
     * @param bytes Byte[] to convert
     * @return UUID
     */
    public static UUID bytesToUuid(@NonNull final byte[] bytes)
    {
        final char[] hexChars = new char[bytes.length * 2];
        for ( int j = 0; j < bytes.length; j++ )
        {
            final int v = bytes[j] & 0xFF;
            hexChars[j * 2] = HEX_ARRAY[v >>> 4];
            hexChars[j * 2 + 1] = HEX_ARRAY[v & 0x0F];
        }

        final String hex = new String(hexChars);

        return UUID.fromString(hex.substring(0, 8) + "-" +
                hex.substring(8, 12) + "-" +
                hex.substring(12, 16) + "-" +
                hex.substring(16, 20) + "-" +
                hex.substring(20, 32));
    }

    /**
     * Converts a {@link UUID} to a byte[]. This is used to create a {@link android.bluetooth.le.ScanFilter}.
     * From http://stackoverflow.com/questions/29664316/bluetooth-le-scan-filter-not-working.
     *
     * @param uuid UUID to convert to a byte[]
     * @return byte[]
     */
    public static byte[] UuidToByteArray(@NonNull final UUID uuid)
    {
        final String hex = uuid.toString().replace("-","");
        final int length = hex.length();
        final byte[] result = new byte[length / 2];

        for (int i = 0; i < length; i += 2)
        {
            result[i / 2] = (byte) ((Character.digit(hex.charAt(i), 16) << 4) + Character.digit(hex.charAt(i+1), 16));
        }

        return result;
    }

    /**
     * Convert major or minor to hex byte[]. This is used to create a {@link android.bluetooth.le.ScanFilter}.
     *
     * @param value major or minor to convert to byte[]
     * @return byte[]
     */
    public static byte[] integerToByteArray(final int value)
    {
        final byte[] result = new byte[2];
        result[0] = (byte) (value / 256);
        result[1] = (byte) (value % 256);

        return result;
    }

    /**
     * Convert major and minor byte array to integer.
     *
     * @param byteArray that contains major and minor byte
     * @return integer value for major and minor
     */
    public static int byteArrayToInteger(final byte[] byteArray)
    {
        return (byteArray[0] & 0xff) * 0x100 + (byteArray[1] & 0xff);
    }
}
