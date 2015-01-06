/**
 * 
 */
package com.i2s.hibernate.sample;

import org.hibernate.dialect.HSQLDialect;

/**
 * @author rmoliveira
 * 
 */
public class I2SHSQLDialect extends HSQLDialect {

    @Override
    public String getForUpdateString() {
        return " for update";
    }

}
