
package bridge.tests;

import org.junit.Test;

import ascelion.rest.bridge.web.Methods;

public class MethodsTest
extends AbstractTestCase<Methods>
{

	@Test
	public void delete()
	{
		this.client.delete();
	}

	@Test
	public void get()
	{
		this.client.get();
	}

	@Test
	public void head()
	{
		this.client.head();
	}

	@Test
	public void options()
	{
		this.client.options();
	}

	@Test
	public void post()
	{
		this.client.post();
	}

	@Test
	public void put()
	{
		this.client.put( "gigel" );
	}
}
