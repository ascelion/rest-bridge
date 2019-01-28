
package ascelion.rest.micro.cdi;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import javax.interceptor.InvocationContext;

import ascelion.rest.bridge.client.RestRequestContext;
import ascelion.utils.chain.InterceptorChainContext;

import lombok.Getter;

final class CDIAroundInvocation implements InvocationContext
{

	private final InterceptorChainContext<RestRequestContext> icc;
	private final RestRequestContext rrc;

	CDIAroundInvocation( InterceptorChainContext<RestRequestContext> icc )
	{
		this.icc = icc;
		this.rrc = icc.getData();
	}

	@Override
	public Object getTarget()
	{
		return this.rrc.getService();
	}

	@Override
	public Object getTimer()
	{
		return null;
	}

	@Override
	public Method getMethod()
	{
		return this.rrc.getMethodInfo().getJavaMethod();
	}

	@Override
	public Constructor<?> getConstructor()
	{
		return null;
	}

	@Override
	public Object[] getParameters()
	{
		return this.rrc.getArguments();
	}

	@Override
	public void setParameters( Object[] params )
	{
		throw new UnsupportedOperationException( "TODO" );
	}

	@Getter
	private final Map<String, Object> contextData = new HashMap<>();

	@Override
	public Object proceed() throws Exception
	{
		return this.icc.proceed();
	}

}
