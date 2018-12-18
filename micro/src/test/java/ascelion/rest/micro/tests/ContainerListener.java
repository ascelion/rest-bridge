
package ascelion.rest.micro.tests;

import com.github.tomakehurst.wiremock.WireMockServer;
import org.jboss.arquillian.container.spi.event.StartContainer;
import org.jboss.arquillian.container.spi.event.StopContainer;
import org.jboss.arquillian.core.api.annotation.Observes;

public class ContainerListener
{

	private WireMockServer wms;

	public void startContainer( @Observes StartContainer event ) throws Exception
	{
		this.wms = new WireMockServer( 8765 );

		this.wms.start();
	}

	public void stopContainer( @Observes StopContainer event ) throws Exception
	{
		this.wms.shutdownServer();
	}

}
