package org.apereo.cas.authentication;

import org.apache.commons.lang3.StringUtils;
import org.apereo.cas.authentication.principal.Principal;
import org.apereo.cas.authentication.principal.PrincipalFactory;
import org.apereo.cas.authentication.surrogate.SurrogateAuthenticationService;
import org.apereo.cas.services.RegisteredService;
import org.apereo.cas.services.RegisteredServiceAccessStrategyUtils;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.util.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.security.auth.login.CredentialNotFoundException;
import javax.security.auth.login.FailedLoginException;
import java.util.Map;

/**
 * This is {@link SurrogateAuthenticationPostProcessor}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
public class SurrogateAuthenticationPostProcessor implements AuthenticationPostProcessor {

    private static final Logger LOGGER = LoggerFactory.getLogger(SurrogateAuthenticationPostProcessor.class);

    private final PrincipalFactory principalFactory;
    private final SurrogateAuthenticationService surrogateAuthenticationService;
    private final ServicesManager servicesManager;

    public SurrogateAuthenticationPostProcessor(final PrincipalFactory principalFactory,
                                                final SurrogateAuthenticationService surrogateAuthenticationService, 
                                                final ServicesManager servicesManager) {
        this.principalFactory = principalFactory;
        this.surrogateAuthenticationService = surrogateAuthenticationService;
        this.servicesManager = servicesManager;
    }

    @Override
    public void process(final AuthenticationBuilder builder, final AuthenticationTransaction transaction) throws AuthenticationException {
        try {
            final SurrogateUsernamePasswordCredential surrogateCredentials = (SurrogateUsernamePasswordCredential) transaction.getCredential();
            final String targetUserId = surrogateCredentials.getSurrogateUsername();
            if (StringUtils.isBlank(targetUserId)) {
                LOGGER.error("No surrogate username was specified as part of the credential");
                throw new CredentialNotFoundException("Missing surrogate username in credential");
            }
            final Authentication authentication = builder.build();
            final Principal principal = authentication.getPrincipal();
            LOGGER.debug("Authenticated [{}] will be checked for surrogate eligibility next...", principal);

            if (transaction.getService() != null) {
                final RegisteredService svc = this.servicesManager.findServiceBy(transaction.getService());
                RegisteredServiceAccessStrategyUtils.ensurePrincipalAccessIsAllowedForService(transaction.getService(), svc, authentication);
            }
            
            if (this.surrogateAuthenticationService.canAuthenticateAs(targetUserId, principal, transaction.getService())) {
                LOGGER.debug("Principal [{}] is authorized to authenticate as [{}]", principal, targetUserId);
                builder.setPrincipal(this.principalFactory.createPrincipal(targetUserId));
                return;
            }
            LOGGER.error("Principal [{}] is unable/unauthorized to authenticate as [{}]", principal, targetUserId);
            throw new FailedLoginException();
        } catch (final Exception e) {
            final Map<String, Class<? extends Throwable>> map = CollectionUtils.wrap(getClass().getSimpleName(), SurrogateAuthenticationException.class);
            throw new AuthenticationException(map);
        }
    }

    @Override
    public boolean supports(final Credential credential) {
        return credential.getClass().equals(SurrogateUsernamePasswordCredential.class);
    }
}
