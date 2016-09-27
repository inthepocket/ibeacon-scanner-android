package inthepocket.mobi.beacons.ibeaconscanning;

import java.security.InvalidParameterException;
import java.util.UUID;

/**
 * Created by eliaslecomte on 23/09/2016.
 */

public final class Region implements inthepocket.mobi.beacons.ibeaconscanning.interfaces.Region
{
    private final static int MAJOR_MINOR_MAX_VALUE = 65535;

    private UUID uuid;
    private int major;
    private int minor;
    private String identifier;

    private Region()
    {
    }

    private Region(final RegionBuilder regionBuilder)
    {
        this.uuid = regionBuilder.uuid;
        this.major = regionBuilder.major;
        this.minor = regionBuilder.minor;
        this.identifier = regionBuilder.identifier;
    }

    @Override
    public UUID getId1()
    {
        return this.uuid;
    }

    @Override
    public int getId2()
    {
        return this.major;
    }

    @Override
    public int getId3()
    {
        return this.minor;
    }

    @Override
    public String getIdentifier()
    {
        return this.identifier;
    }

    //region Builder

    public static class RegionBuilder
    {
        private UUID uuid;
        private int major;
        private int minor;
        private String identifier;

        public RegionBuilder setId1(final UUID uuid)
        {
            this.uuid = uuid;

            return this;
        }

        public RegionBuilder setId1(final String uuid)
        {
            this.uuid = UUID.fromString(uuid);

            return this;
        }

        public RegionBuilder setId2(final int major)
        {
            this.major = major;

            return this;
        }

        public RegionBuilder setId3(final int minor)
        {
            this.minor = minor;

            return this;
        }

        public RegionBuilder setIdentifier(final String identifier)
        {
            this.identifier = identifier;

            return this;
        }

        public Region build()
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
