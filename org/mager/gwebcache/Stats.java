/*
 * $Id$
 * This is an unpublished work copyright (c) 2004 Jens-Uwe Mager
 * 30177 Hannover, Germany, jum@anubis.han.de
 */

package org.mager.gwebcache;

import java.io.*;
import java.util.*;
import javax.servlet.*;

/**
 * Implement the simple statistics as defined per the V1 protocol,
 * stat by client/version, by this day/last day and this week/last week.
 * Also used by stats.jsp for display.
 * Follow the singleton pattern
 */
public class Stats  implements Serializable {
    /**
     * The date the stat was started
     */
    public Date statsStartTime;
    /**
     * The date this cache was started.
     */
    public Date startTime;
    /**
     * The date this cache was stoped.
     * Will be used to determine the average uptime/downtime
     */
    public Date stopTime;
    /**
     * The hour we last updated the hourly statistics.
     */
    public volatile long lastHour;
    /**
     * The day we last updated the daily statistics.
     */
    public volatile long lastDay;
    /**
     * The week we last updated the weekly statistics.
     */
    public volatile long lastWeek;
    /**
     * The number of requests received.
     */
    public volatile Counter numRequests;

    /**
     * The number of updates received.
     */
    public volatile Counter numUpdates;

    /**
     * The number of urlfile requests received.
     * This is a GWC1 requests
     */
    public volatile Counter urlfileRequests;

    /**
     * The number of hostfile requests received.
     * This is a GWC1 requests
     */
    public volatile Counter hostfileRequests;
    
    /**
     * The number of statfile requests received.
     * This is a GWC1 requests.
     */
    public volatile Counter statfileRequests;
    
    /**
     * The number of ping requests received.
     * This is a GWC1 requests.
     */
    public volatile Counter pingRequestsGWC1;
    
    /**
     * The number of update received.
     * This is a GWC1 update.
     */
    public volatile Counter updateRequestsGWC1;
    
    /**
     * The number of url=... updates received.
     * This is a GWC1 update.
     */
    public volatile Counter URLUpdateRequestsGWC1;
    
    /**
     * The number of ip=X.Y.Z.W:PORT updates received.
     * This is a GWC1 update.
     */
    public volatile Counter IPUpdateRequestsGWC1;
    
    /**
     * The number of get requests received.
     * This is a GWC2 requests for url and host
     */
    public volatile Counter getRequests;
    
    /**
     * The number of ping requests received.
     * This is a GWC2 requests.
     */
    public volatile Counter pingRequestsGWC2;
    
    /**
     * The number of updates received.
     * This is a GWC2 update.
     */
    public volatile Counter updateRequestsGWC2;
    
    /**
     * The number of url=... updates received.
     * This is a GWC2 update.
     */
    public volatile Counter URLUpdateRequestsGWC2;
    
    /**
     * The number of ip=X.Y.Z.W:PORT updates received.
     * This is a GWC2 update.
     */
    public volatile Counter IPUpdateRequestsGWC2;
    /**
     * The number of stats page requests received.
     */
    public volatile Counter numStatsRequests;
    /**
     * The number of data page requests received.
     */
    public volatile Counter numDataRequests;
    /**
     * The number of index page requests received.
     */
    public volatile Counter numIndexRequests;
    /**
     * The number of licence page requests received.
     */
    public volatile Counter numLicenseRequests;
    /**
     * The number of request by client 
     * We don't differenciate between version
     * String(client) -> Counter
     */
    public Map clientRequests;

    /**
     * The number of request by client/version 
     * We differenciate between version
     * ClientVersion -> Counter
     */
    public Map clientVersionRequests;
    
    /**
     * The only instance of the stats data.
     */
    private static Stats instance = new Stats();

