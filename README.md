## Yex Tool
Tool to test Yex integration

## Building Notes
You need: JDK 8, Protocol buffers (protoc) 2.6.1, Maven. Building is supported from the command line with Maven and from any IDE that can load Maven projects.

## Build From Source
1. git clone git@github.com:panpengfei/yex-tool.git  
1. cd yex-tool  
1. vim pom.xml 
   * update protoc config(/usr/local/bin/protoc) to your own protoc install directory
1. mvn clean package

## Usage
<pre>
usage: java -jar target/yex-tool-1.0.0.jar  -f &lt;input file&lt; [-h] [-jo |
       -js | -pb]   -s &lt;server address&lt;

Tool to test Yex integration
 -f &lt;input file&gt;       input file
 -h,--help             display help message
 -jo                   json format, and NativeRequest/NativeResponse json
                       object format
 -js                   json format, and NativeRequest/NativeResponse json
                       string format
 -pb                   protocol buffers format, as default value
 -s &lt;server address&gt;   server address
</pre>
