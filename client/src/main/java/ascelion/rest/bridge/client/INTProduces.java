
package ascelion.rest.bridge.client;

import javax.ws.rs.Produces;

final class INTProduces extends INTAnnotBase<Produces>
{

	INTProduces( Produces annotation )
	{
		super( annotation );
	}

	@Override
	void before( RestRequestContextImpl rc )
	{
		rc.produces( this.annotation.value() );
	}

}
