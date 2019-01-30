
package ascelion.rest.bridge.client;

import java.util.function.Function;

import javax.ws.rs.core.Response;

public final class RestClientProperties
{

	static public final AsyncInterceptor<Object> NO_ASYNC_INTERCEPTOR = () -> null;
	static public final Function<RestRequestContext, RestRequestContext> NO_REQUEST_INTERCEPTOR = rc -> rc;
	static public final Function<Response, Throwable> NO_RESPONSE_HANDLER = rsp -> null;

	/**
	 * Defaults to application/octet-stream.
	 */
	static public final String DEFAULT_CONTENT_TYPE = "ascelion.rest.bridge.defaultContentType";

	static public final String RESPONSE_HANDLER = "ascelion.rest.bridge.responseHandler";
	static public final String ASYNC_INTERCEPTOR = "ascelion.rest.bridge.asyncInterceptor";
	static public final String REQUEST_INTERCEPTOR = "ascelion.rest.bridge.requestInterceptor";
}
