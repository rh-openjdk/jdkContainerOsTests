# jdkContainerOsTests

This repository holds the collection of tests run against Red Hat's Build of OpenJDK Containers on different versions of Red Hat Enterprise Linux (RHEL). The supported version of RHEL are 7.9, 8.8 and 9.2

Supported Containers:

| Operating System version | OpenJDK Version | Runtime of Full JDK | Link to Container                                                                                                                    |
|--------------------------|-----------------|---------------------|--------------------------------------------------------------------------------------------------------------------------------------|
| Rhel 7                   | 8               | Full JDK            | [openjdk18-openshift](https://catalog.redhat.com/software/containers/redhat-openjdk-18/openjdk18-openshift/58ada5701fbe981673cd6b10) |
| Rhel 7                   | 11              | Full JDK            | [openjdk-11-rhel7](https://catalog.redhat.com/software/containers/openjdk/openjdk-11-rhel7/5bf57185dd19c775cddc4ce5)                 |
| UBI 8                    | 8               | Full JDK            | [openjdk-8](https://catalog.redhat.com/software/containers/ubi8/openjdk-8/5dd6a48dbed8bd164a09589a)                                  |
| UBI 8                    | 11              | Full JDK            | [openjdk-11](https://catalog.redhat.com/software/containers/ubi8/openjdk-11/5dd6a4b45a13461646f677f4)                                |
| UBI 8                    | 17              | Full JDK            | [openjdk-17](https://catalog.redhat.com/software/containers/ubi8/openjdk-17/618bdbf34ae3739687568813)                                |
| UBI 8                    | 8               | Runtime             | [openjdk-8-runtime](https://catalog.redhat.com/software/containers/ubi8/openjdk-8-runtime/6048ed07dbb14c0b8248bdc4)                  |
| UBI 8                    | 11              | Runtime             | [openjdk-11-runtime](https://catalog.redhat.com/software/containers/ubi8/openjdk-11-runtime/606dcb7d0f75e8ece4deec1f)                |
| UBI 8                    | 17              | Runtime             | [openjdk-17-runtime](https://catalog.redhat.com/software/containers/ubi8/openjdk-17-runtime/618bdc5f843af1624c4e4ba8)                |
| UBI 9                    | 11              | Full JDK            | [openjdk-11](https://catalog.redhat.com/software/containers/ubi9/openjdk-11/61ee7bafed74b2ffb22b07ab)                                |
| UBI 9                    | 17              | Full JDK            | [openjdk-17](https://catalog.redhat.com/software/containers/ubi9/openjdk-17/61ee7c26ed74b2ffb22b07f6)                                |
| UBI 9                    | 11              | Runtime             | [openjdk-11-runtime](https://catalog.redhat.com/software/containers/ubi9/openjdk-11-runtime/61ee7d1c33f211c45407a91c)                |
| UBI 9                    | 17              | Runtime             | [openjdk-17-runtime](https://catalog.redhat.com/software/containers/ubi9/openjdk-17-runtime/61ee7d45384a3eb331996bee)                |

How to run the container testsuite.

TODO

## Cryptographic Algorithms/Providers Testing
The tests 700 - 705 are made for testing whether the Red Hat's Build of OpenJDK containers are FIPS-compatible and behave as expected (for example when setting FIPS in the container manually).
The main purpose of these tests, however, is to assure that, in the containers, there is the correct set of cryptographic algorithms and providers available.

The main file that is concerned with this testing is `containersQa/algorithmTesting/CheckAlgorithms.java`.
(Note that this file can also work on its own - even outside of containers, but it needs `containersQa/algorithmTesting/cipherList.java` to work.)
It was originally a part of the `rhqe` test suite, but now, it is located here.
As its name says, it is used for checking algorithms (and providers).
It can either just list the algorithms/providers or assert them against a config file.
Running the class with `--help` brings up a help message explaining the different arguments.

There are currently 4 different config files:

| Config File                                         | Description                             |
|-----------------------------------------------------|-----------------------------------------|
| `containersQa/algorithmTesting/el8ConfigFips.txt`   | Used on RHEL 8 hosts set to FIPS mode.  |
| `containersQa/algorithmTesting/el8ConfigLegacy.txt` | Used on RHEL 8 hosts without FIPS mode. |
| `containersQa/algorithmTesting/el9ConfigFips.txt`   | Used on RHEL 9 hosts set to FIPS mode.  |
| `containersQa/algorithmTesting/el9ConfigLegacy.txt` | Used on RHEL 9 hosts without FIPS mode. |

It should be straightforward to create new config files for these tests as needed - it is explained in the help message of `algorithmTesting.CheckAlgorithms`. 
