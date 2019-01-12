
package ascelion.rest.bridge.tests;

import java.lang.reflect.TypeVariable;

import io.leangen.geantyref.GenericTypeReflector;
import org.junit.After;
import org.junit.Before;

public abstract class AbstractTestCase<T>
extends Deployments
{

	static private final TypeVariable INTEFACE_TYPE = AbstractTestCase.class.getTypeParameters()[0];

	protected T client;

	@Before
	public void setUp() throws Exception
	{
		this.client = TestClientProvider.getInstance().createClient( this.target, interfaceType() );
	}

	@After
	public void tearDown()
	{
		TestClientProvider.getInstance().release( this.client );
	}

	protected final Class<T> interfaceType()
	{
		return (Class) GenericTypeReflector.getTypeParameter( getClass(), INTEFACE_TYPE );
	}
}
