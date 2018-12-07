
package ascelion.rest.bridge.web;

import javax.ws.rs.DefaultValue;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.PathParam;
import javax.ws.rs.QueryParam;

import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Data
public class BeanParamData
{

	@PathParam( "path1" )
	private String pathParam1;

	@HeaderParam( "header1" )
	@DefaultValue( "none" )
	private String headerParam1;

	@QueryParam( "query1" )
	private String queryParam1;

	@PathParam( "path2" )
	private String pathParam2;

	@HeaderParam( "header2" )
	private String headerParam2;

	@QueryParam( "query2" )
	private String queryParam2;

	public BeanParamData( String pathParam2, String headerParam2, String queryParam2 )
	{
		this.pathParam2 = pathParam2;
		this.headerParam2 = headerParam2;
		this.queryParam2 = queryParam2;
	}

}
