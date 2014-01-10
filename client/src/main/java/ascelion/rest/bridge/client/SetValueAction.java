package ascelion.rest.bridge.client;

import ascelion.rest.bridge.client.Action.Priority;

class SetValueAction
extends Action
{

	private Object value;

	SetValueAction( int ix )
	{
		super( ix, Priority.SET_VALUE );
	}

	@Override
	public void evaluate( Object[] arguments )
	{
		this.value = arguments[this.ix];
	}

	@Override
	public void execute( RestContext cx )
	{
		cx.parameterValue = this.value;
	}
}

