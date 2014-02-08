
package ascelion.rest.bridge.web;

import javax.inject.Inject;

public class BeanIMPL
extends IMPL<BeanData>
implements BeanAPI
{

	@Inject
	private Hello hello;

	@Override
	public BeanData get()
	{
		if( this.hello == null ) {
			throw new IllegalStateException();
		}

		return new BeanData( "value" );
	}
}
