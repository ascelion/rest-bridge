
package ascelion.rest.bridge.client;

import javax.ws.rs.Consumes;

final class INTConsumes extends INTAnnotBase<Consumes>
{

	INTConsumes( Consumes annotation )
	{
		super( annotation );
	}

	@Override
	protected void before( RestRequestContext rc )
	{
		rc.consumes( this.annotation.value() );
	}

}
