/*
 * $Id$
 * This is an unpublished work copyright (c) 2004 Jens-Uwe Mager
 * 30177 Hannover, Germany, jum@anubis.han.de
 */

package org.mager.gwebcache;

import java.io.*;

/**
 * Encapsulate the Gnutella client ID and version strings.
 */
public class ClientVersion implements Serializable, Comparable<ClientVersion> {

    public final static String UNKNOWN = "UNKNOWN";
    /**
     * The string identfying a particular client servent.
     * Several cache implementations require this to be a
     * four character all uppercase string.
     */
    private String client;
    /**
     * The string identifying the version of the servent.
     */
    private String version;

    public ClientVersion(String client, String version) {
        this.client = client == null ? UNKNOWN : client;
        this.version = version == null ? UNKNOWN : version;
    }

    public String getClient() {
        return client;
    }

    public String getVersion() {
        return version;
    }

    public String toString() {
        return client + "/" + version;
    }
   
    public int hashCode(){
        return client.hashCode() ^ version.hashCode();
    }

    public boolean equals(Object o){
        if (! (o instanceof ClientVersion))
            return false;
        ClientVersion c = (ClientVersion)o;
        if(c.client.equals(client) && c.version.equals(version))
            return true;
        return false;
    }

    public int compareTo(ClientVersion o) {
        int ret = client.compareTo(o.client);
        if (ret == 0)
            return version.compareTo(o.version);
        return ret;
    }
}
