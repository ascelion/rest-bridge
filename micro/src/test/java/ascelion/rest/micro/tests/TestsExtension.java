
package ascelion.rest.micro.tests;

import org.jboss.arquillian.container.test.spi.client.deployment.ApplicationArchiveProcessor;
import org.jboss.arquillian.core.spi.LoadableExtension;

public class TestsExtension implements LoadableExtension
{

	@Override
	public void register( ExtensionBuilder bld )
	{
		bld.service( ApplicationArchiveProcessor.class, WiremockArchiveProcessor.class );
		bld.service( ApplicationArchiveProcessor.class, JcommanderArchiveProcessor.class );
	}

}
