
package bridge.tests;

import org.junit.Assert;
import org.junit.Test;

import ascelion.rest.bridge.web.BeanAPI;
import ascelion.rest.bridge.web.BeanData;

public class APITest
extends AbstractTestCase<BeanAPI>
{

	@Test
	public void create()
	{
		final BeanData b = new BeanData( "cici" );

		Assert.assertEquals( b, this.client.create( b ) );
	}

	@Test
	public void get()
	{
		this.client.get();
	}
}
