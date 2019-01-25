
package ascelion.rest.bridge.client;

import java.lang.reflect.Type;

class EntityAction
extends Action
{

	final Type entityType;

	EntityAction( ActionParam p, Type entityType )
	{
		super( p );

		this.entityType = entityType;
	}

	@Override
	public RestRequest<?> execute( RestRequest<?> req )
	{
		req.rc.entity( this.param.currentValue( req ) );

		return req;
	}
}
