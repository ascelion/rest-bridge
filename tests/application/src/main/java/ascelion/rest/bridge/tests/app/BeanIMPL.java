
package ascelion.rest.bridge.tests.app;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import ascelion.rest.bridge.tests.api.BeanAPI;
import ascelion.rest.bridge.tests.api.BeanData;
import ascelion.rest.bridge.tests.api.Hello;

@ApplicationScoped
public class BeanIMPL
extends IMPL<BeanData>
implements BeanAPI
{

	@Inject
	private Hello hello;

	@Override
	public BeanData get()
	{
		this.hello.sayByParam( "guest" );

		return new BeanData( "value" );
	}
}
