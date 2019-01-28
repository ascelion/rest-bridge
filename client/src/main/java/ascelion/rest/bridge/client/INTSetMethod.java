
package ascelion.rest.bridge.client;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor( access = AccessLevel.PRIVATE )
final class INTSetMethod extends RestRequestInterceptorBase
{

	static final RestRequestInterceptor INSTANCE = new INTSetMethod();

	@Override
	protected void before( RestRequestContext rc )
	{
		RestClient.invokedMethod( rc.getMethodInfo().getJavaMethod() );
	}

	@Override
	protected void after( RestRequestContext rc, Object result, Exception exception )
	{
		RestClient.invokedMethod( null );
	}

	@Override
	public int priority()
	{
		return PRIORITY_INVOCATION - 1;
	}

}
