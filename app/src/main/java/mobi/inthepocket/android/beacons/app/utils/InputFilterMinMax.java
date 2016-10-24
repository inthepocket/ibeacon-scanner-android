package mobi.inthepocket.android.beacons.app.utils;

import android.text.InputFilter;
import android.text.Spanned;

/**
 * Created by elias on 23/10/2016.
 */

public class InputFilterMinMax implements InputFilter
{
    private final int min;
    private final int max;

    public InputFilterMinMax(final int min, final int max)
    {
        this.min = min;
        this.max = max;
    }

    public InputFilterMinMax(final String min, final String max)
    {
        this.min = Integer.parseInt(min);
        this.max = Integer.parseInt(max);
    }

    @Override
    public CharSequence filter(final CharSequence source, final int start, final int end, final Spanned dest, final int dstart, final int dend)
    {
        try
        {
            final int input = Integer.parseInt(dest.toString() + source.toString());
            if (isInRange(this.min, this.max, input))
            {
                return null;
            }
        }
        catch (final NumberFormatException numberFormatException)
        {
        }

        return "";
    }

    //region Helpers

    private static boolean isInRange(final int a, final int b, final int c)
    {
        return b > a ? c >= a && c <= b : c >= b && c <= a;
    }

    //endregion
}
