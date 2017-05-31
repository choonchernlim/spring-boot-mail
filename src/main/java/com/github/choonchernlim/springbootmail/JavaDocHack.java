package com.github.choonchernlim.springbootmail;

/**
 * This is a temporary hack to allow `maven-javadoc-plugin` to generate a `*-javadoc.jar` artifact
 * to satisfy OSSRH rule in order to successfully perform a `mvn deploy`.
 * <p>
 * The reason is because the source code are all written in Groovy, hence `maven-javadoc-plugin` will not
 * generate `*-javadoc.jar` because no Java files exist.
 */
@SuppressWarnings("unused")
public final class JavaDocHack {
}
