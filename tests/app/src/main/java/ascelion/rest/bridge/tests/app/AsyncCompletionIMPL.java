
package ascelion.rest.bridge.tests.app;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.ExecutorService;
import java.util.function.Supplier;

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;

import ascelion.rest.bridge.tests.api.API;
import ascelion.rest.bridge.tests.api.AsyncAPI;
import ascelion.rest.bridge.tests.api.BeanData;

/**
 *https://github.com/eclipse-ee4j/jersey/issues/3672
 */
//@Path( "async-completion" )
@RequestScoped
public class AsyncCompletionIMPL implements AsyncAPI
{

	@Inject
	private API<BeanData> api;
	@Inject
	private ExecutorService exec;

	@Override
	public CompletionStage<BeanData> create( BeanData t )
	{
		return async( () -> this.api.create( t ) );
	}

	@Override
	public CompletionStage<Void> delete()
	{
		return async( () -> {
			this.api.delete();
			return null;
		} );
	}

	@Override
	public CompletionStage<BeanData> get()
	{
		return async( this.api::get );
	}

	@Override
	public CompletionStage<BeanData> update( BeanData t )
	{
		return async( () -> this.api.update( t ) );
	}

	private <X> CompletableFuture<X> async( Supplier<X> action )
	{
		final CompletableFuture<X> cf = new CompletableFuture<>();

		this.exec.submit( () -> {
			cf.complete( action.get() );
		} );

		return cf;
	}

}
