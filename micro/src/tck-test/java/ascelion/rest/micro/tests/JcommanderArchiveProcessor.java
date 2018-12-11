
package ascelion.rest.micro.tests;

import java.io.File;
import java.net.URL;

import com.beust.jcommander.JCommander;
import org.jboss.arquillian.container.test.spi.client.deployment.ApplicationArchiveProcessor;
import org.jboss.arquillian.test.spi.TestClass;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.importer.ZipImporter;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.jboss.shrinkwrap.api.spec.WebArchive;

public class JcommanderArchiveProcessor implements ApplicationArchiveProcessor
{

	@Override
	public void process( Archive<?> archive, TestClass testClass )
	{
		final URL res = getClass().getResource( "/" + JCommander.class.getName().replace( '.', '/' ) + ".class" );

		if( res == null || !"jar".equals( res.getProtocol() ) ) {
			return;
		}

		final String file = res.getPath().substring( 5 )
			.replaceAll( "\\!.+", "" );

		final JavaArchive packed = ShrinkWrap
			.create( ZipImporter.class, "jcommander.jar" )
			.importFrom( new File( file ) )
			.as( JavaArchive.class );

		archive.as( WebArchive.class ).addAsLibrary( packed );
	}
}