    private Stats() {
        clientVersionRequests =
                        Collections.synchronizedMap( new HashMap());
        clientRequests =
                        Collections.synchronizedMap( new HashMap());
        numUpdates = new Counter();
        numRequests = new Counter();
        
        hostfileRequests = new Counter();
        urlfileRequests = new Counter();
        statfileRequests = new Counter();
        pingRequestsGWC1 = new Counter();
        updateRequestsGWC1 = new Counter();
        URLUpdateRequestsGWC1 = new Counter();
        IPUpdateRequestsGWC1 = new Counter();
       
        getRequests = new Counter();
        pingRequestsGWC2 = new Counter();
        updateRequestsGWC2 = new Counter();
        URLUpdateRequestsGWC2 = new Counter();
        IPUpdateRequestsGWC2 = new Counter();
        
        numStatsRequests = new Counter();
        numDataRequests = new Counter();
        numIndexRequests = new Counter();
        numLicenseRequests = new Counter();
        
        startTime = new Date();
        statsStartTime = startTime;
        lastHour = startTime.getTime()/Data.MILLIS_PER_HOUR;
        lastDay = startTime.getTime()/Data.MILLIS_PER_DAY;
        lastWeek = startTime.getTime()/Data.MILLIS_PER_WEEK;
        stopTime = null;
    }

    /**
     * Retrieve the current cache Stats instance.
     * @return A Stats obejct.
     */
    public static Stats getInstance() {
        return instance;
    }

    /**
     * Check if we are past the current hour and move the
     * stats from this hour to that of the previous hour. Do
     * it also for day and week.
     * @param now An instance of Date signifying the current
     * time.
     */
    public void bumpHour(Date now) {
        bumpHour(now.getTime());
    }

    /**
     * Check if we are past the current hour and move the
     * stats from this hour to that of the previous hour. Do
     * it also for day and week
     * @param now the time in millisecond since 1st january 1970
     */
    public void bumpHour(long now) {
        int time = 0;
        long thisHour = now / Data.MILLIS_PER_HOUR;
        long thisDay = now / Data.MILLIS_PER_DAY;
        long thisWeek = now / Data.MILLIS_PER_WEEK;
        if (thisWeek != lastWeek) {
            lastWeek = thisWeek;
            time = Counter.WEEK_UPDATE;
        } else if (thisDay != lastDay) {
            lastDay = thisDay;
            time = Counter.DAY_UPDATE;
        } else if (thisHour != lastHour) {
            lastHour = thisHour;
            time = Counter.HOUR_UPDATE;
        }
        if (time != 0) {
            numRequests.bumpTime(time);
            numUpdates.bumpTime(time);
            //GWC1 requests
            urlfileRequests.bumpTime(time);
            hostfileRequests.bumpTime(time);
            statfileRequests.bumpTime(time);
            pingRequestsGWC1.bumpTime(time);
            updateRequestsGWC1.bumpTime(time);
            //GWC2 requests
            updateRequestsGWC2.bumpTime(time);
            getRequests.bumpTime(time);
            pingRequestsGWC2.bumpTime(time);
            //Page requests
            numStatsRequests.bumpTime(time);
            numDataRequests.bumpTime(time);
            numIndexRequests.bumpTime(time);
            numLicenseRequests.bumpTime(time);
            //Requests by client
            for (Iterator i = clientRequests.entrySet().iterator();
                                                            i.hasNext();) {
                ((Counter) ((Map.Entry) i.next()).getValue()).bumpTime(time);
            }
            //Request by client/version
            for (Iterator i = clientVersionRequests.entrySet().iterator();
                                                            i.hasNext();) {
                ((Counter) ((Map.Entry) i.next()).getValue()).bumpTime(time);
            }
        }
    }

    /**
     * Increment the number of requests received.
     */
    public void bumpRequests() {
        numRequests.bumpCount();
    }

    /**
     * Increment the total number of updates received.
     */
    private void bumpUpdates() {
        numUpdates.bumpCount();
    }

    /**
     * Increment the number of GWC1 updates received.
     * and total number of updates received.
     */
    public void bumpGWC1Updates() {
        bumpUpdates();
        updateRequestsGWC1.bumpCount();
    }

