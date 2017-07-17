# Configuration

<sup>:paperclip:</sup>*Whenever OpenOffice.org (OOo for short) is mentioned, this can generally be interpreted to include any office suite derived from OOo such as [Apache OpenOffice](https://www.openoffice.org) and [LibreOffice](https://www.libreoffice.org).*
<br />
<sup>:paperclip:</sup>*JODConverter uses milliseconds for all time values.*

When using a DefaultOfficeManager, there are a number of settings that can be configured. Some of the default settings used by JODConverter have been chosen because they have a greater chance of working out of the box, but they are not necessarily the optimal ones.

##### :1234:``portNumbers``&nbsp;&nbsp;and/or&nbsp;&nbsp;:capital_abcd:``pipeNames``
OOo inter-process communication can use either TCP sockets and/or named pipes. Named pipes have the advantage of not taking up TCP ports (with their potential security implications), and they are marginally faster. However they require a native library to be loaded by the JVM, and this means having to set the `java.library.path` system property. That's why it's not the default. The path that needs to be added to `java.library.path` is different depending on the platform, but it should be the directory in the OOo installation containing libjpipe.

- On Linux it's e.g.: java -Djava.library.path=/opt/openoffice.org/ure/lib
- On Windows it's e.g.: java "-Djava.library.path=C:\Program Files (x86)\OpenOffice 4\program"

&nbsp;*Default*: TCP socket, on port 2002.

```java
// This example will use 4 TCP ports, which will cause
// JODConverter to start 4 office processes when the
// OfficeManager will be started.
OfficeManager officeManager =
    DefaultOfficeManager.builder()
        .portNumbers(2002, 2003, 2004, 2005);
        .build();
```

##### :file_folder:``officeHome``
This property sets the office home directory of the office installation that will be used to perform document conversions.

&nbsp;*Default*: Auto-detected, starting with LibreOffice (over OpenOffice) and the most recent version. 

```java
// This example will force JODConverter to use the OpenOffice 4
// installation that can be found using the specified path.
OfficeManager officeManager =
    DefaultOfficeManager.builder()
        .officeHome("D:\\Program Files (x86)\\OpenOffice 4");
        .build();
```

##### :capital_abcd:``processManager``
A process manager is used when JODConverter needs to deal with a started office process. When JODConverter starts an office process, it must retrieve the PID of the started process in order to be able to kill it later if required.

&nbsp;*Default*: By default, JODConverter will try to find the best process manager according to the OS on which JODConverter is running. But any process manager implementing the ProcessManager interface can be used if found on the classpath.

```java
// This example will create an instance of the com.example.foo.CustomProcessManager
// class that will be used buy the created OfficeManager.
OfficeManager officeManager =
    DefaultOfficeManager.builder()
        .processManager("com.example.foo.CustomProcessManager")
        .build();
```
            
##### :file_folder:``workingDir``
This property sets the directory where temporary office profile directories will be created. An office profile directory is created per office process launched. This property will also be used to create a temporary directory where files will be created when conversions are done using InputStream/OutputStream.

&nbsp;*Default*: The system temporary directory as specified by the `java.io.tmpdir` system property.

##### :file_folder:``templateProfileDir``
A DefaultOfficeManager creates temporary profile directories for its OOo processes, to avoid interfering with e.g. another OOo instance being used by the user. Using this property, you can provide a template profile directory containing customized settings. The OfficeManager will copy such template directory to the temporary profile, so OOo will use the same settings while still keeping the OOo instances separate.

A profile can be customized in OOo by selecting the Tools > Options menu item. Settings that may be worth customizing for automated conversions include e.g.

- Load/Save > General: you may e.g. want to disable "Save URLs relative to Internet" for security reasons
- Load/Save > Microsoft Office: these options affect conversions of embedded documents, e.g. an Excel table contained in a Word document. If not enabled, the embedded table will likely be lost when converting the Word document to another format.

&nbsp;*Default*: By default, this temporary profile will be a new one created by OOo with its own defaults settings, and relies on the [-nofirststartwizard](https://wiki.openoffice.org/wiki/Framework/Article/Command_Line_Arguments) command line option.

##### :white_check_mark:``killExistingProcess``
This property specifies whether an existing office process is killed when starting a new office process for the same connection string.

&nbsp;*Default*: true.

##### :watch:``processTimeout``
This property sets the timeout, in milliseconds, when trying to execute an office process call (start/terminate).

&nbsp;*Default*: 120000 (2 minutes)

##### :watch:``processRetryInterval``
This property sets the delay, in milliseconds, between each try when trying to execute an office process call (start/terminate).

&nbsp;*Default*: 250 (0.25 seconds)

##### :watch:``taskExecutionTimeout``
This property sets the maximum time allowed to process a task. If the processing time of a task is longer than this timeout, this task will be aborted and the next task is processed.

&nbsp;*Default*: 120000 (2 minutes)

##### :1234:``maxTasksPerProcess``
This property sets the maximum number of tasks an office process can execute before restarting.

&nbsp;*Default*: 200

##### :watch:``taskQueueTimeout``
This property is used to set the maximum living time of a task in the conversion queue. The task will be removed from the queue if the waiting time is longer than this timeout and an OfficeException will be thrown.

&nbsp;*Default*: 30000 (30 seconds)