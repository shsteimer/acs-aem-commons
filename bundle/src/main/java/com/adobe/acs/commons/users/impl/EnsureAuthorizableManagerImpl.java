
package com.adobe.acs.commons.users.impl;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.management.DynamicMBean;
import javax.management.NotCompliantMBeanException;

import org.apache.commons.lang.StringUtils;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Properties;
import org.apache.felix.scr.annotations.Property;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.ReferenceCardinality;
import org.apache.felix.scr.annotations.ReferencePolicy;
import org.apache.felix.scr.annotations.References;
import org.apache.felix.scr.annotations.Service;
import org.apache.sling.commons.osgi.PropertiesUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.adobe.granite.jmx.annotation.AnnotatedStandardMBean;

@Component
@Properties({ @Property(label = "MBean Name", name = "jmx.objectname",
        value = "com.adobe.acs.commons:type=Ensure Service User", propertyPrivate = true) })
@References({
        @Reference(referenceInterface = EnsureServiceUser.class, policy = ReferencePolicy.DYNAMIC,
                cardinality = ReferenceCardinality.OPTIONAL_MULTIPLE),
        @Reference(referenceInterface = EnsureGroup.class, policy = ReferencePolicy.DYNAMIC,
                cardinality = ReferenceCardinality.OPTIONAL_MULTIPLE) })
@Service(value = DynamicMBean.class)
public class EnsureAuthorizableManagerImpl extends AnnotatedStandardMBean implements EnsureAuthorizableManager {

    private final Logger log = LoggerFactory.getLogger(EnsureAuthorizableManagerImpl.class);

    private Map<String, EnsureServiceUser> ensureServiceUsers = new ConcurrentHashMap<String, EnsureServiceUser>();
    private Map<String, EnsureGroup> ensureGroups = new ConcurrentHashMap<>();

    public EnsureAuthorizableManagerImpl() throws NotCompliantMBeanException {
        super(EnsureAuthorizableManager.class);
    }

    @Override
    public final void ensureAll() {
        for (final EnsureServiceUser ensureServiceUser : ensureServiceUsers.values()) {
            try {
                ensureServiceUser.ensure(ensureServiceUser.getOperation(), ensureServiceUser.getServiceUser());
            } catch (EnsureAuthorizableException e) {
                log.error("Error Ensuring Service User [ {} ]", ensureServiceUser.getServiceUser().getPrincipalName(),
                        e);
            }
        }

        for (final EnsureGroup ensureGroup : ensureGroups.values()) {
            try {
                ensureGroup.ensure(ensureGroup.getOperation(), ensureGroup.getGroup());
            } catch (EnsureAuthorizableException e) {
                log.error("Error Ensuring Group [ {} ]", ensureGroup.getGroup().getPrincipalName(), e);
            }
        }
    }

    @Override
    public final void ensurePrincipalName(String principalName) {
        for (final EnsureServiceUser ensureServiceUser : ensureServiceUsers.values()) {
            if (StringUtils.equals(principalName, ensureServiceUser.getServiceUser().getPrincipalName())) {
                try {
                    ensureServiceUser.ensure(ensureServiceUser.getOperation(), ensureServiceUser.getServiceUser());
                } catch (EnsureAuthorizableException e) {
                    log.error("Error Ensuring Service User [ {} ]", ensureServiceUser.getServiceUser()
                            .getPrincipalName(), e);
                }
            }
        }

        for (final EnsureGroup ensureGroup : ensureGroups.values()) {
            if (StringUtils.equals(principalName, ensureGroup.getGroup().getPrincipalName())) {
                try {
                    ensureGroup.ensure(ensureGroup.getOperation(), ensureGroup.getGroup());
                } catch (EnsureAuthorizableException e) {
                    log.error("Error Ensuring Group [ {} ]", ensureGroup.getGroup().getPrincipalName(), e);
                }
            }
        }
    }

    protected final void bindEnsureServiceUser(final EnsureServiceUser service, final Map<Object, Object> props) {
        final String type = PropertiesUtil.toString(props.get("service.pid"), null);
        if (type != null) {
            this.ensureServiceUsers.put(type, service);
        }
    }

    protected final void unbindEnsureServiceUser(final EnsureServiceUser service, final Map<Object, Object> props) {
        final String type = PropertiesUtil.toString(props.get("service.pid"), null);
        if (type != null) {
            this.ensureServiceUsers.remove(type);
        }
    }

    protected final void bindEnsureGroup(final EnsureGroup service, final Map<Object, Object> props) {
        final String type = PropertiesUtil.toString(props.get("service.pid"), null);
        if (type != null) {
            this.ensureGroups.put(type, service);
        }
    }

    protected final void unbindEnsureGroup(final EnsureGroup service, final Map<Object, Object> props) {
        final String type = PropertiesUtil.toString(props.get("service.pid"), null);
        if (type != null) {
            this.ensureGroups.remove(type);
        }
    }
}
