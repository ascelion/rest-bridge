package ascelion.utils.chain;

public interface InterceptorChainWrapper<X>
{

	Object around( InterceptorChainContext<X> context ) throws Exception;

	default int priority()
	{
		return 0;
	}
}

