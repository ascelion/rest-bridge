
package ascelion.rest.bridge.tests.app;

import javax.inject.Inject;

import ascelion.rest.bridge.tests.api.BeanAPI;
import ascelion.rest.bridge.tests.api.BeanData;
import ascelion.rest.bridge.tests.api.Hello;

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
