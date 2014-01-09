
package bridge.tests;

import java.lang.reflect.TypeVariable;
import java.net.URI;

import org.jboss.arquillian.test.api.ArquillianResource;
import org.junit.Before;
import org.junit.Rule;
import org.junit.rules.MethodRule;

import bridge.tests.providers.IgnoreWithRule;

import com.googlecode.gentyref.GenericTypeReflector;

public abstract class AbstractTestCase<T>
extends Deployments
{

	static public Class providerClass()
	throws ClassNotFoundException
	{
		final String clientProviderName = System.getProperty( ClientProvider.class.getName(),
			"bridge.tests.providers.RestBridgeProvider" );

		return Thread.currentThread().getContextClassLoader().loadClass( clientProviderName );
	}

	static private final TypeVariable INTEFACE_TYPE = AbstractTestCase.class.getTypeParameters()[0];

	@Rule
	public MethodRule ignoreRule = new IgnoreWithRule();

	@ArquillianResource
	protected URI target;

	protected T client;

	@Before
	public void setUp()
	throws Exception
	{
		final Class<T> clientClass = (Class) GenericTypeReflector.getTypeParameter( getClass(), INTEFACE_TYPE );
		final Class<? extends ClientProvider> providerClass = providerClass();

		this.client = providerClass.newInstance().createClient( this.target, clientClass );
	}
}
