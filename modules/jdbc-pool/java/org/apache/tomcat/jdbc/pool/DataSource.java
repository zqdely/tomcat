/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.tomcat.jdbc.pool;

import java.io.PrintWriter;
import java.lang.management.ManagementFactory;
import java.sql.SQLException;
import java.util.Hashtable;
import java.util.Properties;

import javax.management.InstanceNotFoundException;
import javax.management.MBeanRegistration;
import javax.management.MBeanServer;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;

import org.apache.tomcat.jdbc.pool.jmx.ConnectionPoolMBean;


/**
 * A DataSource that can be instantiated through IoC and implements the DataSource interface
 * since the DataSourceProxy is used as a generic proxy
 * @author Filip Hanik
 * @version 1.0
 */
public class DataSource extends DataSourceProxy implements MBeanRegistration,javax.sql.DataSource, org.apache.tomcat.jdbc.pool.jmx.ConnectionPoolMBean {

    public DataSource() {
        super();
    }

    public DataSource(PoolProperties poolProperties) {
        super(poolProperties);
    }

//===============================================================================
//  Register the actual pool itself under the tomcat.jdbc domain
//===============================================================================
    protected volatile ObjectName oname = null;

    /**
     * Unregisters the underlying connection pool mbean.<br/>
     * {@inheritDoc}
     */
    public void postDeregister() {
        if (oname!=null) unregisterJmx();
    }

    /**
     * no-op<br/>
     * {@inheritDoc}
     */
    public void postRegister(Boolean registrationDone) {
    }


    /**
     * no-op<br/>
     * {@inheritDoc}
     */
    public void preDeregister() throws Exception {
    }

    /**
     * If the connection pool MBean exists, it will be registered during this operation.<br/>
     * {@inheritDoc}
     */
    public ObjectName preRegister(MBeanServer server, ObjectName name) throws Exception {
        try {
            this.oname = createObjectName(name);
            if (oname!=null) registerJmx();
        }catch (MalformedObjectNameException x) {
            log.error("Unable to create object name for JDBC pool.",x);
        }
        return name;   
    }
    
    /**
     * Creates the ObjectName for the ConnectionPoolMBean object to be registered
     * @param original the ObjectName for the DataSource
     * @return the ObjectName for the ConnectionPoolMBean
     * @throws MalformedObjectNameException
     */
    public ObjectName createObjectName(ObjectName original) throws MalformedObjectNameException {
        String domain = "tomcat.jdbc";
        Hashtable<String,String> properties = original.getKeyPropertyList();
        String origDomain = original.getDomain();
        properties.put("type", "ConnectionPool");
        properties.put("class", this.getClass().getName());
        if (original.getKeyProperty("path")!=null) {
            properties.put("engine", origDomain);
        }
        ObjectName name = new ObjectName(domain,properties);
        return name;
    }
    
    /**
     * Registers the ConnectionPoolMBean under a unique name based on the ObjectName for the DataSource
     */
    protected void registerJmx() {
        try {
            if (pool.getJmxPool()!=null) {
                MBeanServer mbs = ManagementFactory.getPlatformMBeanServer();
                mbs.registerMBean(pool.getJmxPool(), oname);
            }
        } catch (Exception e) {
            log.error("Unable to register JDBC pool with JMX",e);
        }
    }
    
    protected void unregisterJmx() {
        try {
            MBeanServer mbs = ManagementFactory.getPlatformMBeanServer();
            mbs.unregisterMBean(oname);
        } catch (InstanceNotFoundException ignore) {
        } catch (Exception e) {
            log.error("Unable to unregister JDBC pool with JMX",e);
        }
    }

//===============================================================================
//  Expose JMX attributes through Tomcat's dynamic reflection
//===============================================================================
    /**
     * Forces an abandon check on the connection pool.
     * If connections that have been abandoned exists, they will be closed during this run
     */
    public void checkAbandoned() {
        try {
            createPool().checkAbandoned();
        }catch (SQLException x) {
            throw new RuntimeException(x);
        }
    }

    /**
     * Forces a check for downsizing the idle connections
     */
    public void checkIdle() {
        try {
            createPool().checkIdle();
        }catch (SQLException x) {
            throw new RuntimeException(x);
        }
    }

    /**
     * @return number of connections in use by the application
     */
    public int getActive() {
        try {
            return createPool().getActive();
        }catch (SQLException x) {
            throw new RuntimeException(x);
        }
    }
    
