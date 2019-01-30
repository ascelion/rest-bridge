
package ascelion.rest.micro.tests;

import javax.ws.rs.GET;
import javax.ws.rs.Path;

import org.eclipse.microprofile.rest.client.annotation.RegisterProvider;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;
import org.eclipse.microprofile.rest.client.tck.interfaces.Loggable;
import org.eclipse.microprofile.rest.client.tck.providers.ReturnWithURLRequestFilter;

@Path( "" )
@RegisterProvider( ReturnWithURLRequestFilter.class )
@RegisterRestClient( baseUri = ClientWithTwoInterceptors.URI )
public interface ClientWithTwoInterceptors
{

	String URI = "http://localhost:3141/";

	@GET
	@Loggable
	String getLoggable();

	@GET
	@Secured
	String getSecured();

	@GET
	@Loggable
	@Secured( "with-value" )
	String getTwoWithValue();

	@GET
	@BothInterceptors
	String getWithBoth();

	@GET
	@BothInterceptors
	@Loggable
	@Secured( "all" )
	String getWithAll();

	@GET
	String getNoInterceptor();

}
