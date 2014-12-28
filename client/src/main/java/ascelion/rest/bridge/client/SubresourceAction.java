
package ascelion.rest.bridge.client;

class SubresourceAction
extends Action
{

	final Class resourceType;

	SubresourceAction( Class resourceType )
	{
		super( 0 );

		if( !resourceType.isInterface() ) {
			throw new IllegalArgumentException( "Return type not an interface" );
		}

		this.resourceType = resourceType;
	}

	@Override
	void execute( RestContext cx )
	{
		final RestClientIH ih = new RestClientIH( this.resourceType, cx.target, cx.onBuildRequest, cx.headers, cx.cookies, cx.form );

		cx.result = RestClientIH.newProxy( this.resourceType, ih );
	}
}
