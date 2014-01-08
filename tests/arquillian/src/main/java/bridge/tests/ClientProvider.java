
package bridge.tests;

import java.net.URI;

public interface ClientProvider
{

	<T> T createClient( URI target, Class<T> cls );
}