    /**
     * @return number of connections in use by the application
     * {@link DataSource#getActive()}
     */
    public int getNumActive() {
        return getActive();
    }

    /**
     * @return number of threads waiting for a connection
     */
    public int getWaitCount() {
        try {
            return createPool().getWaitCount();
        }catch (SQLException x) {
            throw new RuntimeException(x);
        }
    }

    /**
     * NOT USED ANYWHERE
     * @return nothing 
     */
    public String getConnectionProperties() {
        try {
            return createPool().getPoolProperties().getConnectionProperties();
        }catch (SQLException x) {
            throw new RuntimeException(x);
        }
    }

    /**
     * @return connection properties passed into the JDBC Driver upon connect
     */
    public Properties getDbProperties() {
        try {
            return createPool().getPoolProperties().getDbProperties();
        }catch (SQLException x) {
            throw new RuntimeException(x);
        }
    }

    /**
     * @return the configured default catalog
     */
    public String getDefaultCatalog() {
        try {
            return createPool().getPoolProperties().getDefaultCatalog();
        }catch (SQLException x) {
            throw new RuntimeException(x);
        }
    }

    /**
     * @return the configured default isolation level
     */
    public int getDefaultTransactionIsolation() {
        try {
            return createPool().getPoolProperties().getDefaultTransactionIsolation();
        }catch (SQLException x) {
            throw new RuntimeException(x);
        }
    }

    /**
     * @return the configured driver class name
     */
    public String getDriverClassName() {
        try {
            return createPool().getPoolProperties().getDriverClassName();
        }catch (SQLException x) {
            throw new RuntimeException(x);
        }
    }

    /**
     * @return the number of established but idle connections
     */
    public int getIdle() {
        try {
            return createPool().getIdle();
        }catch (SQLException x) {
            throw new RuntimeException(x);
        }
    }

    /**
     * {@link #getIdle()}
     */
    public int getNumIdle() {
        return getIdle();
    }

    /**
     * @return the configured number of initial connections 
     */
    public int getInitialSize() {
        try {
            return createPool().getPoolProperties().getInitialSize();
        }catch (SQLException x) {
            throw new RuntimeException(x);
        }
    }

    /**
     * @return the configured initialization SQL 
     */
    public String getInitSQL() {
        try {
            return createPool().getPoolProperties().getInitSQL();
        }catch (SQLException x) {
            throw new RuntimeException(x);
        }
    }

    /**
     * @return the configuration string for interceptors
     */
    public String getJdbcInterceptors() {
        try {
            return createPool().getPoolProperties().getJdbcInterceptors();
        }catch (SQLException x) {
            throw new RuntimeException(x);
        }
    }

    /**
     * @return the configured number of maximum allowed connections
     */
    public int getMaxActive() {
        try {
            return createPool().getPoolProperties().getMaxActive();
        }catch (SQLException x) {
            throw new RuntimeException(x);
        }
    }

    /**
     * @return the configured number of maximum idle connections
     */
    public int getMaxIdle() {
        try {
            return createPool().getPoolProperties().getMaxIdle();
        }catch (SQLException x) {
            throw new RuntimeException(x);
        }
    }

    /**
     * @return the configured maximum wait time in milliseconds if a connection is not available
     */
    public int getMaxWait() {
        try {
            return createPool().getPoolProperties().getMaxWait();
        }catch (SQLException x) {
            throw new RuntimeException(x);
        }
    }

    /**
     * @return the configured idle time, before a connection that is idle can be released
     */
    public int getMinEvictableIdleTimeMillis() {
        try {
            return createPool().getPoolProperties().getMinEvictableIdleTimeMillis();
        }catch (SQLException x) {
            throw new RuntimeException(x);
        }
    }

    /**
     * @return the configured minimum amount of idle connections 
     */
    public int getMinIdle() {
        try {
            return createPool().getPoolProperties().getMinIdle();
        }catch (SQLException x) {
            throw new RuntimeException(x);
        }
    }
    
    /**
     * @return the configured maxAge for a connection.
     * A connection that has been established for longer than this configured value in milliseconds
     * will be closed upon a return
     */
    public long getMaxAge() {
        try {
            return createPool().getPoolProperties().getMaxAge();
        }catch (SQLException x) {
            throw new RuntimeException(x);
        }
    }    

    /**
     * @return the name of the pool
     */
    public String getName() {
        try {
            return createPool().getName();
        }catch (SQLException x) {
            throw new RuntimeException(x);
        }
    }

