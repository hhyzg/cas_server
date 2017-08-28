package org.apereo.cas.mongo;

import com.mongodb.Mongo;
import com.mongodb.MongoClient;
import com.mongodb.MongoClientOptions;
import com.mongodb.MongoClientURI;
import com.mongodb.MongoCredential;
import com.mongodb.ServerAddress;
import com.mongodb.WriteConcern;
import org.apereo.cas.configuration.model.support.mongo.AbstractMongoInstanceProperties;
import org.apereo.cas.util.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.BeanCreationException;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.core.type.filter.AnnotationTypeFilter;
import org.springframework.data.annotation.Persistent;
import org.springframework.data.mapping.model.CamelCaseAbbreviatingFieldNamingStrategy;
import org.springframework.data.mapping.model.FieldNamingStrategy;
import org.springframework.data.mapping.model.PropertyNameFieldNamingStrategy;
import org.springframework.data.mongodb.MongoDbFactory;
import org.springframework.data.mongodb.core.MongoClientOptionsFactoryBean;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.SimpleMongoDbFactory;
import org.springframework.data.mongodb.core.convert.CustomConversions;
import org.springframework.data.mongodb.core.convert.DbRefResolver;
import org.springframework.data.mongodb.core.convert.DefaultDbRefResolver;
import org.springframework.data.mongodb.core.convert.MappingMongoConverter;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.MongoMappingContext;
import org.springframework.util.ClassUtils;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * This is {@link MongoDbObjectFactory}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
public class MongoDbObjectFactory {
    private static final Logger LOGGER = LoggerFactory.getLogger(MongoDbObjectFactory.class);
    private static final int TIMEOUT = 5000;
    private static final int DEFAULT_PORT = 27017;

    private CustomConversions customConversions = new CustomConversions(Arrays.asList(
            new BaseConverters.LoggerConverter(),
            new BaseConverters.ClassConverter(),
            new BaseConverters.CommonsLogConverter(),
            new BaseConverters.PersonAttributesConverter(),
            new BaseConverters.CacheLoaderConverter(),
            new BaseConverters.RunnableConverter(),
            new BaseConverters.ReferenceQueueConverter(),
            new BaseConverters.ThreadLocalConverter(),
            new BaseConverters.CertPathConverter(),
            new BaseConverters.CaffeinCacheConverter(),
            new BaseConverters.CaffeinCacheLoaderConverter(),
            new BaseConverters.CacheConverter()
    ));

    public MongoDbObjectFactory() {
        this.customConversions = new CustomConversions(new ArrayList<>());
    }

    /**
     * Build mongo template mongo template.
     *
     * @param mongo the mongo
     * @return the mongo template
     */
    public MongoTemplate buildMongoTemplate(final AbstractMongoInstanceProperties mongo) {
        final MongoDbFactory mongoDbFactory = mongoDbFactory(buildMongoDbClient(mongo), mongo.getDatabaseName());
        return new MongoTemplate(mongoDbFactory, mappingMongoConverter(mongoDbFactory));
    }

    /**
     * Build mongo template mongo template.
     *
     * @param clientUri the client uri
     * @return the mongo template
     */
    public MongoTemplate buildMongoTemplate(final String clientUri) {
        final MongoClientURI uri = buildMongoClientURI(clientUri);
        final Mongo mongo = buildMongoDbClient(clientUri);
        final MongoDbFactory mongoDbFactory = mongoDbFactory(mongo, uri.getDatabase());
        return new MongoTemplate(mongoDbFactory, mappingMongoConverter(mongoDbFactory));
    }

    private MongoMappingContext mongoMappingContext() {
        final MongoMappingContext mappingContext = new MongoMappingContext();
        mappingContext.setInitialEntitySet(getInitialEntitySet());
        mappingContext.setSimpleTypeHolder(this.customConversions.getSimpleTypeHolder());
        mappingContext.setFieldNamingStrategy(this.fieldNamingStrategy());
        return mappingContext;
    }

    private MappingMongoConverter mappingMongoConverter(final MongoDbFactory mongoDbFactory) {
        final DbRefResolver dbRefResolver = new DefaultDbRefResolver(mongoDbFactory);
        final MappingMongoConverter converter = new MappingMongoConverter(dbRefResolver, this.mongoMappingContext());
        converter.setCustomConversions(customConversions);
        return converter;
    }

    private MongoDbFactory mongoDbFactory(final Mongo mongo, final String databaseName) {
        return new SimpleMongoDbFactory(mongo, databaseName, null, null);
    }

    private Set<Class<?>> getInitialEntitySet() {
        final Set<Class<?>> initialEntitySet = new HashSet<>();
        for (final String basePackage : getMappingBasePackages()) {
            initialEntitySet.addAll(scanForEntities(basePackage));
        }
        return initialEntitySet;
    }

