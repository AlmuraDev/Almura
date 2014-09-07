AlmuraMod
=============

## Prerequisites
* [Java] 7
* [Gradle] 2.0+

## Cloning
If you are using Git, use this command to clone the project: `git clone git@github.com:AlmuraDev/AlmuraMod.git`

## Setup
__Note:__ If you do not have [Gradle] installed then use ./gradlew for Unix systems or Git Bash and gradlew.bat for Windows systems in place of any 'gradle' command.

__For [Eclipse]__  
  1. Run `gradle clean cleanCache`  
  2. Run `gradle setupDecompWorkspace --refresh-dependencies`  
  3. Make sure you have the Gradle plugin installed (Help > Eclipse Marketplace > Gradle Integration Plugin)  
  4. Import AlmuraMod as a Gradle project (File > Import)
  5. Select the root folder for AlmuraMod and click **Build Model**
  6. Check AlmuraMod when it finishes building and click **Finish**

__For [IntelliJ]__  
  1. Run `gradle clean cleanCache`  
  2. Run `gradle setupDecompWorkspace --refresh-dependencies`  
  3. Make sure you have the Gradle plugin enabled (File > Settings > Plugins).  
  4. Click File > Import Module and select the **build.gradle** file for AlmuraMod.

## Running
__Note 1:__ The following is aimed to help you setup run configurations for Eclipse and IntelliJ, if you do not want to be able to run AlmuraMod directly from your IDE then you can skip this.
__Note 2:__ When running the Server, make sure you set it to *__online-mode=false__* in the server.properties in ~/run/server!

__For [Eclipse]__  
  1. Go to **Run > Run Configurations**.  
  2. Right-click **Java Application** and select **New**.  
  3. Set the current project.  
  4. Set the name as `AlmuraMod (Client)` and apply the information for Client below.
  5. Repeat step 1 through 4, then set the name as `AlmuraMod (Server)` and apply the information for Server below.

__For [IntelliJ]__  
  1. Go to **Run > Edit Configurations**.  
  2. Click the green + button and select **Application**.  
  3. Set the name as `AlmuraMod (Client)` and apply the information for Client below.  
  4. Repeat step 2 and set the name as `AlmuraMod (Server)` and apply the information for Server below.  
  4a. When launching the server for the first time, it will shutdown by itself. You will need to modify the server.properties to set onlinemode=false and modify the eula.txt to set eula=true (this means you agree to the Mojang EULA, if you do not wish to do this then you cannot run the server).

__Client__

|     Property      | Value                                     |
|:-----------------:|:------------------------------------------|
|    Main class     | GradleStart                               |
|    VM options     | -Djava.library.path="../../build/natives" |
| Working directory | ~/run/client (Included in project)                                                                      |
| Module classpath  | AlmuraMod (IntelliJ Only)                                                                                |

__Server__

|     Property      | Value                              |
|:-----------------:|:-----------------------------------|
|    Main class     | GradleStartServer                  |
| Working directory | ~/run/server (Included in project) |
| Module classpath  | AlmuraMod (IntelliJ Only)           |


## Building
__Note:__ If you do not have [Gradle] installed then use ./gradlew for Unix systems or Git Bash and gradlew.bat for Windows systems in place of any 'gradle' command.

In order to build AlmuraMod you simply need to run the `gradle` command. You can find the compiled JAR files in `~/build/libs` but in most cases you'll only need 'almuramod-x.x.x-SNAPSHOT.jar'.

[Eclipse]: http://www.eclipse.org/
[Gradle]: http://www.gradle.org/
[IntelliJ]: http://www.jetbrains.com/idea/
[Java]: http://java.oracle.com/
