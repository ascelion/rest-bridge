
package ascelion.rest.bridge.web;

import javax.validation.constraints.NotNull;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Data
public class BeanData
{

	@NotNull
	private String notNull;

}
