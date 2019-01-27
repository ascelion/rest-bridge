
package ascelion.rest.bridge.client;

import javax.ws.rs.Produces;

final class INTProduces extends INTAnnotBase<Produces>
{

	INTProduces( Produces annotation )
	{
		super( annotation );
	}

	@Override
	protected void before( RestRequestContext rc )
	{
		rc.produces( this.annotation.value() );
	}

	@Override
	public int priority()
	{
		return PRIORITY_PARAMETERS - 2;
	}
}
