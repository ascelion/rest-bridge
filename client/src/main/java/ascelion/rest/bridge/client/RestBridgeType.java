
package ascelion.rest.bridge.client;

import java.util.concurrent.Executor;
import java.util.function.Supplier;

import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Configuration;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
final class RestBridgeType
{

	final Class<?> type;
	final Configuration conf;
	final ConvertersFactory cvsf;
	final ResponseHandler rsph;
	final Executor exec;
	final AsyncInterceptor<Object> aint;
	final Supplier<WebTarget> tsup;

	RestBridgeType( Class<?> type, RestBridgeType rbt, Supplier<WebTarget> tsup )
	{
		this.type = type;
		this.conf = rbt.conf;
		this.cvsf = rbt.cvsf;
		this.rsph = rbt.rsph;
		this.exec = rbt.exec;
		this.aint = rbt.aint;
		this.tsup = tsup;
	}
}
