package ascelion.rest.bridge.client;

import javax.ws.rs.DefaultValue;

import ascelion.rest.bridge.client.Action.Priority;

class DefaultValueAction
extends AnnotationAction<DefaultValue>
{

	public DefaultValueAction( DefaultValue annotation, int ix )
	{
		super( annotation, ix, Priority.DEFAULT_VALUE );
	}

	@Override
	public void execute( RestContext cx )
	{
		if( cx.parameterValue == null ) {
			cx.parameterValue = this.annotation.value();
		}
	}

}

