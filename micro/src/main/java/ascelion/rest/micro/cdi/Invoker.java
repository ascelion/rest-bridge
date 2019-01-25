
package ascelion.rest.micro.cdi;

import javax.enterprise.inject.spi.Interceptor;
import javax.interceptor.InvocationContext;

import static javax.enterprise.inject.spi.InterceptionType.AROUND_INVOKE;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
final class Invoker<T>
{

	private final Interceptor<T> interceptor;
	private final T instance;

	Object invoke( InvocationContext context ) throws Exception
	{
		return this.interceptor.intercept( AROUND_INVOKE, this.instance, context );
	}
}
