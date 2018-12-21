
package ascelion.rest.bridge.tests.arquillian;

import java.util.Collection;
import java.util.HashSet;
import java.util.stream.Stream;

import ascelion.rest.bridge.tests.TestClientProvider;
import ascelion.rest.bridge.tests.arquillian.IgnoreWithProvider.IgnoreWithProviders;

import static java.util.Arrays.asList;

import org.jboss.arquillian.junit.Arquillian;
import org.junit.runner.notification.RunNotifier;
import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.InitializationError;

public class ArquillianUnit
extends Arquillian
{

	static {
		asList( System.getProperty( "java.class.path", "" ).split( ":" ) ).forEach( System.out::println );
	}

	public ArquillianUnit( Class<?> klass ) throws InitializationError
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

	@Override
	protected boolean isIgnored( FrameworkMethod method )
	{
		if( super.isIgnored( method ) ) {
			return true;
		}

		final Collection<IgnoreWithProvider> ignores = new HashSet<>();

		IgnoreWithProvider a1 = method.getAnnotation( IgnoreWithProvider.class );

		if( a1 == null ) {
			a1 = method.getMethod().getDeclaringClass().getAnnotation( IgnoreWithProvider.class );
		}
		if( a1 != null ) {
			ignores.add( a1 );
		}

		IgnoreWithProviders a2 = method.getAnnotation( IgnoreWithProviders.class );

		if( a2 == null ) {
			a2 = method.getMethod().getDeclaringClass().getAnnotation( IgnoreWithProviders.class );
		}
		if( a2 != null ) {
			ignores.addAll( asList( a2.value() ) );
		}

		return ignores.stream()
			.flatMap( t -> Stream.of( t.value() ) )
			.anyMatch( c -> c.isInstance( TestClientProvider.getInstance() ) );
	}
}
