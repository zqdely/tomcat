/*
 * Copyright 1999,2004 The Apache Software Foundation.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */


package org.apache.catalina.deploy;

import java.io.Serializable;


/**
 * Representation of a context initialization parameter that is configured
 * in the server configuration file, rather than the application deployment
 * descriptor.  This is convenient for establishing default values (which
 * may be configured to allow application overrides or not) without having
 * to modify the application deployment descriptor itself.
 *
 * @author Craig R. McClanahan
 * @version $Revision: 302879 $ $Date: 2004-05-13 22:40:49 +0200 (jeu., 13 mai 2004) $
 */

public class ApplicationParameter implements Serializable {


    // ------------------------------------------------------------- Properties


    /**
     * The description of this environment entry.
     */
    private String description = null;

    public String getDescription() {
        return (this.description);
    }

    public void setDescription(String description) {
        this.description = description;
    }


    /**
     * The name of this application parameter.
     */
    private String name = null;

    public String getName() {
        return (this.name);
    }

    public void setName(String name) {
        this.name = name;
    }


    /**
     * Does this application parameter allow overrides by the application
     * deployment descriptor?
     */
    private boolean override = true;

    public boolean getOverride() {
        return (this.override);
    }

    public void setOverride(boolean override) {
        this.override = override;
    }


    /**
     * The value of this application parameter.
     */
    private String value = null;

    public String getValue() {
        return (this.value);
    }

    public void setValue(String value) {
        this.value = value;
    }

    // --------------------------------------------------------- Public Methods


    /**
     * Return a String representation of this object.
     */
    public String toString() {

        StringBuffer sb = new StringBuffer("ApplicationParameter[");
        sb.append("name=");
        sb.append(name);
        if (description != null) {
            sb.append(", description=");
            sb.append(description);
        }
        sb.append(", value=");
        sb.append(value);
        sb.append(", override=");
        sb.append(override);
        sb.append("]");
        return (sb.toString());

    }


}
