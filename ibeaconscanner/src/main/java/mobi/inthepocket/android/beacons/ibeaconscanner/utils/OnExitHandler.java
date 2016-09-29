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
    private final SparseArray<Runnable> runnableSparseArray;
    private final long exitTimeoutInMillis;
    private final ExitCallback exitCallback;

    public OnExitHandler(@NonNull final ExitCallback exitCallback, final long exitTimeoutInMillis)
    {
        this.handler = new Handler();
        this.runnableSparseArray = new SparseArray<>();
        this.exitCallback = exitCallback;
        this.exitTimeoutInMillis = exitTimeoutInMillis;
    }

    /**
     * By passing the {@link Region}'s, the {@link OnExitHandler} will call {@link ExitCallback#didExit(Region)}
     * when it has not been entered for {@link #exitTimeoutInMillis}.
     * @param region
     */
    public synchronized void enterRegion(@NonNull final Region region)
    {
        // using the {@link Region#hashCode} could collide but the chances of this happening are relatively low
        final int id = region.hashCode();

        if (this.runnableSparseArray.get(id) != null)
        {
            // runnable already exists, remove it and post again
            final Runnable runnable = this.runnableSparseArray.get(id);
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
                    OnExitHandler.this.runnableSparseArray.remove(id);
                }
            };

            this.runnableSparseArray.append(id, runnable);
            this.handler.postDelayed(runnable, this.exitTimeoutInMillis);
        }
    }

    public interface ExitCallback
    {
        void didExit(Region region);
    }
}
