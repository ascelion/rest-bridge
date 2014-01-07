
package bridge;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.TypeVariable;
import java.net.URI;

import org.apache.commons.io.FileUtils;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.exporter.ExplodedExporter;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.jboss.shrinkwrap.resolver.api.maven.ConfigurableMavenResolverSystem;
import org.jboss.shrinkwrap.resolver.api.maven.Maven;
import org.jboss.shrinkwrap.resolver.api.maven.PomEquippedResolveStage;
import org.jboss.shrinkwrap.resolver.api.maven.ScopeType;
import org.junit.Before;
import org.junit.runner.RunWith;

import com.googlecode.gentyref.GenericTypeReflector;

@RunWith( Arquillian.class )
public abstract class AbstractTestCase<T, P extends ClientProvider>
{

	@Deployment( testable = false )
	static public WebArchive createDeployment()
	throws IOException
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

		final File output = new File( new File( "target" ), "exploded" );

		output.mkdirs();
		FileUtils.cleanDirectory( output );

		web.as( ExplodedExporter.class ).exportExploded( output );

		return web;
	}

	static private final TypeVariable PROVIDER_TYPE = AbstractTestCase.class.getTypeParameters()[1];

	static private final TypeVariable INTEFACE_TYPE = AbstractTestCase.class.getTypeParameters()[0];

	@ArquillianResource
	protected URI target;

	protected T client;

	@Before
	public void setUp()
	throws Exception
	{
		final Class<P> providerClass = providerClass();
		final Class<T> clientClass = (Class) GenericTypeReflector.getTypeParameter( getClass(), INTEFACE_TYPE );

		this.client = providerClass.newInstance().createClient( this.target, clientClass );
	}

	protected Class<P> providerClass()
	{
		return (Class) GenericTypeReflector.getTypeParameter( getClass(), PROVIDER_TYPE );
	}

}
