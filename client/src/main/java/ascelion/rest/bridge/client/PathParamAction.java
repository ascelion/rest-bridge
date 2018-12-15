
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
	public void execute( RestRequest req )
	{
		final String val = this.param.converter.apply( this.param.currentValue( req ) );

		req.path( this.annotation.value(), val );
	}

}
