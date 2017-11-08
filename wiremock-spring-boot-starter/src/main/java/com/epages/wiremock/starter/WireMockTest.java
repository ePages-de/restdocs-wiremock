package com.epages.wiremock.starter;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import com.github.tomakehurst.wiremock.WireMockServer;


/**
 * Test class annotation enabling a WireMock server in a spring integration
 * test.
 * 
 * As a result the following actions are performed.
 * 
 * <li>A bean of type {@link WireMockServer} gets put into the application
 * context.
 * <li>The WireMock server gets started once before running your test class.
 * <li>All feign/ribbon-based services get pre-configured to use the WireMock
 * server.
 * <li>A new <tt>wiremock.port</tt> property is set to the port WireMock is
 * running on.
 * 
 * @see WireMockServer
 * 
 */
@Retention(RetentionPolicy.RUNTIME)
@Documented
//@Import({ WireMockAutoConfiguration.class })
@Target({ ElementType.TYPE, ElementType.METHOD })
public @interface WireMockTest {

	/**
	 * List the names of your ribbon/feign based services. These will be
	 * auto-configured to use the WireMock server as endpoint.
	 */
	String[] ribbonServices() default {};

	/**
	 * Set this property to the root folder of your JSON stubs. This folder gets
	 * looked up on the classpath and can be overwritten on a per-test basis, by
	 * repeating the {@link WireMockTest} annotation on method level.
	 */
	String stubPath() default "";

	/**
	 * Sets WireMock to a fixed port. By default WireMock is started on a dynamic port.
	 */
	int port() default 0;

	/**
	 * Should wiremock run in record mode instead of serving stubs
	 * @return
	 */
	boolean record() default false;

	/**
	 * The target url for recording
	 * Mandatory in record mode
	 * @return
	 */
	String targetBaseUrl() default "";
}
