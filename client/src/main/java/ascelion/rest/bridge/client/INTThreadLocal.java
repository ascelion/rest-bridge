
package ascelion.rest.bridge.client;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor( access = AccessLevel.PRIVATE )
final class INTThreadLocal extends RestRequestInterceptorBase
{

	static final RestRequestInterceptor INSTANCE = new INTThreadLocal();

	@Override
	protected void before( RestRequestContext rc )
	{
		RestRequestContext.TL.set( rc );
	}

	@Override
	protected void after( RestRequestContext rc, Object result, Exception exception )
	{
		RestRequestContext.TL.remove();
	}

	@Override
	public int priority()
	{
		return PRIORITY_INVOCATION - 1;
	}

}
