[TOC]

###  文件结构

+ database

  数据库有关脚本

+ doc

  所有文档

  + JavaDoc

    Java文档

  + OpenAPI

    API文档，包括Java和Python。符合OpenAPI3.0标准，请使用专门的软件观看。

  + PythonDoc

    Python文档

+ source

  所有源代码

  + Cilent

    客户端

  + Server

    服务端

```
project
├─database
├─doc
│  ├─JavaDoc
│  ├─OpenAPI
│  │  ├─Java(Spring)
│  │  └─Python(Flask)
│  └─PythonDoc
└─source
    ├─Cilent
    │  └─electron
    └─Server
        ├─Java
        │  ├─MelodyServer
        │  │  └─src
        │  └─PythonCore
        │      └─src
        └─Python
            └─createMidi
```

### 环境配置

教程以Windows为基准，其他平台可参考配置。

#### 基本环境

在使用前请确保你已经配置以下所有程序(windows需要加入环境变量)，标有*表示仅在列出的版本通过测试，不保证其他版本的正常运行。

+ Java
  + JDK ≥ 17
+ Python
  + Python 3.10.10*****
+ Node.js
  + Node.js ≥ 12.9
  + npm ≥ 6.9

#### 服务器环境配置

本项目使用了以下第三方工具，请安装并配置环境变量。

+ ffmpeg
+ timidity++(需要配置音色库)
+ Maven

配置文件：

+ 文件：`project\source\Server\Java\PythonCore\CONSTANT.py`

  `PREFIX_PATH`值：Flask所产生临时文件的路径

+ 文件：`project\source\Server\Java\MelodyServer\src\main\resources\application.yml`

  springboot服务器的配置文件，其中有几项极其重要的值必须设定：

  + `rsa-key-path`下所有属性
  + `cache`下所有属性
  + *****`spring.datasource`下所有属性

  对于其他属性，可自行设置。

#### 数据库配置

在`project\database`路径下有数据库脚本文件，本身不包含任何数据，用于在程序运行时记录有关信息，导入该脚本即可。

#### 程序第三方包安装及启动

在进行本步骤时，请确保你已经安装上一节中提到的环境。

+ Java

  1. 在路径`project\source\Server\Java\PythonCore`下执行`mvn install`

  2. 在路径`project\source\Server\Java\MelodyServer`下执行`mvn package`

     此时在路径`project\source\Server\Java\MelodyServer\target`下会有打好的jar包。

  3. 运行`java -jar example.jar`即可启动springboot服务器。

+ Python

  1. 在路径`project\source\Server\Python\createMidi`下执行`pip install -r requirements.txt`

  2. 运行`python server.py`即可启动Flask服务器

+ Node.js

1. 在路径`source\Cilent\electron`下运行`npm install`
2. 运行`npm start`即可启动客户端

### 程序使用说明

在配置好上述所有环境后即可启动程序

在springboot服务器启动后默认在`localhost`的`80`和`443`分别启动HTTP和HTTPS服务，此时可以通过浏览器直接访问，也可启动客户端进行访问。