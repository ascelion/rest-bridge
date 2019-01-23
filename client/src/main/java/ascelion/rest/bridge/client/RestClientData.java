
package ascelion.rest.bridge.client;

import java.util.concurrent.Executor;
import java.util.function.Function;
import java.util.function.Supplier;

import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Configuration;
import javax.ws.rs.core.Response;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
final class RestClientData
{

	final Class<?> type;
	final Configuration conf;
	final ConvertersFactory cvsf;
	final RequestInterceptor reqi;
	final Function<Response, Throwable> rsph;
	final Executor exec;
	final AsyncInterceptor<Object> aint;
	final Supplier<WebTarget> tsup;

	RestClientData( Class<?> type, RestClientData rcd, Supplier<WebTarget> tsup )
	{
		this.type = type;
		this.conf = rcd.conf;
		this.cvsf = rcd.cvsf;
		this.reqi = rcd.reqi;
		this.rsph = rcd.rsph;
		this.exec = rcd.exec;
		this.aint = rcd.aint;
		this.tsup = tsup;
	}
}
