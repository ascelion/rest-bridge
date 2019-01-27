
package ascelion.rest.bridge.client;

import java.util.concurrent.Executor;
import java.util.function.Function;
import java.util.function.Supplier;

import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Configuration;
import javax.ws.rs.core.Response;

public interface RestClientInfo
{

	Configuration getConfiguration();

	ConvertersFactory getConvertersFactory();

	Supplier<WebTarget> getTarget();

	Executor getExecutor();

	Function<Response, Throwable> getResponseHandler();

	AsyncInterceptor<?> getAsyncInterceptor();
}
