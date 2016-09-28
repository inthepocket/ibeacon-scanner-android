package mobi.inthepocket.android.beacons.ibeaconscanner.utils;

import android.os.Handler;
import android.support.annotation.NonNull;
import android.util.SparseArray;

import mobi.inthepocket.android.beacons.ibeaconscanner.Region;

/**
 * Created by eliaslecomte on 28/09/2016.
 *
 * This class generates {@link ExitCallback#didExit(Region)} when a region has not been entered for
 * {@link #exitTimeoutInMillis}.
 */

public class OnExitHandler
{
    private final Handler handler;
    private final SparseArray<Runnable> sparseArray;
    private final long exitTimeoutInMillis;
    private final ExitCallback exitCallback;

    public OnExitHandler(@NonNull final ExitCallback exitCallback, final long exitTimeoutInMillis)
    {
        this.handler = new Handler();
        this.sparseArray = new SparseArray<>();
        this.exitCallback = exitCallback;
        this.exitTimeoutInMillis = exitTimeoutInMillis;
    }

    /**
     * By passing the {@link Region}'s, the {@link OnExitHandler} will call {@link ExitCallback#didExit(Region)}
     * when it has not been entered for {@link #exitTimeoutInMillis}.
     * @param region
     */
    public synchronized void enterRegion(final Region region)
    {
        final int id = region.hashCode();

        if (this.sparseArray.get(id) != null)
        {
            // runnable already exists, remove it and post again
            final Runnable runnable = this.sparseArray.get(id);
            this.handler.removeCallbacks(runnable);
            this.handler.postDelayed(runnable, this.exitTimeoutInMillis);
        }
        else
        {
            // first time, post it!
            final Runnable runnable = new Runnable()
            {
                @Override
                public void run()
                {
                    OnExitHandler.this.exitCallback.didExit(region);
                    OnExitHandler.this.sparseArray.remove(id);
                }
            };

            this.sparseArray.append(id, runnable);
            this.handler.postDelayed(runnable, this.exitTimeoutInMillis);
        }
    }

    public interface ExitCallback
    {
        void didExit(Region region);
    }
}
