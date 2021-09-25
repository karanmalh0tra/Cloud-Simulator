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

### Program Sequence
1. There are a total of 4 simulations. Each of them covers IaaS, PaaS, FaaS and SaaS service models respectively. The configurations for those service models are entered in the cloud-provider.conf by the cloud provider and cloud-customer.conf by the cloud consumer. A unique broker is assembled for each service model.
2.	Running the Simulation class runs all the above simulations one after the other.
3.  The abstraction for provider and consumer configs is provided through setting different config files for each. The consumer only has control over the cloud-customer.conf file and the provider has control over the cloud-provider.conf file.
4.	The logs are generated in the log directory of the project. They have a timestamp mentioned in the log files to figure out which log file to search the logs in.
5.	The same logs and the simulation output would also appear in the console/terminal on running `sbt run` command.

### Assumptions for Service Model
The following Assumptions are made to divide the access control between the Provider and the Customer for the various service models.
1. IaaS: The provider has control over the hardware only. The customer controls the VMs and the Cloudlets.
2. PaaS: The Provider controls the hardware and the VMs. The customer has control over the number of VMs and entire control over Cloudlets.
3. FaaS: The Provider controls the hardware, the VMs and cloudlets scheduling policies. The customer has access to the Cloudlets requirements and the number of cloudlets. This goes in line with just having a function created on cloud but depending on the function, the task(cloudlet) can be huge/small.
4. SaaS: The Provider has control over the hardware, the VMs, and the cloudlets specifications. The customer has access to the number of Cloudlets (number of instances of the SaaS software being used).
A network layer has been created and Network Bandwidth and Network Latency was specified to simulate the real-world scenario where allocations and scheduling takes time due to constraints in bandwidth and latency. Without specifying these, the cloudlets get scheduled readily which isn’t what 
really happens when we use real world Cloud Platforms.

### Costing based on Service Model
The customer is charged for the following:
1. Cost per second of use
2. Cost per memory use
3. Cost per storage used
4. Cost per bandwidth

Based on these, the costs of VMs, Cloudlets and Services offered by the Providers are calculated and charged to the consumer.
To charge the consumers for cloud usage, the following assumptions were made to differentiate costing between each Service Model(IaaS, PaaS, FaaS and SaaS)
1.	IaaS: The customer is charged for the usage of VMs and Cloudlets. This is because the Provider is merely responsible for having the hardware. The consumer decides on the number of VMs, the specifications and the consumption through cloudlets.
2.	PaaS: The customer is charged a fixed amount per Virtual Machine required. This is because the client can only manage the number of VMs they want. Besides this, the clients are charged entirely for the Cloudlets utilization.
3.	FaaS: The customers are charged a fixed amount for the usage of FaaS and charged entirely for the utilization through Cloudlets.
4.	SaaS: The customers are charged per cloudlet since the services are offered by the Cloud Provider and the customers merely use the application. This can also be free-to-use but assuming a Provider is only a SaaS provider, the providers only stream of revenue would be the utilization of Cloudlets by the consumer.

The logs print the total cost incurred by the customer so the providers can analyze this and judge if they’re profiting or making a loss by providing the different service models to the consumers.

### Allocation and Scheduling Policies Implemented
Various Allocation and Scheduling Policies are compared for VM and Cloudlets.
Changing configurations accommodates to changing policies in code.
The different VmAllocationPolicies covered are:
1.	RoundRobin
2.	BestFit
3.	FirstFit
4.	Simple (Default)

TimeShared and SpaceShared scheduling policies were covered for VMs and Cloudlets.

### Results
IaaS using RoundRobin VmAllocationPolicy, TimeShared Vm and Cloudlet Scheduling.
![IaaS using RoundRobin VmAllocationPolicy, TimeShared Vm and Cloudlet Scheduling](https://github.com/karanmalh0tra/Cloud-Simulator/blob/main/images/IaaS-1.png "IaaS using RoundRobin VmAllocationPolicy, TimeShared Vm and Cloudlet Scheduling.")

IaaS using BestFit VmAllocationPolicy, TimeShared Vm and Cloudlet Scheduling
![IaaS using BestFit VmAllocationPolicy, TimeShared Vm and Cloudlet Scheduling](https://github.com/karanmalh0tra/Cloud-Simulator/blob/main/images/IaaS-2.png "IaaS using BestFit VmAllocationPolicy, TimeShared Vm and Cloudlet Scheduling")

PaaS using WorstFit VmAllocationPolicy, TimeShared Vm and SpaceShared Cloudlet Scheduling
![PaaS using WorstFit VmAllocationPolicy, TimeShared Vm and SpaceShared Cloudlet Scheduling](https://github.com/karanmalh0tra/Cloud-Simulator/blob/main/images/PaaS-1.png "PaaS using WorstFit VmAllocationPolicy, TimeShared Vm and SpaceShared Cloudlet Scheduling")

PaaS using Simple VmAllocationPolicy, TimeShared Vm and SpaceShared Cloudlet Scheduling
![PaaS using Simple VmAllocationPolicy, TimeShared Vm and SpaceShared Cloudlet Scheduling](https://github.com/karanmalh0tra/Cloud-Simulator/blob/main/images/PaaS-2.png "PaaS using Simple VmAllocationPolicy, TimeShared Vm and SpaceShared Cloudlet Scheduling")

FaaS using WorstFit VmAllocationPolicy, TimeShared Vm and Cloudlet Scheduling
![FaaS using WorstFit VmAllocationPolicy, TimeShared Vm and Cloudlet Scheduling](https://github.com/karanmalh0tra/Cloud-Simulator/blob/main/images/FaaS-1.png "FaaS using WorstFit VmAllocationPolicy, TimeShared Vm and Cloudlet Scheduling")

SaaS using RoundRobin VmAllocationPolicy, TimeShared Vm and Cloudlet Scheduling and Cloudlet Count = 16
![SaaS using RoundRobin VmAllocationPolicy, TimeShared Vm and Cloudlet Scheduling and Cloudlet Count = 16](https://github.com/karanmalh0tra/Cloud-Simulator/blob/main/images/SaaS-1.png "SaaS using RoundRobin VmAllocationPolicy, TimeShared Vm and Cloudlet Scheduling and Cloudlet Count = 16")

SaaS using RoundRobin VmAllocationPolicy, TimeShared Vm and Cloudlet Scheduling and Cloudlet Count = 32
![SaaS using RoundRobin VmAllocationPolicy, TimeShared Vm and Cloudlet Scheduling and Cloudlet Count = 32](https://github.com/karanmalh0tra/Cloud-Simulator/blob/main/images/SaaS-2.PNG "SaaS using RoundRobin VmAllocationPolicy, TimeShared Vm and Cloudlet Scheduling and Cloudlet Count = 32")
