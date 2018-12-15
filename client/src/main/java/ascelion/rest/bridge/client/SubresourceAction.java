
package ascelion.rest.bridge.client;

class SubresourceAction
extends Action
{

	final Class resourceType;

	SubresourceAction( Class resourceType, ActionParam param )
	{
		super( param );

		if( !resourceType.isInterface() ) {
			throw new IllegalArgumentException( "Return type not an interface" );
		}

		this.resourceType = resourceType;
	}

	@Override
	void execute( RestRequest cx )
	{
		throw new UnsupportedOperationException( "TODO" );
//		final RestClientIH ih = new RestClientIH( cx.client, cx.target, this.resourceType, cx.headers, cx.cookies, new Form() );
//
//		cx.result = ih.newProxy();
	}
}
