
package ascelion.rest.bridge.client;

import java.lang.reflect.Method;
import java.util.concurrent.CompletionStage;

import javax.ws.rs.Path;
import javax.ws.rs.core.GenericType;

import lombok.Getter;

@Getter
public class RestMethodInfo extends RestServiceInfo
{

	private final Method javaMethod;
	private final String methodURI;
	private final String httpMethod;
	private final GenericType<?> returnType;
	private final boolean async;

	RestMethodInfo( RestMethodInfo rmi )
	{
		super( rmi );

		this.javaMethod = rmi.javaMethod;
		this.methodURI = rmi.methodURI;
		this.httpMethod = rmi.httpMethod;
		this.returnType = rmi.returnType;
		this.async = rmi.async;
	}

	RestMethodInfo( RestServiceInfo rsi, Method method )
	{
		super( rsi, rsi.getServiceType() );

		this.javaMethod = method;
		this.httpMethod = RBUtils.getHttpMethod( this.javaMethod );
		this.returnType = RBUtils.genericType( getServiceType(), this.javaMethod );
		this.async = CompletionStage.class.equals( this.javaMethod.getReturnType() );
		this.methodURI = getServiceURI() + RBUtils.getRequestURI( this.javaMethod.getAnnotation( Path.class ) );
	}
}
