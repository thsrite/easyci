# easyci
简易化自动化部署系统

easy-ci：后端

easyci-ui：前端

shell：shell脚本


easyci是傻瓜式一键部署本系统

sh：shell脚本

work：vue的dist文件及jar包、nginx配置文件、mysql数据库sql

install.sh：一键安装系统环境脚本

startEasyCi：启动服务脚本

stopEasyCi：停止服务脚本

  EasyCi系统开发的目的是免去远程发布的免密登陆、拉取gitlab代码的认证、手动添加gitlab hook、查看gitlab中该项目的git地址等等多余的操作。
	
  这些操作均有后台自动完成，系统提供运行环境一键安装脚本、自动化安装部署本系统、开箱即用，只需要几个参数即可实现项目的远程构建，暂时只支持vue和java项目的构建。
	
  Easyci系统采用B/S架构，后端采用springboot框架、前端采用Vue的element ui、数据库采用mysql、运行工具为shell脚本、采用websocket进行实时日志传输。
 
工具链：

EasyCI：easyci系统后台，调用shell脚本、通过接口将结果返回前端

EasyCI-UI：easyci系统前台，vue。调用后台接口展示数据

Gitlab：代码托管工具，通过gitlab hook触发easyci持续集成交付

shell脚本：linux脚本

Docker私服：私有docker镜像仓库，用于远程构建

MySQL：easyci系统数据库

流程：

1.根据安装文档安装系统，启动系统

2.访问系统首先需要验证gitlab：gitlab的url、用户名和密码。用于选择项目构建、服务器拉取代码验证、自动添加hook，免去手动操作。

3.添加远程发布服务器：服务器ip、端口、用户名、密码。后台自动完成服务器间的免密登录，用于项目远程发布、查看服务器容器以及容器的各种操作。

4.部署：选择项目url、输入docker容器端口映射关系、选择项目类型、输入收件人邮箱、选择部署服务器，只需要输入两项内容即可完成部署。

5.点击部署，会弹出窗口显示系统部署日志。

6.部署会自动添加easyci的hook接口url到该部署项目的hook中，用于自动触发构建。

7.部署完成自动发送邮件给收件人邮箱，显示构建结果、构建时间、构建项目、构建日志。

8.后续开发人员提交代码到该项目的gitlab，会触发gitlab的hook调用easyci接口，查询之前部署的信息进行自动部署，实现持续集成。

9.页面展示easyci本机正在运行的容器和添加的远程服务器正在运行的容器，支持数据自动刷新与关闭

10.可以选择添加的远程服务器，查看该服务器的容器列表

11.容器操作：可点击启动  、停止、重启、销毁在页面对服务器中运行的容器进行操作，即docker  start|stop|restart|rm 容器名称

12..容器实时日志：点击日志，可以查看该容器的实时日志，即docker logs -f 容器名称
