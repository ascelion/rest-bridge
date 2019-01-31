
package ascelion.rest.bridge.tests;

import java.io.IOException;
import java.net.URI;
import java.util.concurrent.TimeUnit;

import ascelion.rest.bridge.tests.api.SLF4JHandler;
import ascelion.rest.bridge.tests.app.RestApplication;
import ascelion.rest.bridge.tests.app.RestFeature;
import ascelion.rest.bridge.tests.arquillian.ArquillianUnit;

import ch.qos.logback.core.CoreConstants;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Before;
import org.junit.Rule;
import org.junit.rules.Timeout;
import org.junit.runner.RunWith;

@RunWith( ArquillianUnit.class )
public abstract class Deployments
{

	static {
		System.setProperty( CoreConstants.DISABLE_SERVLET_CONTAINER_INITIALIZER_KEY, "true" );
		SLF4JHandler.install();
	}

	@Deployment( testable = false )
	static public Archive<?> createDeployment() throws IOException
	{
		final WebArchive web = ShrinkWrap.create( WebArchive.class )
			.addClasses( RestApplication.class, RestFeature.class )
			.addAsWebInfResource( EmptyAsset.INSTANCE, "beans.xml" );

		return web;
	}

	@Rule
	public final Timeout timeout = System.getenv().containsKey( "ECLIPSE_VERSION" )
		? null
		: new Timeout( 15, TimeUnit.SECONDS );

	@ArquillianResource
	protected URI target;

	@Before
	public final void setUpProvider()
	{
		TestClientProvider.getInstance().reset();
	}
}
