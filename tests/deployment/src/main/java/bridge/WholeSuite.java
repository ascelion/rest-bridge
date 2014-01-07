
package bridge;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

@RunWith( Suite.class )
@SuiteClasses( {
	HelloTestBridge.class, HelloTestProxy.class,
	BeanResourceBridgeTest.class, BeanResourceProxyTest.class,
} )
public class WholeSuite
{
}
