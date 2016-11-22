package mobi.inthepocket.android.beacons.ibeaconscanner;

import android.support.annotation.NonNull;

/**
 * Interface {@link ScanService} specifies the contract for a {@link Beacon} scanner.
 */

public interface ScanService
{
    /**
     * Start monitoring {@code beacon} and all previously added {@link Beacon}s.
     *
     * @param beacon to start monitoring
     */
    void startMonitoring(@NonNull final Beacon beacon);

    /**
     * Stop monitoring for {@code beacon}. Keep monitoring for other {@link Beacon}s that were added with
     * {@link #startMonitoring(Beacon)}.
     *
     * @param beacon to stop monitoring
     */
    void stopMonitoring(@NonNull final Beacon beacon);

    /**
     * Starts monitoring for previously added {@link Beacon}s.
     */
    void start();

    /**
     * Stops monitoring and removes all monitored {@link Beacon}s.
     */
    void stop();

    /**
     * Set the {@link IBeaconScanner.Callback} that will be notified for {@link Beacon} enters, exits or if an {@link Error}
     * happened.
     *
     * @param callback that will be notified when something happens
     */
    void setCallback(@NonNull final IBeaconScanner.Callback callback);
}
