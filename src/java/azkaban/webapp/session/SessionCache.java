package azkaban.webapp.session;

import azkaban.utils.Props;
import net.sf.ehcache.Cache;
import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Element;
import net.sf.ehcache.config.CacheConfiguration;
import net.sf.ehcache.store.MemoryStoreEvictionPolicy;

/**
 * Cache for web session.
 * 
 * The following global azkaban properties can be used:
 * max.num.sessions - used to determine the number of live sessions that azkaban will handle. Default is 10000
 * session.time.to.live -Number of seconds before sessione expires. Default set to 3 days.
 */
public class SessionCache {
    private static final int MAX_NUM_SESSIONS = 10000;
    private static final int SESSION_TIME_TO_LIVE = 86400*3;
    private CacheManager manager = CacheManager.create();
    private Cache cache;

    /**
     * Constructor taking global props.
     * @param props
     */
    public SessionCache(Props props) {
        CacheConfiguration config = new CacheConfiguration();
        config.setName("sessionCache");
        config.setMaxEntriesLocalHeap(props.getInt("max.num.sessions",
                MAX_NUM_SESSIONS));
        config.setTimeToLiveSeconds(props.getInt("session.time.to.live",
                SESSION_TIME_TO_LIVE));
        config.eternal(false);
        config.diskPersistent(false);
        config.memoryStoreEvictionPolicy(MemoryStoreEvictionPolicy.LRU);

        cache = new Cache(config);
        manager.addCache(cache);
    }

    /**
     * Returns the cached session using the session id.
     * @param sessionId
     * @return
     */
    public Session getSession(String sessionId) {
        Element elem = cache.get(sessionId);
        if (elem == null) {
            return null;
        }

        return (Session) elem.getObjectValue();
    }

    /**
     * Adds a session to the cache. Accessible through the session ID.
     * 
     * @param id
     * @param session
     */
    public void addSession(Session session) {
        Element elem = new Element(session.getSessionId(), session);
        cache.put(elem);
    }

    /**
     * Removes the session from the cache.
     * @param id
     * @return
     */
    public boolean removeSession(String id) {
        return cache.remove(id);
    }
}