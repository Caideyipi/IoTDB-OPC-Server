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
A standalone OPC UA server designed for the Apache IoTDB OPC UA sink client, which is a classical OPC UA Server using Eclipse Milo with `addNodes` function implemented.

**The official document of OPC foundation:**
[OPC Foundation Services docs](https://www.opcfoundation.cn/developer-tools/specifications-unified-architecture/part-4-services)

## Quick Start

To start this server, you can simply run it in IDEA with program arguments, or package this and start in an outer environment.

```shell
# package
mvn clean package -P get-jar-with-dependencies

# cd directory
cd target

# run server
java -jar iotdb_opc_server-0.0.1-jar-with-dependencies.jar
```

You can use this SQL in an outer IoTDB in tree model to connect with this server.

```sql
IoTDB> create pipe opc ('sink'='opc-ua-sink', 'node-url'='opc.tcp://<your_ip>:12686/iotdb', 'with-quality'='true')
```
This SQL will push any IoTDB data to this server, which will reflect on IoTDB paths with the newest data.

## Parameter Description

The parameters as follows:

| Parameter                | Description                                      | Default                                |
|:-------------------------|:-------------------------------------------------|:---------------------------------------|
| -https_port              | Https Port                                       | 8443                                   |
| -tcp_port                | TCP Port.                                        | 12686                                  |
| -u,--user                | User name                                        | root                                   |
| -pw,--password           | Password                                         | root                                   |
| -enable_anonymous_access | Whether to enable anonymous access of OPC Server | true                                   |
| -security_dir            | Directory to store security certificates.        | ${user.home}/iotdb_opc_server_security |
| -help                    | Display this help message and exit.              | /                                      |

When you start the server, you can also inject some parameters into it, run this to see the details.

```shell
java -jar iotdb_opc_server-0.0.1-jar-with-dependencies.jar -help
```

## Supplement

You may use this server's add node function with other clients.

**Note: Currently, the 'add node' function only supports object nodes and measurement nodes.**
