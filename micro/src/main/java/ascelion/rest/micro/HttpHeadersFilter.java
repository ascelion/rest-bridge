
package ascelion.rest.micro;

import java.io.IOException;

import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.container.ContainerResponseContext;
import javax.ws.rs.container.ContainerResponseFilter;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.ext.Provider;

@Provider
public class HttpHeadersFilter implements ContainerRequestFilter, ContainerResponseFilter
{

	private final ThreadLocalValue<HttpHeaders> tlv = (ThreadLocalValue<HttpHeaders>) ThreadLocalProxy.create( HttpHeaders.class );

	@Context
	private HttpHeaders headers;

	@Override
	public void filter( ContainerRequestContext reqCx ) throws IOException
	{
		this.tlv.set( this.headers );
	}

	@Override
	public void filter( ContainerRequestContext reqCx, ContainerResponseContext rspCx ) throws IOException
	{
		this.tlv.set( null );
	}

}
