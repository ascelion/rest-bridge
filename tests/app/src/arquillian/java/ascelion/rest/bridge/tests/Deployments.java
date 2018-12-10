
package ascelion.rest.bridge.tests;

import java.io.File;
import java.io.IOException;
import java.net.URI;

import ascelion.rest.bridge.tests.arquillian.ArquillianUnit;

import org.apache.commons.io.FileUtils;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.exporter.ExplodedExporter;
import org.jboss.shrinkwrap.api.gradle.archive.importer.embedded.EmbeddedGradleImporter;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.runner.RunWith;

@RunWith( ArquillianUnit.class )
public abstract class Deployments
{

	static {
		SLF4JHandler.install();
	}

	@Deployment( testable = false )
	static public WebArchive createDeployment() throws IOException
	{
		final WebArchive web = ShrinkWrap.create( EmbeddedGradleImporter.class )
			.forThisProjectDirectory()
			.forTasks( "war" )
			.importBuildOutput()
			.as( WebArchive.class );

		web.addAsWebInfResource( Deployments.class.getResource( "/beans.xml" ), "beans.xml" );

		final File output = new File( new File( "build" ), "exploded" );

		output.mkdirs();

		FileUtils.cleanDirectory( output );

		web.as( ExplodedExporter.class ).exportExploded( output );

		return web;
	}

	@ArquillianResource
	protected URI target;

}
