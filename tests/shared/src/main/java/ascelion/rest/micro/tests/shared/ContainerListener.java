
package ascelion.rest.micro.tests.shared;

import ascelion.rest.bridge.tests.api.API;

import com.github.tomakehurst.wiremock.WireMockServer;
import org.jboss.arquillian.container.spi.event.StartContainer;
import org.jboss.arquillian.container.spi.event.StopContainer;
import org.jboss.arquillian.core.api.annotation.Observes;

public class ContainerListener
{

	private WireMockServer wms;

	public void startContainer( @Observes StartContainer event ) throws Exception
	{
		final int port = API.reservePort();

		System.setProperty( "wiremock.server.port", Integer.toString( port ) );

		this.wms = new WireMockServer( port );

		this.wms.start();
	}

	public void stopContainer( @Observes StopContainer event ) throws Exception
	{
		this.wms.shutdownServer();
	}

}
