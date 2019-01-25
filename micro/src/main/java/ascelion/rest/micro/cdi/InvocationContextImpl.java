
package ascelion.rest.micro.cdi;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Callable;

import javax.interceptor.InvocationContext;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

@RequiredArgsConstructor
final class InvocationContextImpl implements InvocationContext
{

	private final Object target;
	private final Method method;
	private final Callable<Object> finished;
	private final Invoker<?>[] invokers;

	@Getter
	@Setter
	private Object[] parameters;
	@Getter
	private final Map<String, Object> contextData = new HashMap<>();
	private int index;

	@Override
	public Object getTarget()
	{
		return this.target;
	}

	@Override
	public Object getTimer()
	{
		return null;
	}

	@Override
	public Method getMethod()
	{
		return this.method;
	}

	@Override
	public Constructor<?> getConstructor()
	{
		return null;
	}

	@Override
	public Object proceed() throws Exception
	{
		if( this.index < this.invokers.length ) {
			try {
				return this.invokers[this.index++].invoke( this );
			}
			finally {
				this.index--;
			}
		}
		else {
			return this.finished.call();
		}
	}
}
