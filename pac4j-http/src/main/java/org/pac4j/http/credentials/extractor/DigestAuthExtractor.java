package org.pac4j.http.credentials.extractor;

import org.pac4j.core.context.HttpConstants;
import org.pac4j.core.context.WebContext;
import org.pac4j.core.context.session.SessionStore;
import org.pac4j.core.credentials.Credentials;
import org.pac4j.core.credentials.TokenCredentials;
import org.pac4j.core.credentials.extractor.CredentialsExtractor;
import org.pac4j.core.credentials.extractor.HeaderExtractor;
import org.pac4j.core.exception.CredentialsException;
import org.pac4j.core.util.CommonHelper;
import org.pac4j.core.util.Pac4jConstants;
import org.pac4j.http.credentials.DigestCredentials;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.StringTokenizer;

/**
 * To extract digest auth header.
 *
 * @author Mircea Carasel
 * @since 1.9.0
 */
public class DigestAuthExtractor implements CredentialsExtractor {

    private final HeaderExtractor extractor;

    public DigestAuthExtractor() {
        this(HttpConstants.AUTHORIZATION_HEADER, HttpConstants.DIGEST_HEADER_PREFIX);
    }

    public DigestAuthExtractor(final String headerName, final String prefixHeader) {
        this.extractor = new HeaderExtractor(headerName, prefixHeader);
    }

    /**
     * Extracts digest Authorization header components.
     * As per RFC 2617 :
     * username is the user's name in the specified realm
     * qop is quality of protection
     * uri is the request uri
     * response is the client response
     * nonce is a server-specified data string which should be uniquely generated
     *   each time a 401 response is made
     * cnonce is the client nonce
     * nc is the nonce count
     * If in the Authorization header it is not specified a username and response, we throw CredentialsException because
     * the client uses an username and a password to authenticate. response is just a MD5 encoded value
     * based on user provided password and RFC 2617 digest authentication encoding rules
     * @param context the current web context
     * @return the Digest credentials
     */
    @Override
    public Optional<Credentials> extract(final WebContext context, final SessionStore sessionStore) {
        final var credentials = this.extractor.extract(context, sessionStore);
        if (!credentials.isPresent()) {
            return Optional.empty();
        }

        var token = ((TokenCredentials) credentials.get()).getToken();
        var valueMap = parseTokenValue(token);
        var username = valueMap.get("username");
        var response = valueMap.get("response");

        if (CommonHelper.isBlank(username) || CommonHelper.isBlank(response)) {
            throw new CredentialsException("Bad format of the digest auth header");
        }
        var realm = valueMap.get("realm");
        var nonce = valueMap.get("nonce");
        var uri = valueMap.get("uri");
        var cnonce = valueMap.get("cnonce");
        var nc = valueMap.get("nc");
        var qop = valueMap.get("qop");
        var method = context.getRequestMethod();

        return Optional.of(new DigestCredentials(response, method, username, realm, nonce, uri, cnonce, nc, qop));
    }

    private Map<String, String> parseTokenValue(String token) {
        var tokenizer = new StringTokenizer(token, ", ");
        String keyval;
        final Map<String, String> map = new HashMap<>();
        while (tokenizer.hasMoreElements()) {
            keyval = tokenizer.nextToken();
            if (keyval.contains("=")) {
                var key = keyval.substring(0, keyval.indexOf("="));
                var value = keyval.substring(keyval.indexOf("=") + 1);
                map.put(key.trim(), value.replaceAll("\"", Pac4jConstants.EMPTY_STRING).trim());
            }
        }
        return map;
    }

    @Override
    public String toString() {
        return CommonHelper.toNiceString(this.getClass(), "extractor", extractor);
    }
}
