# Cloud-Simulator
Create cloud simulators in Scala for evaluating executions of applications in cloud datacenters with different characteristics and deployment models.

---
Name: Karan Malhotra
---

### Installations
+ Install [Simple Build Toolkit (SBT)](https://www.scala-sbt.org/1.x/docs/index.html)
+ Ensure you can create, compile and run Java and Scala programs.

### Development Environment
+ Windows 10
+ Java 11.0.11
+ Scala 3.0.2
+ SBT Script Version 1.5.5
+ IntelliJ IDEA Ultimate

### Steps to Run the Application
+ Clone the repository
+ Navigate to the project directory using a terminal
+ Execute the below commands via terminal
```
sbt clean compile test
```
```
sbt run
```

### Test Cases
1. `testCheckConfig` tests if the configs are loaded or whether they're null.
2. `testHostCount` tests if the number of hosts created are equal to the number of hosts specified in the config.
3. Similar tests are done for `testVmCount` and `testCloudletsCount`.
4. `testHostAndVMConfig` tests if the resources of the hosts are sufficient to allocate the VMs.
5. `testCost` checks whether the total costs of simulation through either Service is > 0.

