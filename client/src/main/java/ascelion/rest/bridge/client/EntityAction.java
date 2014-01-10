package ascelion.rest.bridge.client;

import java.lang.reflect.Type;

class EntityAction
extends Action
{

	final Type entityType;

	EntityAction( Type entityType, int ix )
	{
		super( ix );

		this.entityType = entityType;
	}

	@Override
	public void execute( RestContext cx )
	{
		cx.entity = cx.parameterValue;
		cx.entityPresent = true;
	}
}

