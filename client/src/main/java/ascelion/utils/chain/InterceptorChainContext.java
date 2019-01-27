
package ascelion.utils.chain;

import java.util.Collection;
import java.util.Iterator;
import java.util.concurrent.Callable;
import java.util.stream.Stream;

import ascelion.utils.chain.InterceptorChain.Invocation;

import lombok.Getter;

public final class InterceptorChainContext<X>
{

	@Getter
	private final X data;
	private final Iterator<AroundInterceptor<X>> interceptors;

	InterceptorChainContext( X data, Collection<Invocation<X>> interceptors, Callable<?> action )
	{
		this.data = data;

		final Stream<AroundInterceptor<X>> last = Stream.of( context -> action.call() );

		this.interceptors = Stream.concat( interceptors.stream().map( i -> i.a ), last ).iterator();
	}

	public Object proceed() throws Exception
	{
		AroundInterceptor<X> next;

		while( ( next = this.interceptors.next() ).disabled() ) {
			;
		}

		return next.around( this );
	}
}
