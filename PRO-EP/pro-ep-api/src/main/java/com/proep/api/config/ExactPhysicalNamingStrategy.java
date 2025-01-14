package com.proep.api.config;

import org.hibernate.boot.model.naming.Identifier;
import org.hibernate.engine.jdbc.env.spi.JdbcEnvironment;
import org.hibernate.boot.model.naming.PhysicalNamingStrategyStandardImpl;

public class ExactPhysicalNamingStrategy extends PhysicalNamingStrategyStandardImpl {

    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	@Override
    public Identifier toPhysicalColumnName(Identifier name, JdbcEnvironment context) {
        return name;
    }
}
