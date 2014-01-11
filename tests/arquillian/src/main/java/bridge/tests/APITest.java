
package bridge.tests;

import org.junit.Assert;
import org.junit.Test;

import ascelion.rest.bridge.web.BeanAPI;
import ascelion.rest.bridge.web.BeanValidData;

public class APITest
extends AbstractTestCase<BeanAPI>
{

	@Test
	public void create()
	{
		final BeanValidData b = new BeanValidData( "cici" );

		Assert.assertEquals( b, this.client.create( b ) );
	}

	@Test
	public void get()
	{
		this.client.get();
	}
}
