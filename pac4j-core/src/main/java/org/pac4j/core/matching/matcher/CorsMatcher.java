package org.pac4j.core.matching.matcher;

import org.pac4j.core.context.HttpConstants;
import org.pac4j.core.context.WebContext;
import org.pac4j.core.context.session.SessionStore;
import org.pac4j.core.util.CommonHelper;
import org.pac4j.core.util.Pac4jConstants;

import java.util.Set;
import java.util.stream.Collectors;

/**
 * Define how the CORS requests are authorized.
 *
 * @author Jerome Leleu
 * @since 4.0.0
 */
public class CorsMatcher implements Matcher {

    private String allowOrigin;

    private String exposeHeaders;

    private int maxAge = -1;

    private Boolean allowCredentials;

    private Set<HttpConstants.HTTP_METHOD> allowMethods;

    private String allowHeaders;

    @Override
    public boolean matches(final WebContext context, final SessionStore sessionStore) {
        CommonHelper.assertNotBlank("allowOrigin", allowOrigin);

        context.setResponseHeader(HttpConstants.ACCESS_CONTROL_ALLOW_ORIGIN_HEADER, allowOrigin);

        if (CommonHelper.isNotBlank(exposeHeaders)) {
            context.setResponseHeader(HttpConstants.ACCESS_CONTROL_EXPOSE_HEADERS_HEADER, exposeHeaders);
        }

        if (maxAge != -1) {
            context.setResponseHeader(HttpConstants.ACCESS_CONTROL_MAX_AGE_HEADER, Pac4jConstants.EMPTY_STRING + maxAge);
        }

        if (allowCredentials != null && allowCredentials) {
            context.setResponseHeader(HttpConstants.ACCESS_CONTROL_ALLOW_CREDENTIALS_HEADER, allowCredentials.toString());
        }

        if (allowMethods != null) {
            final var methods = allowMethods.stream().map(Enum::toString).collect(Collectors.joining(", "));
            context.setResponseHeader(HttpConstants.ACCESS_CONTROL_ALLOW_METHODS_HEADER, methods);
        }

        if (CommonHelper.isNotBlank(allowHeaders)) {
            context.setResponseHeader(HttpConstants.ACCESS_CONTROL_ALLOW_HEADERS_HEADER, allowHeaders);
        }

        return true;
    }

    public String getAllowOrigin() {
        return allowOrigin;
    }

    public void setAllowOrigin(final String allowOrigin) {
        this.allowOrigin = allowOrigin;
    }

    public String getExposeHeaders() {
        return exposeHeaders;
    }

    public void setExposeHeaders(final String exposeHeaders) {
        this.exposeHeaders = exposeHeaders;
    }

    public int getMaxAge() {
        return maxAge;
    }

    public void setMaxAge(final int maxAge) {
        this.maxAge = maxAge;
    }

    public Boolean getAllowCredentials() {
        return allowCredentials;
    }

    public void setAllowCredentials(final Boolean allowCredentials) {
        this.allowCredentials = allowCredentials;
    }

    public Set<HttpConstants.HTTP_METHOD> getAllowMethods() {
        return allowMethods;
    }

    public void setAllowMethods(final Set<HttpConstants.HTTP_METHOD> allowMethods) {
        this.allowMethods = allowMethods;
    }

    public String getAllowHeaders() {
        return allowHeaders;
    }

    public void setAllowHeaders(final String allowHeaders) {
        this.allowHeaders = allowHeaders;
    }

    @Override
    public String toString() {
        return CommonHelper.toNiceString(this.getClass(), "allowOrigin", allowOrigin, "exposeHeaders", exposeHeaders, "maxAge", maxAge,
                "allowCredentials", allowCredentials, "allowMethods", allowMethods, "allowHeaders", allowHeaders);
    }
}
