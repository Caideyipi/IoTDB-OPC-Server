<!--

    Licensed to the Apache Software Foundation (ASF) under one
    or more contributor license agreements.  See the NOTICE file
    distributed with this work for additional information
    regarding copyright ownership.  The ASF licenses this file
    to you under the Apache License, Version 2.0 (the
    "License"); you may not use this file except in compliance
    with the License.  You may obtain a copy of the License at

        http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing,
    software distributed under the License is distributed on an
    "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
    KIND, either express or implied.  See the License for the
    specific language governing permissions and limitations
    under the License.

-->

# IoTDB-OPC-Server
The Outer OPC UA Server for Apache IoTDB OPC UA sink client, which is a classical OPC UA Server using Eclipse Milo with `addNodes` function implemented. The docs can refer to the official website
of OPC foundation: https://www.opcfoundation.cn/developer-tools/specifications-unified-architecture/part-4-services

To start this server, you can simply run it in IDEA with program arguments, or package this and start in an outer environment.

```shell
# package
mvn clean package -P get-jar-with-dependencies
```
```shell
# cd directory
cd target
```
```shell
# run server
java -jar iotdb_opc_server-0.0.1-jar-with-dependencies.jar
```

When you start the server, you can also inject some parameters into it, run this to see the details.

```shell
# run server
java -jar iotdb_opc_server-0.0.1-jar-with-dependencies.jar -help
```
Then the output is as follows:
```text
For more information, please check the following hint.
usage: java -jar iotdb_opc_server-0.0.1-jar-with-dependencies.jar
       [-enable_anonymous_access] [-help] [-https_port <https_port>] [-pw <password>]
       [-security_dir <security_dir>] [-tcp_port <tcp_port>] -u <username>
 -enable_anonymous_access       Whether to enable anonymous access of this server.
                                Default is true. (optional)
 -help                          Display help information. (optional)
 -https_port <https_port>       Https Port. Default is 8443. (optional)
 -pw,--password <password>      Password. Default is root. (optional)
 -security_dir <security_dir>   Security directory of OPC Server. Default is
                                C:\Users\13361\iotdb_opc_server_security. (optional)
 -tcp_port <tcp_port>           TCP Port. Default is 12686. (optional)
 -u,--user <username>           User name, default is root. (optional)
```


You can use this SQL in an outer IoTDB in tree model to connect with this server.

```sql
IoTDB> create pipe opc ('sink'='opc-ua-sink', 'node-url'='<node-url specified>', 'with-quality'='true')
```
This SQL will push any IoTDB data to this server, which will reflect on IoTDB paths with the newest data. 

**NOTE: You may use this server's add node function with other clients, but currently this just supports object nodes and measurement nodes.**
