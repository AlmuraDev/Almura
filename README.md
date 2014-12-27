Almura
=============

## Prerequisites
* [Java] 7
* [Gradle] 2.1+

## Cloning
If you are using Git, use this command to clone the project: `git clone git@github.com:AlmuraDev/Almura.git`

## Setup
__Note:__ If you do not have [Gradle] installed then use ./gradlew for Unix systems or Git Bash and gradlew.bat for Windows systems in place of any 'gradle' command.

__For [Eclipse]__  
  1. Run `gradle setupDecompWorkspace --refresh-dependencies`  
  2. Make sure you have the Gradle plugin installed (Help > Eclipse Marketplace > Gradle Integration Plugin)  
  3. Import Almura as a Gradle project (File > Import)
  4. Select the root folder for Almura and click **Build Model**
  5. Check Almura when it finishes building and click **Finish**

__For [IntelliJ]__  
  1. Run `gradle setupDecompWorkspace --refresh-dependencies`  
  2. Make sure you have the Gradle plugin enabled (File > Settings > Plugins).  
  3. Click File > Import Module and select the **build.gradle** file for Almura.

## Running
__Note 1:__ The following is aimed to help you setup run configurations for Eclipse and IntelliJ, if you do not want to be able to run Almura directly from your IDE then you can skip this.

__For [Eclipse]__  
  1. Go to **Run > Run Configurations**.  
  2. Right-click **Java Application** and select **New**.  
  3. Set the current project.  
  4. Set the name as `Almura (Client)` and apply the information for Client below.
  5. Repeat step 1 through 4, then set the name as `Almura (Server)` and apply the information for Server below.  
  5a. When launching the server for the first time, it will shutdown by itself. You will need to modify the server.properties to set onlinemode=false and modify the eula.txt to set eula=true (this means you agree to the Mojang EULA, if you do not wish to do this then you cannot run the server).


__For [IntelliJ]__  
  1. Go to **Run > Edit Configurations**.  
  2. Click the green + button and select **Application**.  
  3. Set the name as `Almura (Client)` and apply the information for Client below.  
  4. Repeat step 2 and set the name as `Almura (Server)` and apply the information for Server below.  
  4a. When launching the server for the first time, it will shutdown by itself. You will need to modify the server.properties to set onlinemode=false and modify the eula.txt to set eula=true (this means you agree to the Mojang EULA, if you do not wish to do this then you cannot run the server).

__Client__

|     Property      | Value                                                      |
|:-----------------:|:-----------------------------------------------------------|
|    Main class     | GradleStart                                                |
|    VM options     | -Dfml.coreMods.load=net.malisis.core.asm.MalisisCorePlugin |
| Working directory | ~/run (Included in project)                         |
| Module classpath  | Almura (IntelliJ Only)                                     |

__Server__

|     Property      | Value                              |
|:-----------------:|:-----------------------------------|
|    Main class     | GradleStartServer                  |
| Working directory | ~/run (Included in project) |
| Module classpath  | Almura (IntelliJ Only)             |


## Building
__Note:__ If you do not have [Gradle] installed then use ./gradlew for Unix systems or Git Bash and gradlew.bat for Windows systems in place of any 'gradle' command.

In order to build Almura you simply need to run the `gradle` command. You can find the compiled JAR files in `~/build/libs` but in most cases you'll only need 'almura-x.x.x-SNAPSHOT.jar'.

## FAQ
__Why do I get `javac: source release 1.7 requires target release 1.7` in IntelliJ when running the client configuration?__
>Sometimes another project can mess with the settings in IntelliJ. Fixing this is relatively easy.

>1. Go to 'File > Settings'.
>2. Click the drop down for 'Compiler' on the left-hand side and select 'Java Compiler'.
>3. Select Obsidian and set the 'Target bytecode version' as '1.7'.
>4. Click Apply and OK and try running it again.

__Why do I get `Zip file rt.jar failed to read properly` in IntelliJ?__
>This is the result of Forge attempting to classload the Java runtime JAR, overall it is not an error that will cause any harm to your development and should be ignored.

__A dependency was added, but my IDE is missing it! How do I add it?__
>If a new dependency was added, you can just restart your IDE and the Gradle plugin for that IDE should pull in the new dependencies.

__Help! Things are not working!__
>Some issues can be resolved by deleting the '.gradle' folder in your user directory and running through the setup steps again, or even running `gradle cleanCache` and running through the setup again. Otherwise if you are having trouble with something that the README does not cover, feel free to join our IRC channel and ask for assistance.

[Eclipse]: http://www.eclipse.org/
[Gradle]: http://www.gradle.org/
[IntelliJ]: http://www.jetbrains.com/idea/
[Java]: http://java.oracle.com/
