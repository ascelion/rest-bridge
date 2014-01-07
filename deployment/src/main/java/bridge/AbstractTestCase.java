
package bridge;

import java.io.File;
import java.net.URL;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.jboss.shrinkwrap.resolver.api.maven.ConfigurableMavenResolverSystem;
import org.jboss.shrinkwrap.resolver.api.maven.Maven;
import org.jboss.shrinkwrap.resolver.api.maven.PomEquippedResolveStage;
import org.jboss.shrinkwrap.resolver.api.maven.ScopeType;
import org.junit.Before;
import org.junit.runner.RunWith;

import ascelion.rest.bridge.client.RestClient;
import ascelion.rest.bridge.web.RestApplication;

@RunWith( Arquillian.class )
public abstract class AbstractTestCase
{

	@Deployment( testable = false )
	static public WebArchive createDeployment()
	{
		final WebArchive web = ShrinkWrap.create( WebArchive.class );

		final ConfigurableMavenResolverSystem mvn = Maven
			.configureResolver()
			.workOffline()
			.withClassPathResolution( true );

		PomEquippedResolveStage stage;

		try {
			stage = mvn.configureViaPlugin();
		}
		catch( final Exception e ) {
			stage = mvn.loadPomFromFile( "pom.xml" );
		}

		final File[] deps = stage.importDependencies( ScopeType.COMPILE, ScopeType.RUNTIME )
			.resolve().withTransitivity().asFile();

		web.addAsLibraries( deps );

		return web;
	}

	@ArquillianResource
	protected URL target;

	protected RestClient client;

	@Before
	public void setUp()
	{
		this.client = new RestClient( RestApplication.BASE );
	}
}
