---
layout: default
title: CAS - Surrogate Authentication
---

# Surrogate Authentication

Surrogate authentication (impersonation) is the ability to authenticate on behalf of another user. The two actors in this case are:

1. The primary admin user whose credentials are verified upon authentication.
2. The surrogate user, selected by the admin, to which CAS will switch after credential verification and is one that is linked to the single sign-on session.

Example use cases for impersonation include:

1. Logging into an application on behalf of a user to execute and make changes.
2. Troubleshoot a bothersome authentication experience with an application on behalf of another user.

Surrogate authentication is enabled by including the following dependencies in the WAR overlay:

```xml
<dependency>
    <groupId>org.apereo.cas</groupId>
    <artifactId>cas-server-support-surrogate-webflow</artifactId>
    <version>${cas.version}</version>
</dependency>
```

## Account Storage

The following account stores may be configured and used to locate surrogates authorized for a particular user.

### Static

Surrogate accounts may be defined statically in the CAS configuration. To see the relevant list of CAS properties, please [review this guide](Configuration-Properties.html#surrogate-authentication).

### JSON

Similar to above, except that surrogate accounts may be defined in an external JSON file whose path is specified via the CAS configuration. The syntax of the JSON file should match the following snippet:

```json
{
    "casuser": ["jsmith", "banderson"],
    "adminuser": ["jsmith", "tomhanks"]
}
```

To see the relevant list of CAS properties, please [review this guide](Configuration-Properties.html#surrogate-authentication).

### LDAP

LDAP support for surrogate authentication is enabled by including the following dependencies in the WAR overlay:

```xml
<dependency>
    <groupId>org.apereo.cas</groupId>
    <artifactId>cas-server-support-surrogate-authentication-ldap</artifactId>
    <version>${cas.version}</version>
</dependency>
```

Surrogate accounts may also be retrieved from an LDAP instance. Such accounts are expected to be found in a configured attribute defined for the primary user in LDAP whose value(s) may be examined against a regular expression pattern of your own choosing to further narrow down the list of authorized surrogate accounts. To see the relevant list of CAS properties, please [review this guide](Configuration-Properties.html#surrogate-authentication).

### JDBC

JDBC support for surrogate authentication is enabled by including the following dependencies in the WAR overlay:

```xml
<dependency>
    <groupId>org.apereo.cas</groupId>
    <artifactId>cas-server-support-surrogate-authentication-jdbc</artifactId>
    <version>${cas.version}</version>
</dependency>
```

Aside from the usual database settings, this mode requires the specification of two SQL queries; one that determines eligibility and one that is able to retrieve
the list of accounts that can be impersonated for a given admin user. To see the relevant list of CAS properties, please [review this guide](Configuration-Properties.html#jdbc-surrogate-accounts).

### REST

REST support for surrogate authentication is enabled by including the following dependencies in the WAR overlay:

```xml
<dependency>
    <groupId>org.apereo.cas</groupId>
    <artifactId>cas-server-support-surrogate-authentication-rest</artifactId>
    <version>${cas.version}</version>
</dependency>
```

| Method       | Description                                                   | Parameter(s)             | Response
|--------------|---------------------------------------------------------------|--------------------------------------
| `GET`        | Whether principal can authenricate as a surrogate account.    | `surrogate`, `principal` | `202`
| `GET`        | List of accounts principal is eligible to impersonate.        | `principal` | JSON list of usernames.

To see the relevant list of CAS properties, please [review this guide](Configuration-Properties.html#rest-surrogate-accounts).

## Account Selection

The surrogate user selection can happen via the following ways.

### Preselected

This is the case where the surrogate user identity is known beforehand and is provided to CAS upon login using a special syntax.
When entering credentials, the following syntax should be used:

```bash
[surrogate-userid][separator][primary-userid]
```

For example, if you are `casuser` and you need to switch to `jsmith` as the surrogate user, the credential id provided to CAS would be `jsmith+casuser` where the separator is `+` and can be altered via the CAS configuration. You will need to provide your own password of course.

### GUI

This is the case where the surrogate user identity is *not* known beforehand, and you wish to choose the account from a prepopulated list. When entering credentials, the following syntax should be used:

```bash
[separator][primary-userid]
```

For example, if you are `casuser` and you need to locate the surrogate account to which you may want to switch, the credential id provided to CAS would be `+casuser` where the separator is `+` and can be altered via the CAS configuration. You will need to provide your own password of course.

## Session Expiration

An impersonation session can be assigned a specific expiration policy that would control how long a surrogate session may last. This means that the SSO session established as part of impersonation will rightly vanish, once the expiration policy dictates as such. It is recommended that you keep the expiration length short (i.e. 30 minutes) to avoid possible security issues.

## Surrogate Authorization

Each surrogate account storage is able to determine the list of impersonatees to enforce authorization rules. Additionally, you may on a per-service level define whether an application is authorized to leverage surrogate authentication and whether the primary user is tagged with enough attributes and entitlements to allow impersonation to execute.

A sample service definition follows:

```json
{
  "@class" : "org.apereo.cas.services.RegexRegisteredService",
  "serviceId" : "testId",
  "name" : "testId",
  "id" : 1,
  "accessStrategy" : {
    "@class" : "org.apereo.cas.services.SurrogateRegisteredServiceAccessStrategy",
    "surrogateEnabled" : true,
    "surrogateSsoEnabled" : false,
    "enabled": true,
    "ssoEnabled": true,
    "surrogateRequiredAttributes" : {
      "@class" : "java.util.HashMap",
      "givenName" : [ "java.util.HashSet", [ "Administrator" ] ]
    }
  }
}
```

The surrogate access strategy is only activated if the establish authentication and SSO session is one of impersonation. In the above example, surrogate access to the application matching `testId` is allowed only if the authenticated primary user carries an attribute `givenName` which contains a value of `Administrator`. While the surrogate authentication session will also not take part in SSO, a *normal* authentication session however will.