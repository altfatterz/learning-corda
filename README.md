### Learning Corda

This is repository was initially created from the [cordapp-template-kotlin](https://github.com/corda/cordapp-template-kotlin/) repository and modified following the [IOU](https://docs.corda.net/tutorials-index.html) tutorial

### Building the project

```bash
$ git clone https://github.com/altfatterz/learning-corda
$ cd learning-corda
$ ./gradlew clean build
```

### Deploy the nodes

```bash
$ ./gradlew deployNodes
```

This generates in the `learning-corda/build/nodes` 4 directories for `PartyA`, `PartyB`, `PartyC` and `Notary` nodes

### Start the nodes

```bash
$ build/nodes/runnodes
```
 
The 3 nodes will start up in new terminals.

```bash
Advertised P2P messaging addresses      : localhost:10001
RPC connection address                  : localhost:10002
RPC admin connection address            : localhost:10003
Loaded 2 CorDapp(s)                     : Contract CorDapp: iou-contract version 1 by vendor Corda Open Source with licence Apache License, Version 2.0, Workflow CorDapp: Workflows version 1 by vendor Corda Open Source with licence Apache License, Version 2.0 
Node for "Notary" started up and registered in 36.41 sec
...

Advertised P2P messaging addresses      : localhost:10011
RPC connection address                  : localhost:10012
RPC admin connection address            : localhost:10013
Loaded 2 CorDapp(s)                     : Contract CorDapp: iou-contract version 1 by vendor Corda Open Source with licence Apache License, Version 2.0, Workflow CorDapp: Workflows version 1 by vendor Corda Open Source with licence Apache License, Version 2.0 
Node for "PartyA" started up and registered in 32.68 sec
...

Advertised P2P messaging addresses      : localhost:10021
RPC connection address                  : localhost:10022
RPC admin connection address            : localhost:10023
Loaded 2 CorDapp(s)                     : Contract CorDapp: iou-contract version 1 by vendor Corda Open Source with licence Apache License, Version 2.0, Workflow CorDapp: Workflows version 1 by vendor Corda Open Source with licence Apache License, Version 2.0p(s)                     : 
Node for "PartyB" started up and registered in 32.09 sec
...

Advertised P2P messaging addresses      : localhost:10031
RPC connection address                  : localhost:10032
RPC admin connection address            : localhost:10033
Loaded 2 CorDapp(s)                     : Contract CorDapp: iou-contract version 1 by vendor Corda Open Source with licence Apache License, Version 2.0, Workflow CorDapp: Workflows version 1 by vendor Corda Open Source with licence Apache License, Version 2.0p(s)                     : 
Node for "PartyC" started up and registered in 32.09 sec
...
``` 

Check all nodes there is no `IOUState` yet:

```bash
$ run vaultQuery contractStateType: com.example.states.IOUState
```

You can use this shell to interact with your node. For example, enter `run networkMapSnapshot` to see a list of 
the other nodes on the network:

```bash
- addresses:
  - "localhost:10011"
  legalIdentitiesAndCerts:
  - "O=PartyA, L=London, C=GB"
  platformVersion: 4
  serial: 1579514846797
- addresses:
  - "localhost:10001"
  legalIdentitiesAndCerts:
  - "O=Notary, L=London, C=GB"
  platformVersion: 4
  serial: 1579514841953
- addresses:
  - "localhost:10031"
  legalIdentitiesAndCerts:
  - "O=PartyC, L=Zurich, C=CH"
  platformVersion: 4
  serial: 1579514846688
- addresses:
  - "localhost:10021"
  legalIdentitiesAndCerts:
  - "O=PartyB, L=New York, C=US"
  platformVersion: 4
  serial: 1579514846399

```

When started via the command line, each node will display an interactive shell:

### Command Line Client

`clients/src/main/kotlin/com/example/Client.kt` defines a simple command-line client that connects to a node via RPC 
and prints a list of the other nodes on the network.

### Web Client with the following endpoints:

The WebClient is a Spring Boot app which can be started with different profiles:

java -jar build/WebClient --spring.profiles.active=PartyA
java -jar build/WebClient --spring.profiles.active=PartyB
java -jar build/WebClient --spring.profiles.active=PartyC

#### /me

```bash
$ http :8080/me

O=PartyA, L=London, C=GB
```

```bash
$ http :8081/me

O=PartyB, L=New York, C=US
```

```bash
$ http :8082/me

O=PartyC, L=Zurich, C=CH
```

#### /peers

```bash
$ http :8080/peers
```

```json
{
    "peers": [
        {
            "commonName": null,
            "country": "US",
            "locality": "New York",
            "organisation": "PartyB",
            "organisationUnit": null,
            "state": null,
            "x500Principal": {
                "encoded": "MDExCzAJBgNVBAYTAlVTMREwDwYDVQQHDAhOZXcgWW9yazEPMA0GA1UECgwGUGFydHlC",
                "name": "O=PartyB,L=New York,C=US"
            }
        },
        {
            "commonName": null,
            "country": "GB",
            "locality": "London",
            "organisation": "Notary",
            "organisationUnit": null,
            "state": null,
            "x500Principal": {
                "encoded": "MC8xCzAJBgNVBAYTAkdCMQ8wDQYDVQQHDAZMb25kb24xDzANBgNVBAoMBk5vdGFyeQ==",
                "name": "O=Notary,L=London,C=GB"
            }
        },
        {
            "commonName": null,
            "country": "CH",
            "locality": "Zurich",
            "organisation": "PartyC",
            "organisationUnit": null,
            "state": null,
            "x500Principal": {
                "encoded": "MC8xCzAJBgNVBAYTAkNIMQ8wDQYDVQQHDAZadXJpY2gxDzANBgNVBAoMBlBhcnR5Qw==",
                "name": "O=PartyC,L=Zurich,C=CH"
            }
        },
        {
            "commonName": null,
            "country": "GB",
            "locality": "London",
            "organisation": "PartyA",
            "organisationUnit": null,
            "state": null,
            "x500Principal": {
                "encoded": "MC8xCzAJBgNVBAYTAkdCMQ8wDQYDVQQHDAZMb25kb24xDzANBgNVBAoMBlBhcnR5QQ==",
                "name": "O=PartyA,L=London,C=GB"
            }
        }
    ]
}
```

#### GET /ious

```
$ http :8080/ious

[]
```

#### GET /my-ious

```
http :8080/my-ious
```

#### POST /ious

PartyA lends 5 amount to partyB

```
$ echo '{"iouValue":5, "partyName": "O=PartyB, L=New York, C=US"}' | http post :8080/ious

Transaction committed to ledger.
```

Check the ledger on the `PartyA`, `PartyB`

```bash
$ run vaultQuery contractStateType: com.example.states.IOUState

states:
- state:
    data: !<com.example.states.IOUState>
      value: 5
      lender: "O=PartyA, L=London, C=GB"
      borrower: "O=PartyB, L=New York, C=US"
    contract: "com.example.contracts.IOUContract"
    notary: "O=Notary, L=London, C=GB"
    encumbrance: null
    constraint: !<net.corda.core.contracts.SignatureAttachmentConstraint>
      key: "aSq9DsNNvGhYxYyqA9wd2eduEAZ5AXWgJTbTEw3G5d2maAq8vtLE4kZHgCs5jcB1N31cx1hpsLeqG2ngSysVHqcXhbNts6SkRWDaV7xNcr6MtcbufGUchxredBb6"
  ref:
    txhash: "8267ED01EF0C2E124BDBE962FEAAFD63D606E295F5253234C177BEB2B52591BF"
    index: 0
statesMetadata:
- ref:
    txhash: "8267ED01EF0C2E124BDBE962FEAAFD63D606E295F5253234C177BEB2B52591BF"
    index: 0
  contractStateClassName: "com.example.states.IOUState"
  recordedTime: "2020-01-20T10:21:29.256Z"
  consumedTime: null
  status: "UNCONSUMED"
  notary: "O=Notary, L=London, C=GB"
  lockId: null
  lockUpdateTime: null
  relevancyStatus: "RELEVANT"
  constraintInfo:
    constraint:
      key: "aSq9DsNNvGhYxYyqA9wd2eduEAZ5AXWgJTbTEw3G5d2maAq8vtLE4kZHgCs5jcB1N31cx1hpsLeqG2ngSysVHqcXhbNts6SkRWDaV7xNcr6MtcbufGUchxredBb6"
totalStatesAvailable: -1
stateTypes: "UNCONSUMED"
otherResults: []
```

If you run this on the `PartyC` node then we can see, there are no states

```
$ run vaultQuery contractStateType: com.example.states.IOUState

states: []
statesMetadata: []
totalStatesAvailable: -1
stateTypes: "UNCONSUMED"
otherResults: []

```

### GET /my-ious

```bash
$ http :8080/my-ious

[
    {
        "amount": 5,
        "borrower": "O=PartyB, L=New York, C=US",
        "lender": "O=PartyA, L=London, C=GB"
    }
]
```

```bash
$ http :8081/my-ious
[]
```

```bash
$ http :8082/my-ious
[]
```

### Running the nodes from IntelliJ (alternative to deployNodes)

First get the quasar library. This created a folder `lib` with the `quasar.jar` artifact inside it.

```bash
$ ./gradlew install installQuasar 
```

Run the `NodeDriver.kt` from the `workflows` module. This will bootstrap the 4 nodes (`Notary`, `PartyA`, `PartyB`, `PartyC`) within a single JVM.
### Flow commands

```bash
$ flow list
$ flow start
```

Starting the `IOUFlow`:

```bash
$ flow start com.example.flows.IOUFlow iouValue: 50, otherParty: "O=PartyB,L=New York,C=US"

 ✅   Starting
          Requesting signature by notary service
              Requesting signature by Notary service
              Validating response from Notary service
     ✅   Broadcasting transaction to participants
➡️   Done
Flow completed with result: kotlin.Unit
```


```bash
$ flow start com.example.flows.IOUFlow iouValue: 200, otherParty: "O=PartyB,L=New York,C=US"

java.lang.IllegalArgumentException: Failed requirement: The IOU's value can't be too high.
```
