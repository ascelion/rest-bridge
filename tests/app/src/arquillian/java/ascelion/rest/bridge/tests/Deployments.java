
package ascelion.rest.bridge.tests;

import java.io.IOException;
import java.net.URI;
import java.util.concurrent.TimeUnit;

import ascelion.rest.bridge.tests.arquillian.ArquillianUnit;
import ascelion.rest.micro.tests.shared.SLF4JHandler;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.gradle.archive.importer.embedded.EmbeddedGradleImporter;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Rule;
import org.junit.rules.Timeout;
import org.junit.runner.RunWith;

@RunWith( ArquillianUnit.class )
public abstract class Deployments
{

	static {
		SLF4JHandler.install();
	}

	@Deployment( testable = false )
	static public Archive<?> createDeployment() throws IOException
	{
		final WebArchive web = ShrinkWrap.create( EmbeddedGradleImporter.class )
			.forThisProjectDirectory()
			.forTasks( "war" )
			.importBuildOutput( "build/libs/app.war" )
			.as( WebArchive.class );

		return web;
	}

	@Rule
	public final Timeout timeout = new Timeout( 15, TimeUnit.SECONDS );

	@ArquillianResource
	protected URI target;

}
