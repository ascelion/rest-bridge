
package ascelion.rest.bridge.web;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

import com.fasterxml.jackson.annotation.JsonAutoDetect;

import static com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility.ANY;
import static com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility.NONE;

@XmlRootElement
@XmlAccessorType( XmlAccessType.FIELD )
@JsonAutoDetect( fieldVisibility = ANY, creatorVisibility = NONE, getterVisibility = NONE, isGetterVisibility = NONE, setterVisibility = NONE )
public class UserInfoImpl
implements UserInfo
{

	private String username;

	private String password;

	public UserInfoImpl()
	{
	}

	public UserInfoImpl( String username, String password )
	{
		this.username = username;
		this.password = password;
	}

	@Override
	public String password()
	{
		return this.password;
	}

	@Override
	public String username()
	{
		return this.username;
	}
}
