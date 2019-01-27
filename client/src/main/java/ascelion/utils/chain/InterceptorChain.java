
package ascelion.utils.chain;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.Callable;

import static java.lang.String.format;
import static java.util.Collections.unmodifiableList;
import static java.util.stream.Collectors.toList;

import lombok.RequiredArgsConstructor;

public final class InterceptorChain<X>
{

	@RequiredArgsConstructor
	static class Invocation<X> implements Comparable<Invocation<X>>
	{

		final AroundInterceptor<X> a;
		final int x;

		@Override
		public int compareTo( Invocation<X> that )
		{
			final int dif = Integer.compare( this.a.priority(), that.a.priority() );

			if( dif != 0 ) {
				return dif;
			}

			return Integer.compare( this.x, that.x );
		}

		@Override
		public String toString()
		{
			return this.a.getClass().getName();
		}
	}

	private final List<Invocation<X>> invocations = new ArrayList<>();

	public void add( AroundInterceptor<X> i )
	{
		Objects.requireNonNull( i, "Interceptor cannot be null" );

		this.invocations.add( new Invocation<>( i, this.invocations.size() ) );

		Collections.sort( this.invocations );
	}

	public Collection<AroundInterceptor<X>> getAll()
	{
		return unmodifiableList( this.invocations.stream().map( i -> i.a ).collect( toList() ) );
	}

	public <T> T around( X data, Callable<T> action ) throws Exception
	{
		Objects.requireNonNull( action, "Intercepted action cannot be null" );

		final InterceptorChainContext<X> context = new InterceptorChainContext<>( data, this.invocations, action );

		return (T) context.proceed();
	}

	@Override
	public String toString()
	{
		return format( "InterceptorChain[size=%d]", this.invocations.size() );
	}
}
