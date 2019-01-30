
package ascelion.rest.bridge.client;

import javax.ws.rs.PathParam;

class PathParamAction
extends AnnotationAction<PathParam>
{

	PathParamAction( PathParam a, ActionParam p )
	{
		super( p, a );
	}

	@Override
	public RestRequest<?> execute( RestRequest<?> req )
	{
		final String val = this.param.converter.apply( this.param.currentValue( req ) );

		req.rc.path( this.annotation.value(), val );

		return req;
	}

}
