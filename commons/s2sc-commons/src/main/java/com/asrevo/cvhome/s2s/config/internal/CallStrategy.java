package com.asrevo.cvhome.s2s.config.internal;

public enum CallStrategy {
    DEFAULT,
    DIRECT, // used when service call another service directly when it knows its url and port
    GATEWAY, // used when service call another service throw gateway , so it doesn't know service
    // port it just knows its gateway url and port and the service name
    LB, // used when service call another service throw Load balancer, so it doesn't know service
    // port it just knows load balancer url and port and the service name
    IGNORE // ignore calling a service and use static fallback value in code
}
