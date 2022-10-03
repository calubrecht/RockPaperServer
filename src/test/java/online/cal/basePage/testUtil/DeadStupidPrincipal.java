package online.cal.basePage.testUtil;

import java.security.Principal;

public class DeadStupidPrincipal implements Principal {
	String name;

	public DeadStupidPrincipal(String name) {
		this.name = name;
	}

	@Override
	public String getName() {
		// TODO Auto-generated method stub
		return name;
	}

	@Override
	public boolean equals(Object o) {
		if (o == null || !(o instanceof DeadStupidPrincipal)) {
			return false;
		}
		return name.equals(((DeadStupidPrincipal) o).name);
	}

}