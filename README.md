## YexTool
Tool to test Yex integration

## Building Notes
You need: JDK 8, Maven 3.2, Protocol buffers (protoc) 2.6.1. Building is supported from the command line with Maven and from any IDE that can load Maven projects.

## Build From Source
git clone git@github.com:panpengfei/YexTool.git  
cd YexTool  
mvn clean package  

## Usage
<pre>
usage: java -jar target/yex-tool-1.0.0.jar  -f &lt;input file&gt; [-h] -s &lt;server
       address&gt;
Tool to test Yex integration
 -f &lt;input file&gt;       input file
 -h,--help             display help message
 -s &lt;server address&gt;   server address
</pre>
<!---->
