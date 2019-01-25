
package ascelion.rest.bridge.client;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.stream.Stream;

import static java.lang.String.format;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

public final class InterceptorChain<X>
{

	static public final class Context<X>
	{

		@Getter
		private final X data;
		private final Iterator<Interceptor<X>> interceptors;

		private Context( X data, Collection<Invocation<X>> interceptors, Callable<?> action )
		{
			this.data = data;

			final Stream<Interceptor<X>> last = Stream.of( context -> action.call() );

			this.interceptors = Stream.concat( interceptors.stream().map( i -> i.interceptor ), last ).iterator();
		}

		@SuppressWarnings( "rawtypes" )
		public Object proceed() throws Exception
		{
			return this.interceptors.next().around( this );
		}
	}

	public interface Interceptor<X>
	{

		Object around( Context<X> context ) throws Exception;

		default int priority()
		{
			return 0;
		}
	}

	@RequiredArgsConstructor
	static private class Invocation<X> implements Comparable<Invocation<X>>
	{

		final Interceptor<X> interceptor;
		final int x;

		@Override
		public int compareTo( Invocation<X> that )
		{
			final int dif = Integer.compare( this.interceptor.priority(), that.interceptor.priority() );

			if( dif != 0 ) {
				return dif;
			}

			return Integer.compare( this.x, that.x );
		}

		@Override
		public String toString()
		{
			return this.interceptor.getClass().getName();
		}
	}

	private final List<Invocation<X>> chain = new ArrayList<>();

	public void add( Interceptor<X> i )
	{
		this.chain.add( new Invocation<>( i, this.chain.size() ) );

		Collections.sort( this.chain );
	}

	public <T> T around( X data, Callable<T> action ) throws Exception
	{
		final Context<X> context = new Context<>( data, this.chain, action );

		return (T) context.proceed();
	}

	@Override
	public String toString()
	{
		return format( "InterceptorChain[size=%d]", this.chain.size() );
	}
}
