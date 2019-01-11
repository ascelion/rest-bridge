
package ascelion.rest.micro.tests.shared;

import ascelion.rest.bridge.tests.api.SLF4JHandler;

import ch.qos.logback.core.CoreConstants;
import org.jboss.arquillian.container.test.spi.client.deployment.ApplicationArchiveProcessor;
import org.jboss.arquillian.core.spi.LoadableExtension;

public class TestsExtension implements LoadableExtension
{

	static {
		System.setProperty( CoreConstants.DISABLE_SERVLET_CONTAINER_INITIALIZER_KEY, "true" );
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