    protected Collection<String> getMappingBasePackages() {
        return CollectionUtils.wrap(getClass().getPackage().getName());
    }

    private Set<Class<?>> scanForEntities(final String basePackage) {
        if (!StringUtils.hasText(basePackage)) {
            return new HashSet<>();
        }

        final Set<Class<?>> initialEntitySet = new HashSet<>();

        if (StringUtils.hasText(basePackage)) {
            final ClassPathScanningCandidateComponentProvider componentProvider = new ClassPathScanningCandidateComponentProvider(
                    false);
            componentProvider.addIncludeFilter(new AnnotationTypeFilter(Document.class));
            componentProvider.addIncludeFilter(new AnnotationTypeFilter(Persistent.class));

            for (final BeanDefinition candidate : componentProvider.findCandidateComponents(basePackage)) {
                try {
                    initialEntitySet.add(ClassUtils.forName(candidate.getBeanClassName(), getClass().getClassLoader()));
                } catch (final Exception e) {
                    LOGGER.error(e.getMessage(), e);
                }
            }
        }

        return initialEntitySet;
    }

    private boolean abbreviateFieldNames() {
        return false;
    }

    private FieldNamingStrategy fieldNamingStrategy() {
        return abbreviateFieldNames() ? new CamelCaseAbbreviatingFieldNamingStrategy()
                : PropertyNameFieldNamingStrategy.INSTANCE;
    }

    private MongoClientOptionsFactoryBean buildMongoDbClientOptionsFactoryBean(final AbstractMongoInstanceProperties mongo) {
        try {
            final MongoClientOptionsFactoryBean bean = new MongoClientOptionsFactoryBean();
            bean.setWriteConcern(WriteConcern.valueOf(mongo.getWriteConcern()));
            bean.setHeartbeatConnectTimeout((int) mongo.getTimeout());
            bean.setHeartbeatSocketTimeout((int) mongo.getTimeout());
            bean.setMaxConnectionLifeTime(mongo.getConns().getLifetime());
            bean.setSocketKeepAlive(mongo.isSocketKeepAlive());
            bean.setMaxConnectionIdleTime((int) mongo.getIdleTimeout());
            bean.setConnectionsPerHost(mongo.getConns().getPerHost());
            bean.setSocketTimeout((int) mongo.getTimeout());
            bean.setConnectTimeout((int) mongo.getTimeout());
            bean.afterPropertiesSet();
            return bean;
        } catch (final Exception e) {
            throw new BeanCreationException(e.getMessage(), e);
        }
    }

    private MongoClientOptions buildMongoDbClientOptions(final AbstractMongoInstanceProperties mongo) {
        try {
            return buildMongoDbClientOptionsFactoryBean(mongo).getObject();
        } catch (final Exception e) {
            throw new BeanCreationException(e.getMessage(), e);
        }
    }

    private MongoClientOptions buildMongoDbClientOptions() {
        try {
            final MongoClientOptionsFactoryBean bean = new MongoClientOptionsFactoryBean();
            bean.setSocketTimeout(TIMEOUT);
            bean.setConnectTimeout(TIMEOUT);
            bean.setMaxWaitTime(TIMEOUT);
            bean.afterPropertiesSet();
            return bean.getObject();
        } catch (final Exception e) {
            throw new BeanCreationException(e.getMessage(), e);
        }
    }


    private Mongo buildMongoDbClient(final AbstractMongoInstanceProperties mongo) {
        final ServerAddress addr = new ServerAddress(
                mongo.getHost(),
                mongo.getPort());
        return new MongoClient(addr,
                CollectionUtils.wrap(
                        MongoCredential.createCredential(
                                mongo.getUserId(),
                                mongo.getDatabaseName(),
                                mongo.getPassword().toCharArray())),
                buildMongoDbClientOptions(mongo));
    }

    private Mongo buildMongoDbClient(final String clientUri) {
        final MongoClientURI mongoClientUri = buildMongoClientURI(clientUri);
        final MongoCredential credential = MongoCredential.createCredential(
                mongoClientUri.getUsername(),
                mongoClientUri.getDatabase(),
                mongoClientUri.getPassword());

        final String hostUri = mongoClientUri.getHosts().get(0);
        final String[] host = hostUri.split(":");
        final ServerAddress addr = new ServerAddress(
                host[0], host.length > 1 ? Integer.parseInt(host[1]) : DEFAULT_PORT);
        final MongoClient client = new MongoClient(addr,
                Collections.singletonList(credential),
                buildMongoDbClientOptions());
        return client;
    }

    private MongoClientURI buildMongoClientURI(final String clientUri) {
        return new MongoClientURI(clientUri);
    }

}
