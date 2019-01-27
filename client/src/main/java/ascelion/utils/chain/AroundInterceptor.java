
package ascelion.utils.chain;

public interface AroundInterceptor<X>
{

	Object around( InterceptorChainContext<X> context ) throws Exception;

	default boolean disabled()
	{
		return false;
	}

	default int priority()
	{
		return 0;
	}

	default String about()
	{
		return getClass().getSimpleName();
	}
}
