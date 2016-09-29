package mobi.inthepocket.android.beacons.ibeaconscanner.handlers;

import android.os.Handler;
import android.util.SparseArray;

/**
 * Created by eliaslecomte on 28/09/2016.
 */

public abstract class TimeoutHandler<T>
{
    private final Handler handler;
    private final SparseArray<Runnable> sparseArray;
    private final long timeoutInMillis;
    private final TimeoutCallback<T> timeoutCallback;

    public TimeoutHandler(final TimeoutCallback<T> timeoutCallback, final long timeoutInMillis)
    {
        this.handler = new Handler();
        this.sparseArray = new SparseArray<>();
        this.timeoutInMillis = timeoutInMillis;
        this.timeoutCallback = timeoutCallback;
    }

    public synchronized void passItem(final T item)
    {
        // using the {@link T#hashCode} could collide but the chances of this happening are relatively low
        final int id = item.hashCode();

        if (this.sparseArray.get(id) != null)
        {
            // runnable already exists, remove it and post again
            final Runnable runnable = this.sparseArray.get(id);
            this.handler.removeCallbacks(runnable);
            this.handler.postDelayed(runnable, this.timeoutInMillis);
        }
        else
        {
            // first time, post it!
            final Runnable runnable = new Runnable()
            {
                @Override
                public void run()
                {
                    TimeoutHandler.this.timeoutCallback.timedOut(item);
                    TimeoutHandler.this.sparseArray.remove(id);
                }
            };

            this.sparseArray.append(id, runnable);
            this.handler.postDelayed(runnable, this.timeoutInMillis);
        }
    }

    public interface TimeoutCallback<T>
    {
        void timedOut(T t);
    }
}
