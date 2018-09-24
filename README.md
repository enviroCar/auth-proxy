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
    JAVA_OPTS="-Djava.security.egd=file:/dev/./urandom"
    RUN_ARGS="--server.servlet.contextPath=/auth-proxy  --logging.file=/var/log/auth-proxy/auth-proxy.log"
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
 1. Configure proxy in nginx via `/etc/nginx/sites-enabled/envirocar` (similar to [docker nginx config](docker-nginx-proxy.conf))
    ```
    [...]
    upstream auth-proxy {
        server 127.0.0.1:9999 fail_timeout=0;
    }
    [...]
    location /auth-proxy {
        proxy_http_version    1.1;
        proxy_set_header      Connection "";
        proxy_set_header      X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header      Host $http_host;
        proxy_set_header      X-Forwarded-Proto https;
        proxy_redirect        off;
        proxy_connect_timeout 240;
        proxy_send_timeout    240;
        proxy_read_timeout    240;
        proxy_pass            http://auth-proxy;
    }
    [...]
    ```
