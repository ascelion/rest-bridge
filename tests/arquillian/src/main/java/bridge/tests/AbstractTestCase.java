
package bridge.tests;

import java.lang.reflect.TypeVariable;
import java.net.URI;
import java.util.Iterator;
import java.util.ServiceLoader;

import org.jboss.arquillian.test.api.ArquillianResource;
import org.junit.Before;

import bridge.tests.providers.RestBridgeProvider;

import com.googlecode.gentyref.GenericTypeReflector;

public abstract class AbstractTestCase<T>
extends Deployments
{

	static private TestClientProvider initClientProvider()
	{
		final Iterator<TestClientProvider> providers = ServiceLoader.load( TestClientProvider.class ).iterator();

		if( providers.hasNext() ) {
			return providers.next();
		}

		return new RestBridgeProvider();
	}

	static public final TestClientProvider CLIENT_PROVIDER = initClientProvider();

	static private final TypeVariable INTEFACE_TYPE = AbstractTestCase.class.getTypeParameters()[0];

	@ArquillianResource
	protected URI target;

	protected T client;

	@Before
	public void setUp()
	throws Exception
	{
		final Class<T> clientClass = (Class) GenericTypeReflector.getTypeParameter( getClass(), INTEFACE_TYPE );

		this.client = CLIENT_PROVIDER.createClient( this.target, clientClass );
	}
}