    /**
     * @return the configured value - not used in this implementation
     */
    public int getNumTestsPerEvictionRun() {
        try {
            return createPool().getPoolProperties().getNumTestsPerEvictionRun();
        }catch (SQLException x) {
            throw new RuntimeException(x);
        }
    }

    /**
     * @return DOES NOT RETURN THE PASSWORD, IT WOULD SHOW UP IN JMX
     */
    public String getPassword() {
        return "Password not available as DataSource/JMX operation.";
    }

    /**
     * @return the configured remove abandoned timeout in seconds
     */
    public int getRemoveAbandonedTimeout() {
        try {
            return createPool().getPoolProperties().getRemoveAbandonedTimeout();
        }catch (SQLException x) {
            throw new RuntimeException(x);
        }
    }

    /**
     * @return the current size of the pool
     */
    public int getSize() {
        try {
            return createPool().getSize();
        }catch (SQLException x) {
            throw new RuntimeException(x);
        }
    }

    public int getTimeBetweenEvictionRunsMillis() {
        try {
            return createPool().getPoolProperties().getTimeBetweenEvictionRunsMillis();
        }catch (SQLException x) {
            throw new RuntimeException(x);
        }
    }

    public String getUrl() {
        try {
            return createPool().getPoolProperties().getUrl();
        }catch (SQLException x) {
            throw new RuntimeException(x);
        }
    }

    public String getUsername() {
        try {
            return createPool().getPoolProperties().getUsername();
        }catch (SQLException x) {
            throw new RuntimeException(x);
        }
    }

    public long getValidationInterval() {
        try {
            return createPool().getPoolProperties().getValidationInterval();
        }catch (SQLException x) {
            throw new RuntimeException(x);
        }
    }

    public String getValidationQuery() {
        try {
            return createPool().getPoolProperties().getValidationQuery();
        }catch (SQLException x) {
            throw new RuntimeException(x);
        }
    }

    public boolean isAccessToUnderlyingConnectionAllowed() {
        try {
            return createPool().getPoolProperties().isAccessToUnderlyingConnectionAllowed();
        }catch (SQLException x) {
            throw new RuntimeException(x);
        }
    }

    public boolean isDefaultAutoCommit() {
        try {
            return createPool().getPoolProperties().isDefaultAutoCommit();
        }catch (SQLException x) {
            throw new RuntimeException(x);
        }
    }

    public boolean isDefaultReadOnly() {
        try {
            return createPool().getPoolProperties().isDefaultReadOnly();
        }catch (SQLException x) {
            throw new RuntimeException(x);
        }
    }

    public boolean isLogAbandoned() {
        try {
            return createPool().getPoolProperties().isLogAbandoned();
        }catch (SQLException x) {
            throw new RuntimeException(x);
        }
    }

    public boolean isPoolSweeperEnabled() {
        try {
            return createPool().getPoolProperties().isPoolSweeperEnabled();
        }catch (SQLException x) {
            throw new RuntimeException(x);
        }
    }

    public boolean isRemoveAbandoned() {
        try {
            return createPool().getPoolProperties().isRemoveAbandoned();
        }catch (SQLException x) {
            throw new RuntimeException(x);
        }
    }

    public int getAbandonWhenPercentageFull() {
        try {
            return createPool().getPoolProperties().getAbandonWhenPercentageFull();
        }catch (SQLException x) {
            throw new RuntimeException(x);
        }
    }

    public boolean isTestOnBorrow() {
        try {
            return createPool().getPoolProperties().isTestOnBorrow();
        }catch (SQLException x) {
            throw new RuntimeException(x);
        }
    }

    public boolean isTestOnConnect() {
        try {
            return createPool().getPoolProperties().isTestOnConnect();
        }catch (SQLException x) {
            throw new RuntimeException(x);
        }
    }

    public boolean isTestOnReturn() {
        try {
            return createPool().getPoolProperties().isTestOnReturn();
        }catch (SQLException x) {
            throw new RuntimeException(x);
        }
    }

    public boolean isTestWhileIdle() {
        try {
            return createPool().getPoolProperties().isTestWhileIdle();
        }catch (SQLException x) {
            throw new RuntimeException(x);
        }
    }

    public void testIdle() {
        try {
            createPool().testAllIdle();
        }catch (SQLException x) {
            throw new RuntimeException(x);
        }
    }

}
