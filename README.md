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
$ ./gradlew build deployNodes
```

This generates in the `learning-corda/build/nodes` 3 directories for `PartyA`, `PartyB` and `Notary` nodes

### Start the nodes

```bash
$ build/nodes/runnodes
```
 
You might get the following error message if you installed `java` on Mac via `sdkman` 

```bash
Unable to find any JVMs matching version "1.8".
No Java runtime present, try --request to install.
```
 
In order to fix it just change this line in the `runnodes` script

```bash
/usr/libexec/java_home -v 1.8 --exec java -jar runnodes.jar "$@"
```

to 

```bash
java -jar runnodes.jar "$@"
```

The 3 nodes will start up in new terminals.

```bash
Advertised P2P messaging addresses      : localhost:10002
RPC connection address                  : localhost:10003
RPC admin connection address            : localhost:10043
Loaded 0 CorDapp(s)                     : 
Node for "Notary" started up and registered in 36.41 sec
...

Advertised P2P messaging addresses      : localhost:10005
RPC connection address                  : localhost:10006
RPC admin connection address            : localhost:10046
Loaded 0 CorDapp(s)                     : 
Node for "PartyA" started up and registered in 32.68 sec
...

Advertised P2P messaging addresses      : localhost:10008
RPC connection address                  : localhost:10009
RPC admin connection address            : localhost:10049
Loaded 0 CorDapp(s)                     : 
Node for "PartyB" started up and registered in 32.09 sec
``` 

When started via the command line, each node will display an interactive shell:

### Client

`clients/src/main/kotlin/com/template/Client.kt` defines a simple command-line client that connects to a node via RPC 
and prints a list of the other nodes on the network.

You can use this shell to interact with your node. For example, enter `run networkMapSnapshot` to see a list of 
the other nodes on the network:

```bash
- addresses:
  - "localhost:10008"
  legalIdentitiesAndCerts:
  - "O=PartyB, L=New York, C=US"
  platformVersion: 4
  serial: 1574110199770
- addresses:
  - "localhost:10002"
  legalIdentitiesAndCerts:
  - "O=Notary, L=London, C=GB"
  platformVersion: 4
  serial: 1574110193975
- addresses:
  - "localhost:10005"
  legalIdentitiesAndCerts:
  - "O=PartyA, L=London, C=GB"
  platformVersion: 4
  serial: 1574110199669
```

### Endpoints

#### /me

```bash
$ http :8080/me

O=PartyA, L=London, C=GB
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
            "country": "GB",
            "locality": "London",
            "organisation": "PartyA",
            "organisationUnit": null,
            "state": null,
            "x500Principal": {
                "encoded": "MC8xCzAJBgNVBAYTAkdCMQ8wDQYDVQQHDAZMb25kb24xDzANBgNVBAoMBlBhcnR5QQ==",
                "name": "O=PartyA,L=London,C=GB"
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
            "country": "US",
            "locality": "New York",
            "organisation": "PartyB",
            "organisationUnit": null,
            "state": null,
            "x500Principal": {
                "encoded": "MDExCzAJBgNVBAYTAlVTMREwDwYDVQQHDAhOZXcgWW9yazEPMA0GA1UECgwGUGFydHlC",
                "name": "O=PartyB,L=New York,C=US"
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

When running via `runnodes` this gives currently the following error (not sure currently why)

```bash
E 11:53:00 47 RestController.createIOU - Cannot find contract attachments for com.template.contracts.IOUContractnull. See https://docs.corda.net/api-contract-constraints.html#debugging
```

When running via IntelliJ the nodes it works.

```
$ echo '{"iouValue":5, "partyName": "O=PartyB, L=New York, C=US"}' | http post :8080/ious

Transaction committed to ledger.

$ http :8080/ious

[
    {
        "amount": 5,
        "borrower": "O=PartyB, L=New York, C=US",
        "lender": "O=PartyA, L=London, C=GB"
    }
]
```

### Running the nodes from IntelliJ (alternative to deployNodes)

First get the quasar library. This created a folder `lib` with the `quasar.jar` artifact inside it.
```bash
$ ./gradlew install installQuasar 
```

Run the `NodeDriver.kt` from the `workflows` module. This will bootstrap the 3 nodes within a single JVM.
