package org.apereo.cas.configuration.model.support.mfa;

import org.apereo.cas.configuration.model.support.jpa.AbstractJpaProperties;
import org.apereo.cas.configuration.model.support.quartz.ScheduledJobProperties;
import org.apereo.cas.configuration.support.SpringResourceProperties;
import org.springframework.boot.context.properties.NestedConfigurationProperty;

import java.util.concurrent.TimeUnit;

/**
 * This is {@link U2FMultifactorProperties}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
public class U2FMultifactorProperties extends BaseMultifactorProvider {
    private static final long serialVersionUID = 6151350313777066398L;

    /**
     * Store device registration records inside a JDBC resource.
     */
    private Jpa jpa = new Jpa();

    /**
     * Expire and forget device registration requests after this period.
     */
    private long expireRegistrations = 30;
    /**
     * Device registration requests expiration time unit.
     */
    private TimeUnit expireRegistrationsTimeUnit = TimeUnit.SECONDS;

    /**
     * Expire and forget device registration records after this period.
     */
    private long expireDevices = 30;
    /**
     * Device registration record expiration time unit.
     */
    private TimeUnit expireDevicesTimeUnit = TimeUnit.DAYS;

    /**
     * Store device registration records inside a static JSON resource.
     */
    private Json json = new Json();
    /**
     * Clean up expired records via a background cleaner process.
     */
    @NestedConfigurationProperty
    private ScheduledJobProperties cleaner = new ScheduledJobProperties("PT10S", "PT1M");

    public U2FMultifactorProperties() {
        setId("mfa-u2f");
    }

    public ScheduledJobProperties getCleaner() {
        return cleaner;
    }

    public void setCleaner(final ScheduledJobProperties cleaner) {
        this.cleaner = cleaner;
    }

    public Json getJson() {
        return json;
    }

    public void setJson(final Json json) {
        this.json = json;
    }

    public long getExpireRegistrations() {
        return expireRegistrations;
    }

    public void setExpireRegistrations(final long expireRegistrations) {
        this.expireRegistrations = expireRegistrations;
    }

    public TimeUnit getExpireRegistrationsTimeUnit() {
        return expireRegistrationsTimeUnit;
    }

    public void setExpireRegistrationsTimeUnit(final TimeUnit expireRegistrationsTimeUnit) {
        this.expireRegistrationsTimeUnit = expireRegistrationsTimeUnit;
    }

    public long getExpireDevices() {
        return expireDevices;
    }

    public void setExpireDevices(final long expireDevices) {
        this.expireDevices = expireDevices;
    }

    public TimeUnit getExpireDevicesTimeUnit() {
        return expireDevicesTimeUnit;
    }

    public void setExpireDevicesTimeUnit(final TimeUnit expireDevicesTimeUnit) {
        this.expireDevicesTimeUnit = expireDevicesTimeUnit;
    }

    public static class Json extends SpringResourceProperties {
        private static final long serialVersionUID = -6883660787308509919L;
    }

    public Jpa getJpa() {
        return jpa;
    }

    public void setJpa(final Jpa jpa) {
        this.jpa = jpa;
    }

    public static class Jpa extends AbstractJpaProperties {
        private static final long serialVersionUID = -4334840263678287815L;
    }

}

