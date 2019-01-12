
package ascelion.rest.bridge.tests;

import java.util.concurrent.CompletionStage;
import java.util.concurrent.ExecutionException;

import ascelion.rest.bridge.tests.api.AsyncBeanAPI;
import ascelion.rest.bridge.tests.api.BeanData;
import ascelion.rest.bridge.tests.arquillian.IgnoreWithProvider;
import ascelion.rest.bridge.tests.providers.JerseyProxyProvider;
import ascelion.rest.bridge.tests.providers.ResteasyProxyProvider;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import org.junit.Test;

public class AsyncAPITest
extends AbstractTestCase<AsyncBeanAPI>
{

	@Override
	public void setUp() throws Exception
	{
		TestClientProvider.getInstance().getBuilder().register( AsyncThreadFilter.class );

		AsyncThreadFilter.reset();

		super.setUp();
	}

	@Override
	public void tearDown()
	{
		AsyncThreadFilter.checkThread();

		super.tearDown();
	}

	@Test
	@IgnoreWithProvider(
		value = { ResteasyProxyProvider.class, JerseyProxyProvider.class, },
		reason = "interface too generic, cannot infer the return type" )
	public void create() throws InterruptedException, ExecutionException
	{
		final BeanData b0 = new BeanData( "create" );
		final CompletionStage<BeanData> cs = this.client.create( b0 );
		final BeanData b1 = cs.toCompletableFuture().get();

		assertThat( b1, is( equalTo( b0 ) ) );
	}

	@Test
	@IgnoreWithProvider(
		value = { ResteasyProxyProvider.class, JerseyProxyProvider.class, },
		reason = "interface too generic, cannot infer the return type" )
	public void get() throws InterruptedException, ExecutionException
	{
		final BeanData b0 = new BeanData( "value" );
		final CompletionStage<BeanData> cs = this.client.get();
		final BeanData b1 = cs.toCompletableFuture().get();

		assertThat( b1, is( equalTo( b0 ) ) );
	}

	@Test
	@IgnoreWithProvider(
		value = { ResteasyProxyProvider.class, JerseyProxyProvider.class, },
		reason = "interface too generic, cannot infer the return type" )
	public void update() throws InterruptedException, ExecutionException
	{
		final BeanData b0 = new BeanData( "update" );
		final CompletionStage<BeanData> cs = this.client.update( b0 );
		final BeanData b1 = cs.toCompletableFuture().get();

		assertThat( b1, is( equalTo( b0 ) ) );
	}

	@Test
	public void delete() throws InterruptedException, ExecutionException
	{
		this.client
			.delete()
			.toCompletableFuture()
			.get();
	}
}