    /**
     * Increment the number GWC2 of updates received
     * and total number of updates received.
     */
    public void bumpGWC2Updates() {
        bumpUpdates();
        updateRequestsGWC2.bumpCount();
    }

    /**
     * Increment the number of urlfile request
     * This is a GWC1 request
     */
    public void bumpUrlRequests(){
        urlfileRequests.bumpCount();
    }
	
    /**
     * Increment the number of hostfile request
     * This is a GWC1 request
     */
    public void bumpHostRequests(){
        hostfileRequests.bumpCount();
    }

    /**
     * Increment the number of statfile request
     * This is a GWC1 request
     */
    public void bumpStatRequests(){
        statfileRequests.bumpCount();
    }

    /**
     * Increment the number of get request
     * This is a GWC2 request
     */
    public void bumpGetRequests(){
        getRequests.bumpCount();
    }
	
    /**
     * This bump the client only and client/version count
     * @param c
     */
    public void bumpByClient(ClientVersion c) {
        String client = c.getClient();

        //This is not very well syncronized, but a mistake
        //make that we will not count 1 request
		
        //We bump the number of request for this client
        Counter count = (Counter)clientRequests.get(client);
        if(count == null){
            clientRequests.put(client,new Counter(1));
        } else
            count.bumpCount();

        //We bump the number of request for this client/version
        count = (Counter)clientVersionRequests.get(c);
        if(count == null){
            clientVersionRequests.put(c,new Counter(1));
        } else
            count.bumpCount();
    } 

    /**
     * Find the location for the cache stats file. If the "filename"
     * init parameter is not given, use a file in the servlet container
     * provided temporary working directory.
     * @param context The ServletContext to use for logging and
     * for for extracting runtime arguments.
     * @return A File object describing the data file.
     */
    public static File statsFile(ServletContext context) {
        File f;
        String fname = context.getInitParameter("statsfilename");
        if (fname != null)
            f = new File(fname);
        else {
            File tmpDir =
                (File)context.getAttribute("javax.servlet.context.tempdir");
            f = new File(tmpDir, "gwebcache.stats");
        }
        //context.log("datafile=" + f);
        return f;
    }

    /**
     * Write the cache date to disk using serialization.
     * @param context The ServletContext to use for logging.
     */
    public static void writeStats(ServletContext context) {
        //context.log("Stats.writeStats");
        try {
            ObjectOutputStream o = new ObjectOutputStream(
                                    new FileOutputStream(statsFile(context)));
            synchronized (instance) {
                instance.stopTime = new Date();
                o.writeObject(instance);
            }
            o.close();
        } catch (Exception e) {
            context.log("GWebCache: writeStats", e);
        }
    }

    /**
     * Read the cache data from disk using serialization. If an
     * error occurs, instanciate an empty cache.
     * @param context The ServletContext to use for logging.
     */
    public static void readStats(ServletContext context) {
        //context.log("Stats.readStats");
        Stats newStats = null;
        try {
            ObjectInputStream i = new ObjectInputStream(
                                    new FileInputStream(statsFile(context)));
            newStats = (Stats)i.readObject();
            i.close();
        } catch (FileNotFoundException e) {
            context.log("GWebCache: no stats file");
        } catch (Exception e) {
            context.log("GWebCache: readStats", e);
        }
        if (newStats == null){
            instance = new Stats();
        } else {
            synchronized (instance) {
                instance = newStats;
                Date now = new Date();
                //We only update it if we were down for more then 1 minutes
                //So if we reload a new version, this don't restart everything
                if (now.getTime() >= newStats.stopTime.getTime() + 1000 * 60) {
                    instance.startTime = now;
                    //We bump all hour that we were down
                    long hour = instance.lastHour * Data.MILLIS_PER_HOUR
                    + Data.MILLIS_PER_HOUR;
                    while(hour < now.getTime()){
                        instance.bumpHour(hour);
                        hour += Data.MILLIS_PER_HOUR;
                    }
                }                  
            }
        }
    }
}
