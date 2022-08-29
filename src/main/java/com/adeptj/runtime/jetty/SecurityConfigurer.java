package com.adeptj.runtime.jetty;

import org.eclipse.jetty.security.ConstraintMapping;
import org.eclipse.jetty.security.ConstraintSecurityHandler;
import org.eclipse.jetty.security.HashLoginService;
import org.eclipse.jetty.security.UserStore;
import org.eclipse.jetty.security.authentication.FormAuthenticator;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.util.security.Constraint;
import org.eclipse.jetty.util.security.Password;

public class SecurityConfigurer {

    public void configure(ServletContextHandler context) {
        Constraint constraint = new Constraint();
        constraint.setName(Constraint.__FORM_AUTH);
        constraint.setRoles(new String[]{"OSGiAdmin"});
        constraint.setAuthenticate(true);

        ConstraintMapping constraintMapping = new ConstraintMapping();
        constraintMapping.setConstraint(constraint);
        constraintMapping.setPathSpec("/system/console/*");

        ConstraintSecurityHandler securityHandler = new ConstraintSecurityHandler();
        securityHandler.addConstraintMapping(constraintMapping);

        HashLoginService loginService = new HashLoginService();
        UserStore store = new UserStore();
        store.addUser("admin", new Password("admin"), new String[] {"OSGiAdmin"});
        loginService.setUserStore(store);
        securityHandler.setLoginService(loginService);

        FormAuthenticator authenticator = new FormAuthenticator("/admin/login", "/admin/login", false);
        securityHandler.setAuthenticator(authenticator);

        context.setSecurityHandler(securityHandler);
    }
}
