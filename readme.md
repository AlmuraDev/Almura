Almura
=============

## Prerequisites
* [Java] 8
* [Gradle] 3.5+

## Cloning
The following steps will ensure your project is cloned properly.  
1. `git clone git@github.com:AlmuraDev/Almura.git`  
2. `cd Almura`  
3. `cp scripts/pre-commit .git/hooks`

## Setup
__Note:__ If you do not have [Gradle] installed then use ./gradlew for Unix systems or Git Bash and gradlew.bat for Windows systems in place of any 'gradle' command.

__For [IntelliJ]__  
  1. Run `gradle generateDatabaseClasses`  
  2. Run `gradle setupDecompWorkspace --refresh-dependencies`  
  3. Make sure you have the Gradle plugin enabled (File > Settings > Plugins).  
  4. Click File > Import Module and select the **build.gradle** file for Almura.
  5. On the import screen, uncheck `Create separate module per source set`

## Building
__Note:__ If you do not have [Gradle] installed then use ./gradlew for Unix systems or Git Bash and gradlew.bat for Windows systems in place of any 'gradle' command.

In order to build Almura you simply need to run the `gradle` command. You can find the compiled JAR files in `./build/libs` but in most cases you'll only need 'almura-x.x-xxxx-rx.x.jar'.

## Running (Manual Configuration)
__Note 1:__ The following is aimed to help you setup run configurations for Eclipse and IntelliJ. If you do not want to be able to run Almura directly from your IDE then you can skip this.  
__Note 2:__ For more information regarding VM options or program arguments for Mixin, visit https://github.com/SpongePowered/Mixin/wiki/Mixin-Java-System-Properties

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

|     Property      | Value                                                                                                              |
|:-----------------:|:-------------------------------------------------------------------------------------------------------------------|
|    Main class     | GradleStart                                                                                                        |
|    VM options     | -Xincgc -Xms1024M -Xmx2048M -Dfml.coreMods.load=com.almuradev.almura.AlmuraLoadingPlugin,                          |
| Program arguments | --noCoreSearch                                                                                                     |
| Working directory | ./run (Included in project)                                                                                        |
| Module classpath  | Almura (IntelliJ Only)                                                                                             |

__Server__

|     Property      | Value                                                                                                              |
|:-----------------:|:-------------------------------------------------------------------------------------------------------------------|
|    Main class     | GradleStartServer                                                                                                  |
|    VM options     | -Xincgc -Xms1024M -Xmx2048M -Dfml.coreMods.load=com.almuradev.almura.AlmuraLoadingPlugin,                          |
| Program arguments | --noCoreSearch                                                                                                     |
| Working directory | ./run (Included in project)                                                                                        |
| Module classpath  | Almura (IntelliJ Only)                                                                                             |

Advanced Logging -> Program Arguments: -Dlog4j.configurationFile=/path/to/log4j2.xml

[Eclipse]: http://www.eclipse.org/
[Gradle]: http://www.gradle.org/
[IntelliJ]: http://www.jetbrains.com/idea/
[Java]: http://java.oracle.com/

Disable Stupid Gradle Daemon:  [run this in cmd.exe]
(if not exist "%USERPROFILE%/.gradle" mkdir "%USERPROFILE%/.gradle") && (echo. >> "%USERPROFILE%/.gradle/gradle.properties" && echo org.gradle.daemon=false >> "%USERPROFILE%/.gradle/gradle.properties")

* Note; make sure IntelliJ is set to use itself for Compiling and not Gradle.  File > Settings > Build and run using, Run tests using.
