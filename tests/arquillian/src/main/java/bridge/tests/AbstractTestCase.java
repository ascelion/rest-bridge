
package bridge.tests;

import java.lang.reflect.TypeVariable;
import java.net.URI;

import org.jboss.arquillian.test.api.ArquillianResource;
import org.junit.Before;

import com.googlecode.gentyref.GenericTypeReflector;

public abstract class AbstractTestCase<T, P extends ClientProvider>
extends Deployments
{

	static private final TypeVariable PROVIDER_TYPE = AbstractTestCase.class.getTypeParameters()[1];

	static private final TypeVariable INTEFACE_TYPE = AbstractTestCase.class.getTypeParameters()[0];

	@ArquillianResource
	protected URI target;

	protected T client;

	@Before
	public void setUp()
	throws Exception
	{
		final Class<P> providerClass = providerClass();
		final Class<T> clientClass = (Class) GenericTypeReflector.getTypeParameter( getClass(), INTEFACE_TYPE );

		this.client = providerClass.newInstance().createClient( this.target, clientClass );
	}

	protected Class<P> providerClass()
	{
		return (Class) GenericTypeReflector.getTypeParameter( getClass(), PROVIDER_TYPE );
	}

}
