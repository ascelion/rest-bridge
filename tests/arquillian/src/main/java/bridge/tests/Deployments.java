
package bridge.tests;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.exporter.ExplodedExporter;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.jboss.shrinkwrap.resolver.api.maven.ConfigurableMavenResolverSystem;
import org.jboss.shrinkwrap.resolver.api.maven.Maven;
import org.jboss.shrinkwrap.resolver.api.maven.PomEquippedResolveStage;
import org.jboss.shrinkwrap.resolver.api.maven.ScopeType;
import org.junit.runner.RunWith;

import bridge.tests.arquillian.ArquillianUnit;

@RunWith( ArquillianUnit.class )
public abstract class Deployments
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
			final String profiles = System.getProperty( "profiles", "" );

			if( profiles.isEmpty() ) {
				stage = mvn.loadPomFromFile( "pom.xml" );
			}
			else {
				stage = mvn.loadPomFromFile( "pom.xml", profiles.split( "," ) );
			}
		}

		final File[] deps = stage.importDependencies( ScopeType.COMPILE, ScopeType.RUNTIME )
			.resolve().withTransitivity().asFile();

		web.addAsLibraries( deps );
		web.addAsWebInfResource( Deployments.class.getResource( "/META-INF/beans.xml" ), "beans.xml" );

		final File output = new File( new File( "target" ), "exploded" );

		output.mkdirs();
		FileUtils.cleanDirectory( output );

		web.as( ExplodedExporter.class ).exportExploded( output );

		return web;
	}

}
