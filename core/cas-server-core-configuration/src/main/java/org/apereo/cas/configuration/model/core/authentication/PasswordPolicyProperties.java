package org.apereo.cas.configuration.model.core.authentication;

import org.apereo.cas.configuration.model.support.ldap.AbstractLdapProperties;
import org.springframework.util.LinkedCaseInsensitiveMap;

import javax.security.auth.login.LoginException;
import java.io.Serializable;
import java.util.Map;

/**
 * Configuration properties class for password.policy.
 *
 * @author Dmitriy Kopylenko
 * @since 5.0.0
 */
public class PasswordPolicyProperties implements Serializable {

    private static final long serialVersionUID = -3878237508646993100L;
    /**
     * Key-value structure (Map) that indicates a list of boolean attributes as keys.
     * If either attribute value is true, indicating an account state is flagged,
     * the corresponding error can be thrown.
     * Example {@code accountLocked=javax.security.auth.login.AccountLockedException}
     */
    private Map<String, Class<LoginException>> policyAttributes = new LinkedCaseInsensitiveMap<>();

    /**
     * Whether password policy should be enabled.
     */
    private boolean enabled = true;

    /**
     * An implementation of a policy class that knows how to handle LDAP responses.
     * The class must be an implementation of {@code org.ldaptive.auth.AuthenticationResponseHandler}.
     */
    private String customPolicyClass;
    /**
     * When dealing with FreeIPA, indicates the number of allows login failures.
     */
    private int loginFailures = 5;

    /**
     * Used by an account state handling policy that only calculates account warnings
     * in case the LDAP entry carries an attribute {@link #warningAttributeName} 
     * whose value matches this field.
     */
    private String warningAttributeValue;
    /**
     * Used by an account state handling policy that only calculates account warnings
     * in case the LDAP entry carries this attribute.
     */
    private String warningAttributeName;
    /**
     * Indicates if warning should be displayed, when the ldap attribute value
     * matches the {@link #warningAttributeValue}.
     */
    private boolean displayWarningOnMatch = true;

    /**
     * Always display the password expiration warning regardless.
     */
    private boolean warnAll;
    /**
     * In the event that AD is chosen as the type, this is used to calculate
     * a warning period to see if account expiry is within the calculated window.
     */
    private int warningDays = 30;
    /**
     * LDAP type. Accepted values are {@code GENERIC,AD,FreeIPA,EDirectory}
     */
    private AbstractLdapProperties.LdapType type = AbstractLdapProperties.LdapType.GENERIC;

    public AbstractLdapProperties.LdapType getType() {
        return type;
    }

    public String getCustomPolicyClass() {
        return customPolicyClass;
    }

    public void setCustomPolicyClass(final String customPolicyClass) {
        this.customPolicyClass = customPolicyClass;
    }

    public void setType(final AbstractLdapProperties.LdapType type) {
        this.type = type;
    }

    public void setWarnAll(final boolean warnAll) {
        this.warnAll = warnAll;
    }

    public int getWarningDays() {
        return warningDays;
    }

    public void setWarningDays(final int warningDays) {
        this.warningDays = warningDays;
    }

    public String getWarningAttributeValue() {
        return warningAttributeValue;
    }

    public void setWarningAttributeValue(final String warningAttributeValue) {
        this.warningAttributeValue = warningAttributeValue;
    }

    public String getWarningAttributeName() {
        return warningAttributeName;
    }

    public void setWarningAttributeName(final String warningAttributeName) {
        this.warningAttributeName = warningAttributeName;
    }

    public boolean isDisplayWarningOnMatch() {
        return displayWarningOnMatch;
    }

    public void setDisplayWarningOnMatch(final boolean displayWarningOnMatch) {
        this.displayWarningOnMatch = displayWarningOnMatch;
    }

    public boolean isWarnAll() {
        return warnAll;
    }

    public int getLoginFailures() {
        return loginFailures;
    }

    public void setLoginFailures(final int loginFailures) {
        this.loginFailures = loginFailures;
    }

    public Map<String, Class<LoginException>> getPolicyAttributes() {
        return policyAttributes;
    }

    public void setPolicyAttributes(final Map<String, Class<LoginException>> policyAttributes) {
        this.policyAttributes = policyAttributes;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(final boolean enabled) {
        this.enabled = enabled;
    }
}
