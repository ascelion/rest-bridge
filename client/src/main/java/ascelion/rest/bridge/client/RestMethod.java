
package ascelion.rest.bridge.client;

import ascelion.utils.chain.InterceptorChain;

import static java.lang.String.format;

final class RestMethod
{

	private final RestClientInternals rci;
	private final RestMethodInfo rmi;
	final InterceptorChain<RestRequestContext> chain = new InterceptorChain<>();

	RestMethod( RestMethodInfo rmi, RestClientInternals rci )
	{
		this.rci = rci;
		this.rmi = rmi;

		this.rci.rriFactories().forEach( f -> f.create( this.rmi )
			.forEach( this.chain::add ) );

		if( rmi.getHttpMethod() == null ) {
//			resource methods that have a @Path annotation,
//			but no HTTP method are considered sub-resource locators.
			this.chain.add( new INVSubResource( rci, rmi.getReturnType().getRawType() ) );
		}
		else {
			this.chain.add( INVResource.INSTANCE );
		}
	}

	@Override
	public String toString()
	{
		return format( "%s: %s %s", this.rmi.getJavaMethod().getName(), this.rmi.getHttpMethod(), this.rmi.getMethodURI() );
	}

	Object request( Object proxy, Object... arguments ) throws Exception
	{
		final RestRequestContext rc = new RestRequestContext( this.rmi, proxy, arguments );

		return this.chain.around( rc, () -> {
			throw new AssertionError( "unreachable code?" );
		} );
	}

}
