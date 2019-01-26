
package ascelion.utils.chain;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.Callable;

import static java.lang.String.format;

import lombok.RequiredArgsConstructor;

public final class InterceptorChain<X>
{

	@RequiredArgsConstructor
	static class Invocation<X> implements Comparable<Invocation<X>>
	{

		final InterceptorChainWrapper<X> w;
		final int x;

		@Override
		public int compareTo( Invocation<X> that )
		{
			final int dif = Integer.compare( this.w.priority(), that.w.priority() );

			if( dif != 0 ) {
				return dif;
			}

			return Integer.compare( this.x, that.x );
		}

		@Override
		public String toString()
		{
			return this.w.getClass().getName();
		}
	}

	private final List<Invocation<X>> wrappers = new ArrayList<>();

	public void add( InterceptorChainWrapper<X> i )
	{
		Objects.requireNonNull( i, "Interceptor cannot be null" );

		this.wrappers.add( new Invocation<>( i, this.wrappers.size() ) );

		Collections.sort( this.wrappers );
	}

	public <T> T around( X data, Callable<T> action ) throws Exception
	{
		Objects.requireNonNull( action, "Intercepted action cannot be null" );

		final InterceptorChainContext<X> context = new InterceptorChainContext<>( data, this.wrappers, action );

		return (T) context.proceed();
	}

	@Override
	public String toString()
	{
		return format( "InterceptorChain[size=%d]", this.wrappers.size() );
	}
}
