package mobi.inthepocket.android.beacons.ibeaconscanner;

import android.annotation.TargetApi;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.security.InvalidParameterException;
import java.util.Objects;
import java.util.UUID;

/**
 * Created by eliaslecomte on 23/09/2016.
 */

@TargetApi(Build.VERSION_CODES.KITKAT)
public final class Region implements mobi.inthepocket.android.beacons.ibeaconscanner.interfaces.Region
{
    private final static int MAJOR_MINOR_MAX_VALUE = 65535;

    private UUID uuid;
    private int major;
    private int minor;
    private String identifier;

    private Region()
    {
    }

    private Region(final Builder builder)
    {
        this.uuid = builder.uuid;
        this.major = builder.major;
        this.minor = builder.minor;
        this.identifier = builder.identifier;
    }

    @NonNull
    @Override
    public UUID getUUID()
    {
        return this.uuid;
    }

    @Override
    public int getMajor()
    {
        return this.major;
    }

    @Override
    public int getMinor()
    {
        return this.minor;
    }

    @Override
    @Nullable
    public String getIdentifier()
    {
        return this.identifier;
    }

    //region Equals

    @Override
    public int hashCode()
    {
        return Objects.hash(this.uuid, this.major, this.minor);
    }

    @Override
    public boolean equals(final Object o)
    {
        if (this == o)
        {
            return true;
        }

        if (o == null || this.getClass() != o.getClass())
        {
            return false;
        }

        final Region that = (Region)o;

        if (this.uuid != that.uuid)
        {
            return false;
        }

        if (this.major != that.major)
        {
            return false;
        }

        //noinspection RedundantIfStatement
        if (this.minor != that.minor)
        {
            return false;
        }

        return true;
    }

    //endregion

    //region Builder

    public static class Builder
    {
        private UUID uuid;
        private int major;
        private int minor;
        private String identifier;

        public Builder()
        {
        }

        /**
         *
         * @param uuid
         * @return
         */
        public Builder setUUID(@NonNull final UUID uuid)
        {
            this.uuid = uuid;

            return this;
        }

        /**
         *
         * @param uuid that will get parsed with {@link UUID#fromString(String)}
         * @return
         */
        public Builder setUUID(@NonNull final String uuid)
        {
            this.uuid = UUID.fromString(uuid);

            return this;
        }

        /**
         * Major should be an integer between 0 and {@link #MAJOR_MINOR_MAX_VALUE}.
         *
         * @param major
         * @return
         */
        public Builder setMajor(final int major)
        {
            this.major = major;

            return this;
        }

        /**
         * Minor should be an integer between 0 and {@link #MAJOR_MINOR_MAX_VALUE}.
         *
         * @param minor
         * @return
         */
        public Builder setMinor(final int minor)
        {
            this.minor = minor;

            return this;
        }

        public Builder setIdentifier(final String identifier)
        {
            this.identifier = identifier;

            return this;
        }

        /**
         * If {@link #uuid}, {@link #major} and {@link #minor} are valid, build returns a new {@link Region} object.
         *
         * @return {@link Region} if the parameters are valid
         * @throws InvalidParameterException
         */
        public Region build() throws InvalidParameterException
        {
            if (this.uuid == null)
            {
                throw new InvalidParameterException("Id1 (uuid) is not set");
            }

            if (this.major < 0 || this.major > MAJOR_MINOR_MAX_VALUE)
            {
                throw new InvalidParameterException("Major should be a number from 0 to " + MAJOR_MINOR_MAX_VALUE);
            }

            if (this.major < 0 || this.major > MAJOR_MINOR_MAX_VALUE)
            {
                throw new InvalidParameterException("Minor should be a number from 0 to " + MAJOR_MINOR_MAX_VALUE);
            }

            return new Region(this);
        }
    }

    //endregion
}
