package com.antdevrealm.housechaosmain.security.ratelimit;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "rate-limiting")
public class RateLimitProperties {

    private boolean enabled = true;
    private Endpoint login   = new Endpoint(5,  60);
    private Endpoint refresh = new Endpoint(20, 60);

    public static class Endpoint {
        private long capacity;
        private long refillSeconds;

        public Endpoint() {}

        public Endpoint(long capacity, long refillSeconds) {
            this.capacity      = capacity;
            this.refillSeconds = refillSeconds;
        }

        public long getCapacity()      { return capacity; }
        public long getRefillSeconds() { return refillSeconds; }
        public void setCapacity(long capacity)           { this.capacity      = capacity; }
        public void setRefillSeconds(long refillSeconds) { this.refillSeconds = refillSeconds; }
    }

    public boolean isEnabled()  { return enabled; }
    public Endpoint getLogin()   { return login; }
    public Endpoint getRefresh() { return refresh; }

    public void setEnabled(boolean enabled)   { this.enabled = enabled; }
    public void setLogin(Endpoint login)       { this.login   = login; }
    public void setRefresh(Endpoint refresh)   { this.refresh = refresh; }
}
