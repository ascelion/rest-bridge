
package bridge.tests.providers;

import java.util.Arrays;
import java.util.List;

import org.junit.rules.MethodRule;
import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.Statement;

import bridge.tests.AbstractTestCase;
import bridge.tests.ClientProvider;

public class IgnoreWithRule
implements MethodRule
{

	@Override
	public Statement apply( Statement base, FrameworkMethod method, Object target )
	{
		IgnoreWith a = method.getAnnotation( IgnoreWith.class );

		if( a != null ) {
			return wrapStatement( base, a );
		}

		a = method.getMethod().getDeclaringClass().getAnnotation( IgnoreWith.class );

		if( a != null ) {
			return wrapStatement( base, a );
		}

		return base;
	}

	private Statement wrapStatement( Statement base, IgnoreWith annotation )
	{
		final List<Class<? extends ClientProvider>> clients = Arrays.asList( annotation.value() );

		return new Statement()
		{

			@Override
			public void evaluate()
			throws Throwable
			{
				final Class cls = AbstractTestCase.providerClass();

				if( clients.contains( cls ) ) {
					System.err.println( "IGNORED test on " + cls );
				}
				else {
					base.evaluate();
				}
			}
		};
	}
}
