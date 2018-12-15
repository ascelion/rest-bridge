
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
	public void execute( RestRequest cx )
	{
		cx.entity = this.param.currentValue( cx );
	}
}
