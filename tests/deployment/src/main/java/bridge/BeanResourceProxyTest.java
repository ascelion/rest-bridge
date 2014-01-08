
package bridge;

import org.junit.Ignore;
import org.junit.Test;

public class BeanResourceProxyTest
extends BeanResourceTestBase<ProxyProvider>
{

	@Override
	@Test
	@Ignore( "Not supported by jersey-proxy" )
	public void get()
	{
	}
}
