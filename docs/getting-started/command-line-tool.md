# Command Line Tool

The command line tool provides a good way to check that everything is working, i.e., that you have the right OOo version
installed etc. To convert a document, use the provided batch file, located in the bin directory of the cli module
distribution.

### Syntax

```
jodconverter-cli [options] infile outfile [infile outfile ...]
```

or

```
jodconverter-cli [options] -f output-format infile [infile ...]
```

### Parameters

#### infile

The input file to convert (required). When used with the **-f** switch, infile may contain wildcards to match multiple
files to convert. Thus, it is possible with the jodconverter-cli tool to convert more than 1 file at the time.

#### outfile

The target file which is the result of the conversion.

#### -a, --application-context `<file>`

Application context file (optional).

#### -c, --connection-url `<url>`

Remote LibreOffice Online server URL for conversion (optional).

#### -d, --output-directory `<dir>`

Output directory (optional; defaults to input directory).

#### -f, --output-format `<arg>`

Output format (e.g. pdf).

#### -h, --help

Displays help at the command prompt.

#### -i, --office-home `<dir>`

OOo home directory (optional; defaults to auto-detect).
See [Configuration](../../configuration/local-configuration#officehome).

#### -k, --keep-alive

Keep the office process alive on shutdown (optional; defaults to false).
See [Configuration](../../configuration/local-configuration#keepaliveonshutdown).

#### -l, --load-properties

Load properties (optional; eg. -lPassword=myPassword).

#### -m, --process-manager `<classname>`

Class name of the process manager to use (optional; defaults to auto-detect).
See [Configuration](../../configuration/local-configuration#processmanager).

#### -h, --host-name `<arg>`

Host name that will be used in the --accept argument when starting a process.
See [Configuration](../../configuration/local-configuration#hostname).

#### -o, --overwrite

Overwrite existing output file (optional; defaults to false).

#### -p, --port `<arg>`

Office socket port (optional; defaults to 2002).
See [Configuration](../../configuration/local-configuration#portnumbers-pipenames).

#### -r, --registry `<file>`

Document formats registry configuration file (optional).

#### -t, --timeout `<arg>`

Maximum conversion time in seconds (optional; defaults to 120).
See [Configuration](../../configuration/local-configuration#taskexecutiontimeout).

#### -u, --user-profile `<dir>`

Use settings from the given OOo user installation directory (optional).
See [Configuration](../../configuration/local-configuration#templateprofiledir).

#### -v, --version

Displays version information and exit.

#### -w, --working-dir `<dir>`

Directory where temporary office profile directories will be created (optional; defaults to java.io.tmpdir).
See [Configuration](../../configuration/local-configuration#workingdir).

### Remarks

+ Using **-a**

  An application context configuration file is really a Spring configuration file, so a configuration file will start
  with the following:

   ```xml
   <?xml version="1.0" encoding="UTF-8"?>
   <beans xmlns="http://www.springframework.org/schema/beans"
          xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
          xsi:schemaLocation="http://www.springframework.org/schema/beans http://www.springframework.org/schema/beans/spring-beans.xsd">

     <!-- Configuration goes here! -->

   </beans>
   ```

  A configuration file can be used to initialize
  the [filter chain](using-filters.md) that will be applied to the loaded
  document before it is saved to the desired format. Here's an example of a configuration to create a filter chain that
  will first insert a given text to the document, then will insert a graphic into it, and finally will apply the
  configured text strings replacement:

   ```xml
   <?xml version="1.0" encoding="UTF-8"?>
   <beans xmlns="http://www.springframework.org/schema/beans"
          xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
          xsi:schemaLocation="http://www.springframework.org/schema/beans http://www.springframework.org/schema/beans/spring-beans.xsd">

     <!-- Creation of all the required filters we want to add to the filter chain. -->
     <bean id="textInserterFilter" class="org.jodconverter.filter.text.TextInserterFilter">
       <!-- Text to insert -->
       <constructor-arg value="text to insert" />
       <!-- Arguments related to the added box size and position -->
       <constructor-arg value="100" /> <!-- Width, 10 CM -->
       <constructor-arg value="10" />  <!-- Height, 1 CM -->
       <constructor-arg value="50" />  <!-- Horizontal Position, 5 CM -->
       <constructor-arg value="100" /> <!-- Vertical Position, 10 CM -->
     </bean>
     <bean id="graphicInserterFilter" class="org.jodconverter.filter.text.GraphicInserterFilter">
       <!-- Path to the image -->
       <constructor-arg value="src/integTest/resources/images/sample-1.jpg" />
       <!-- Arguments related to the added box size and position -->
       <constructor-arg value="50" />  <!-- Horizontal Position, 5 CM -->
       <constructor-arg value="111" /> <!-- Vertical Position, 11.1 CM (just under text box) -->
     </bean>
     <bean id="textReplacerFilter" class="org.jodconverter.filter.text.TextReplacerFilter">
       <constructor-arg name="searchList">
         <list>
           <value>text</value>
           <value>to insert</value>
         </list>
       </constructor-arg>
       <constructor-arg name="replacementList">
         <list>
           <value>Text</value>
           <value>describing the image below</value>
         </list>
       </constructor-arg>
     </bean>

     <!-- Configure the filter chain that will be used while converting a document. -->
     <bean id="filterChain" class="org.jodconverter.filter.DefaultFilterChain">
       <constructor-arg>
         <list>
           <ref bean="textInserterFilter" />
           <ref bean="graphicInserterFilter" />
           <ref bean="textReplacerFilter" />
         </list>
       </constructor-arg>
     </bean>

   </beans>
   ```

  Combine with the *-c* switch, a configuration file can be used to initialize
  the [SSL Context](../libreoffice-remote/#ssl-support) of the connection
  to the Libre Office Online server:

   ```xml
   <?xml version="1.0" encoding="UTF-8"?>
   <beans xmlns="http://www.springframework.org/schema/beans"
          xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
          xsi:schemaLocation="http://www.springframework.org/schema/beans http://www.springframework.org/schema/beans/spring-beans.xsd">
     <!-- Configure the SSL to secure communication with a Libre Office Online server. -->
     <bean class="org.jodconverter.ssl.SslConfig">
       <!-- Indicates whether SSL support is enabled or not. -->
       <property name="enabled" value="true" />
       <!-- Comma separated values of the supported SSL ciphers. Defaults to the JVM default values. -->
       <property name="ciphers" value="ECDHE_RSA_WITH_AES_256_CBC_SHA384,TLS_ECDHE_RSA_WITH_AES_256_CBC_SHA" />
       <!-- Comma separated values of the enabled SSL protocols. Defaults to the JVM default values. -->
       <property name="enabledProtocols" value="enabledProtocols" />
       <!-- The alias that identifies the key in the key store. -->
       <property name="keyAlias" value="keyalias" />
       <!-- The password used to access the key in the key store. -->
       <property name="keyPassword" value="keypassword" />
       <!-- The path to the key store. -->
       <property name="keyStore" value="/path/to/the/keystore.jks" />
       <!-- The password used to load the key store. -->
       <property name="keyStorePassword" value="keystorepassword" />
       <!-- The type of key store. -->
       <property name="keyStoreType" value="JKS" />
       <!-- The provider for the key store. -->
       <property name="keyStoreProvider" value="BC" />
       <!-- The path to the trust store. -->
       <property name="trustStore" value="/path/to/the/truststore.p12" />
       <!-- The password used to load the trust store . -->
       <property name="trustStorePassword" value="truststorepassword" />
       <!-- The type of trust store. -->
       <property name="trustStoreType" value="PKCS12" />
       <!-- The provider for the trust store. -->
       <property name="trustStoreProvider" value="SUN" />
       <!-- The SSL protocol to use. Default to TLS. -->
       <property name="protocol" value="TLS" />
       <!-- Indicates whether hostname should be verify during SSL handshake. Defaults to true. -->
       <property name="verifyHostname" value="true" />
     </bean>
   </beans>
   ```
