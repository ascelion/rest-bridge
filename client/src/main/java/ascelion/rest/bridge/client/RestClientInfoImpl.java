
package ascelion.rest.bridge.client;

import java.util.concurrent.Executor;
import java.util.function.Function;
import java.util.function.Supplier;

import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Configuration;
import javax.ws.rs.core.Response;

import lombok.Getter;

@Getter
class RestClientInfoImpl implements RestClientInfo
{

	private final AsyncInterceptor<?> asyncInterceptor;
	private final Function<Response, Throwable> responseHandler;
	private final Executor executor;
	private final ConvertersFactory convertersFactory;
	private final Supplier<WebTarget> target;
	private final Configuration configuration;

	RestClientInfoImpl( RestClientInfo rci )
	{
		this( rci, rci.getTarget() );
	}

	RestClientInfoImpl( RestClientInfo rci, Supplier<WebTarget> target )
	{
		this.target = target;
		this.configuration = rci.getConfiguration();
		this.asyncInterceptor = rci.getAsyncInterceptor();
		this.responseHandler = rci.getResponseHandler();
		this.executor = rci.getExecutor();
		this.convertersFactory = rci.getConvertersFactory();
	}

}
