
package bridge.tests.arquillian;

import java.util.Arrays;
import java.util.List;

import org.jboss.arquillian.junit.Arquillian;
import org.junit.runner.notification.RunNotifier;
import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.InitializationError;

import bridge.tests.AbstractTestCase;
import bridge.tests.TestClientProvider;

public class ArquillianUnit
extends Arquillian
{

	static {
		Arrays.asList( System.getProperty( "java.class.path", "" ).split( ":" ) ).forEach( System.out::println );
	}

	public ArquillianUnit( Class<?> klass )
	throws InitializationError
	{
		super( klass );
	}

	@Override
	protected void runChild( FrameworkMethod method, RunNotifier notifier )
	{
		if( isIgnored( method ) ) {
			notifier.fireTestIgnored( describeChild( method ) );
		}
		else {
			super.runChild( method, notifier );
		}
	}

	private boolean isIgnored( FrameworkMethod method )
	{
		IgnoreWith a = method.getAnnotation( IgnoreWith.class );

		if( a == null ) {
			a = method.getMethod().getDeclaringClass().getAnnotation( IgnoreWith.class );
		}

		if( a == null ) {
			return false;
		}

		final List<Class<? extends TestClientProvider>> clients = Arrays.asList( a.value() );

		return clients.stream().anyMatch( c -> c.isInstance( AbstractTestCase.CLIENT_PROVIDER ) );
	}
}
