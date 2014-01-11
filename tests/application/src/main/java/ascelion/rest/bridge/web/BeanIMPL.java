
package ascelion.rest.bridge.web;

import javax.inject.Inject;

public class BeanIMPL
extends IMPL<BeanValidData>
implements BeanAPI
{

	@Inject
	private Hello hello;

	@Override
	public BeanValidData get()
	{
		if( this.hello == null ) {
			throw new IllegalStateException();
		}

		return new BeanValidData( "value" );
	}
}
