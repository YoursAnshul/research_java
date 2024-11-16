package com.pro.api.config;

import org.hibernate.boot.model.naming.Identifier;
import org.hibernate.boot.model.naming.PhysicalNamingStrategyStandardImpl;
import org.hibernate.engine.jdbc.env.spi.JdbcEnvironment;

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
