# Authentication Proxy

A simple authentication proxy for HTTP BasicAuth endpoints

# Running as a service

 * Documentation:
   * https://docs.spring.io/spring-boot/docs/current/reference/html/deployment-install.html#deployment-initd-service
   * https://docs.spring.io/spring-boot/docs/current/reference/html/deployment-install.html#deployment-script-customization


 1. Host System adjustments:
     1. Add user: `auth-proxy`:
        ```
        sudo adduser --system --group --no-create-home --home /opt/auth-proxy auth-proxy
        ```
     1. Add folders with owner and group `auth-proxy`:
       1. `/var/log/auth-proxy/`
       1. `/opt/auth-proxy`
 1. Build application with `mvn install`
 1. Copy `*.jar` from `./target/` folder to `Host System:/opt/auth-proxy/`
 1. Create symlink to generic `auth-proxy.jar`:
    ```
    sudo ln -sv /opt/auth-proxy/auth-proxy-0.0.1-SNAPSHOT.jar /opt/auth-proxy/auth-proxy.jar
    ```
 1. Create symlink in `/etc/init.d` with name `auth-proxy`.
 1. Create `/opt/auth-proxy/auth-proxy.conf` with the following content:
    ```
    MODE=service
    JAVA_OPTS=-Djava.security.egd=file:/dev/./urandom
    LOG_FILENAME=auth-proxy.log
    LOG_FOLDER=/var/log/auth-proxy
    ```
 1. Test service via `sudo /etc/init.d auth-proxy start|status|stop`
 1. Activate service on system level: `sudo update-rc.d auth-proxy defaults`
 1. Restart host system and check log if service is running: `sudo tail -f /var/log/auth-proxy/auth-proxy.log`
 1. Enable logrotation via `/etc/logrotate.d/auth-proxy`:
    ```
    /var/log/auth-proxy/auth-proxy.log {
      copytruncate
      weekly
      rotate 52
      compress
      missingok
      create 640 auth-proxy auth-proxy
    }
    ```
