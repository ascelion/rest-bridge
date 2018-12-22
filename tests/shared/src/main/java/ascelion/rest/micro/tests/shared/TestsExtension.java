
package ascelion.rest.micro.tests.shared;

import org.jboss.arquillian.container.test.spi.client.deployment.ApplicationArchiveProcessor;
import org.jboss.arquillian.core.spi.LoadableExtension;

public class TestsExtension implements LoadableExtension
{

	static {
		SLF4JHandler.install();
	}

	@Override
	public void register( ExtensionBuilder bld )
	{
		bld.service( ApplicationArchiveProcessor.class, WiremockArchiveProcessor.class );
		bld.service( ApplicationArchiveProcessor.class, JcommanderArchiveProcessor.class );

		bld.observer( ContainerListener.class );
	}

}
