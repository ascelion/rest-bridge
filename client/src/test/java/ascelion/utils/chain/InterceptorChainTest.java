
package ascelion.utils.chain;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import ascelion.utils.chain.InterceptorChain;
import ascelion.utils.chain.InterceptorChainContext;
import ascelion.utils.chain.AroundInterceptor;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.Assert.assertThat;

import lombok.RequiredArgsConstructor;
import org.junit.Test;

public class InterceptorChainTest
{

	@RequiredArgsConstructor
	static class INT implements AroundInterceptor<AtomicInteger>
	{

		final AtomicInteger i;
		final AtomicInteger o;
		final int p;
		final int x;
		final List<Integer> called;

		@Override
		public Object around( InterceptorChainContext<AtomicInteger> context ) throws Exception
		{
			this.i.incrementAndGet();

			try {
				context.getData().incrementAndGet();

				this.called.add( this.x );

				return context.proceed();
			}
			finally {
				this.o.incrementAndGet();
			}
		}

		@Override
		public int priority()
		{
			return this.p;
		}
	}

	@Test
	public void run() throws Exception
	{
		final AtomicInteger a1 = new AtomicInteger();
		final AtomicInteger i = new AtomicInteger();
		final AtomicInteger o = new AtomicInteger();

		final InterceptorChain<AtomicInteger> chain = new InterceptorChain<>();
		final List<Integer> called = new ArrayList<>();

		chain.add( new INT( i, o, 4, 0, called ) );
		chain.add( new INT( i, o, 2, 1, called ) );
		chain.add( new INT( i, o, 3, 2, called ) );
		chain.add( new INT( i, o, 0, 3, called ) );
		chain.add( new INT( i, o, 2, 4, called ) );

		final Object v = chain.around( a1, () -> a1.get() );

		assertThat( a1.get(), equalTo( 5 ) );
		assertThat( v, equalTo( 5 ) );

		assertThat( i.get(), equalTo( 5 ) );
		assertThat( o.get(), equalTo( 5 ) );

		assertThat( called, hasSize( 5 ) );
		assertThat( called.get( 0 ), equalTo( 3 ) );
		assertThat( called.get( 1 ), equalTo( 1 ) );
		assertThat( called.get( 2 ), equalTo( 4 ) );
		assertThat( called.get( 3 ), equalTo( 2 ) );
		assertThat( called.get( 4 ), equalTo( 0 ) );
	}

	@Test
	public void run0() throws Exception
	{
		final AtomicInteger a1 = new AtomicInteger();
		final AtomicInteger i = new AtomicInteger();
		final AtomicInteger o = new AtomicInteger();

		final InterceptorChain<AtomicInteger> chain = new InterceptorChain<>();

		final Object v = chain.around( a1, () -> a1.getAndIncrement() );

		assertThat( v, equalTo( 0 ) );
		assertThat( a1.get(), equalTo( 1 ) );

		assertThat( i.get(), equalTo( 0 ) );
		assertThat( o.get(), equalTo( 0 ) );
	}

}
