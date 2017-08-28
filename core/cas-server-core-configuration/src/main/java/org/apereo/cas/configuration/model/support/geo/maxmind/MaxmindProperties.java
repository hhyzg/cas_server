package org.apereo.cas.configuration.model.support.geo.maxmind;

import org.springframework.core.io.Resource;

import java.io.Serializable;

/**
 * This is {@link MaxmindProperties}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */

public class MaxmindProperties implements Serializable {

    private static final long serialVersionUID = 7883029275219817797L;
    /**
     * Path to the location of the database file containing cities.
     */
    private Resource cityDatabase;
    /**
     * Path to the location of the database file containing countries.
     */
    private Resource countryDatabase;

    public Resource getCityDatabase() {
        return cityDatabase;
    }

    public void setCityDatabase(final Resource cityDatabase) {
        this.cityDatabase = cityDatabase;
    }

    public Resource getCountryDatabase() {
        return countryDatabase;
    }

    public void setCountryDatabase(final Resource countryDatabase) {
        this.countryDatabase = countryDatabase;
    }
}
