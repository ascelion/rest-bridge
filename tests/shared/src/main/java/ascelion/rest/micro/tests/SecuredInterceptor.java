
package ascelion.rest.micro.tests;

import java.util.ArrayList;
import java.util.List;

import javax.interceptor.AroundInvoke;
import javax.interceptor.Interceptor;
import javax.interceptor.InvocationContext;

import ascelion.rest.bridge.client.RBUtils;

@Secured
@Interceptor
public class SecuredInterceptor
{

	static public List<String> invoked = new ArrayList<>();

	@AroundInvoke
	public Object check( InvocationContext ctx ) throws Exception
	{
		RBUtils.findAnnotations( Secured.class, ctx.getMethod(), ctx.getTarget().getClass() )
			.stream()
			.map( Secured::value )
			.forEach( invoked::add );

		return ctx.proceed();
	}
}
