package org.pac4j.core.matching.matcher;

import org.junit.Test;
import org.pac4j.core.context.MockWebContext;
import org.pac4j.core.context.session.MockSessionStore;
import org.pac4j.core.exception.TechnicalException;
import org.pac4j.core.util.TestsHelper;

import java.util.HashSet;
import java.util.Set;
import java.util.regex.PatternSyntaxException;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * Tests {@link PathMatcher}.
 *
 * @author Rob Ward
 * @since 2.0.0
 */
public class PathMatcherTests {

    @Test
    public void testBlankPath() {
        final var pathMatcher = new PathMatcher();
        assertTrue(pathMatcher.matches(MockWebContext.create().setPath("/page.html"), new MockSessionStore()));
        assertTrue(pathMatcher.matches(MockWebContext.create(), new MockSessionStore()));
    }

    @Test
    public void testFixedPath() {
        final var pathMatcher = new PathMatcher().excludePath("/foo");
        assertFalse(pathMatcher.matches(MockWebContext.create().setPath("/foo"), new MockSessionStore()));
        assertTrue(pathMatcher.matches(MockWebContext.create().setPath("/foo/bar"), new MockSessionStore()));
    }

    @Test
    public void testBranch() {
        final var pathMatcher = new PathMatcher().excludeBranch("/foo");
        assertFalse(pathMatcher.matches(MockWebContext.create().setPath("/foo"), new MockSessionStore()));
        assertFalse(pathMatcher.matches(MockWebContext.create().setPath("/foo/"), new MockSessionStore()));
        assertFalse(pathMatcher.matches(MockWebContext.create().setPath("/foo/bar"), new MockSessionStore()));
    }

    @Test
    public void testMissingStartCharacterInRegexp() {
        TestsHelper.expectException(() -> new PathMatcher().excludeRegex("/img/.*$"), TechnicalException.class,
                "Your regular expression: '/img/.*$' must start with a ^ and end with a $ to define a full path matching");
    }

    @Test
    public void testMissingEndCharacterInRegexp() {
        TestsHelper.expectException(() -> new PathMatcher().excludeRegex("^/img/.*"), TechnicalException.class,
            "Your regular expression: '^/img/.*' must start with a ^ and end with a $ to define a full path matching");
    }

    @Test(expected = PatternSyntaxException.class)
    public void testBadRegexp() {
        new PathMatcher().excludeRegex("^/img/**$");
    }

    @Test
    public void testNoPath() {
        final var pathMatcher = new PathMatcher().excludeRegex("^/$");
        assertFalse(pathMatcher.matches(MockWebContext.create().setPath("/"), new MockSessionStore()));
    }

    @Test
    public void testMatch() {
        final var matcher = new PathMatcher().excludeRegex("^/(img/.*|css/.*|page\\.html)$");
        assertTrue(matcher.matches(MockWebContext.create().setPath("/js/app.js"), new MockSessionStore()));
        assertTrue(matcher.matches(MockWebContext.create().setPath("/"), new MockSessionStore()));
        assertTrue(matcher.matches(MockWebContext.create().setPath("/page.htm"), new MockSessionStore()));
    }

    @Test
    public void testDontMatch() {
        final var matcher = new PathMatcher().excludeRegex("^/(img/.*|css/.*|page\\.html)$");
        assertFalse(matcher.matches(MockWebContext.create().setPath("/css/app.css"), new MockSessionStore()));
        assertFalse(matcher.matches(MockWebContext.create().setPath("/img/"), new MockSessionStore()));
        assertFalse(matcher.matches(MockWebContext.create().setPath("/page.html"), new MockSessionStore()));
    }

    @Test
    public void testSetters() {
        final Set<String> excludedPaths = new HashSet<>();
        excludedPaths.add("/foo");
        final Set<String> excludedRegexs = new HashSet<>();
        excludedRegexs.add("^/(img/.*|css/.*|page\\.html)$");

        final var matcher = new PathMatcher();
        matcher.setExcludedPaths(excludedPaths);
        matcher.setExcludedPatterns(excludedRegexs);

        assertFalse(matcher.matches(MockWebContext.create().setPath("/foo"), new MockSessionStore()));
        assertTrue(matcher.matches(MockWebContext.create().setPath("/foo/"), null)); // because its a fixed path, not a regex

        assertTrue(matcher.matches(MockWebContext.create().setPath("/error/500.html"), new MockSessionStore()));
        assertFalse(matcher.matches(MockWebContext.create().setPath("/img/"), new MockSessionStore()));
    }

    @Test
    public void testIncludePath() {
        final var matcher = new PathMatcher().includePath("/protect");

        assertTrue(matcher.matches(MockWebContext.create().setPath("/protect"), new MockSessionStore()));
        assertTrue(matcher.matches(MockWebContext.create().setPath("/protected"), new MockSessionStore()));
        assertTrue(matcher.matches(MockWebContext.create().setPath("/protected/index.html"), new MockSessionStore()));
        assertFalse(matcher.matches(MockWebContext.create().setPath("/img/logo.gif"), new MockSessionStore()));
        assertFalse(matcher.matches(MockWebContext.create().setPath("/callback"), new MockSessionStore()));
    }

    @Test
    public void testIncludePaths() {
        final var matcher = new PathMatcher().includePaths("/protect", "/css");

        assertTrue(matcher.matches(MockWebContext.create().setPath("/protect"), new MockSessionStore()));
        assertTrue(matcher.matches(MockWebContext.create().setPath("/protected"), new MockSessionStore()));
        assertTrue(matcher.matches(MockWebContext.create().setPath("/protected/index.html"), new MockSessionStore()));
        assertTrue(matcher.matches(MockWebContext.create().setPath("/css/css1.css"), new MockSessionStore()));

        assertFalse(matcher.matches(MockWebContext.create().setPath("/img/logo.gif"), new MockSessionStore()));
        assertFalse(matcher.matches(MockWebContext.create().setPath("/callback"), new MockSessionStore()));
        assertFalse(matcher.matches(MockWebContext.create().setPath("/notprotected"), new MockSessionStore()));
    }
}
