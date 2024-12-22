From Mysql 8.4 on the password alogrithm `mysql_native_password` is no longer the default, it is replaced by `caching_sha2_password`. 
The mysql-conntector-j plugin used to connect a spring project to the mysql db still uses `mysql_native_password` though. 

Fix: 
- make mysql load the `mysql_native_password` plugin using this flag: <code>--mysql-native-password=ON</code>
    - in docker compose
        <code>
            image: mysql:8.4.2 # any version from 8.4 on
            ...
            command: --mysql-native-password=ON
        </code>
- make sure to use the right mysql-connector-j version, e.g. <code>runtimeOnly 'com.mysql:mysql-connector-j:8.4.0'</code> (see mvn repo for versions).
  If you ommit this, "latest" is used, which means mysql 9+ which does not support `mysql_native_password` at all
- make sure the mysql user you use uses the `mysql_native_password` plugin. 
    - to view users with plugin and host:
        - <code>mysql -u root -p [root_password]</code>
        - <code>select user, plugin, host from mysql.user;</code>
    - To change your users plugin: <code>alter user 'your_user'@'your_users_host' identified with mysql_native_password by 'your_password';</code>
<br>

## Update 22.12.2024
- apparently everything works fine without above fix... Don't delete this file though, just in case