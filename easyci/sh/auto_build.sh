#项目语言
PROJECT_LANG=$1
#镜像名称
IMAGE_NAME=$2
#端口映射
PORT_MAPPING=$3
#容器名称
CONTAINER_NAME=$4
#日志路径
LOG_PATH=$5
#是否回滚成功
IS_ROLLBACK=$6
#收件邮箱
TO_LIST=$7
#git仓库地址
GIT_URL=$8
#项目工作目录
PROJECT_PATH=$9
#是否本地部署
IS_LOCAL=${10}
#远程用户名
REMOTE_USER=${11}
#远程密码
REMOTE_PASSWORD=${12}
#端口映射关系
port=(${PORT_MAPPING//:/ })
#构建状态
status=

###项目构建信息
echo $CONTAINER_NAME拉取代码成功
echo $CONTAINER_NAME项目于$(date "+%Y-%m-%d %H:%M:%S")开始构建
echo 构建项目的git仓库地址为：$GIT_URL
echo 构建项目为$PROJECT_LANG语言
echo docker镜像名称为：$IMAGE_NAME
echo docker容器名称为：$CONTAINER_NAME
echo docker容器端口为${port[1]}
echo 映射宿主机端口为${port[0]}
echo 构架项目日志文件为：$LOG_PATH
echo 构建项目工作目录为：$PROJECT_PATH
echo 收件人邮箱为：$TO_LIST
echo 部署服务器为: $IS_LOCAL

echo ''
echo ''
###系统清理阶段
echo '=======================删除旧的构建========================'
bash $EASY_CI_HOME/auto_destroy.sh $IMAGE_NAME
echo '=========================删除成功=========================='
echo ''
###项目打包阶段
if [[ $PROJECT_LANG = java ]];then

	echo '======================加入Dockerfile文件===================='
	cp $EASY_CI_HOME/Dockerfile-java .
	mv Dockerfile-java Dockerfile
	echo '========================mvn清理开始========================'
	mvn clean
	echo '========================mvn清理结束========================'
	
	echo '=======================jar包打包开始======================='
	mvn package  -DskipTests
	if [[ `echo $?` -eq 0 ]];then
		echo '=======================jar包打包完成======================='
	else
		echo '=======================jar包打包失败======================='
		if [[ $IS_ROLLBACK -lt 1 ]];then
			bash $EASY_CI_HOME/auto_rollback.sh $PROJECT_LANG $IMAGE_NAME $PORT_MAPPING $CONTAINER_NAME $LOG_PATH $IS_ROLLBACK $TO_LIST $GIT_URL $PROJECT_PATH $IS_LOCAL $REMOTE_USER $REMOTE_PASSWORD
		else
			if [[ $TO_LIST != '' ]];then
				LOG=$LOG_PATH
				bash $EASY_CI_HOME/sendmail.sh $TO_LIST 系统错误:$CONTAINER_NAME-$(date "+%Y%m%d%H%M%S") $LOG 
				if [[ `echo $?` -eq 0 ]];then
					echo '邮件发送成功'
				else
					echo '邮件发送失败'
				fi
			fi
			echo '系统错误'
			exit 0
		fi
		exit 0
	fi
	rm -rf target/*sources.jar
	echo '=======================docker打包开始======================='
	sudo docker build -t $2 .
	if [[ `echo $?` -eq 0 ]];then
		echo '=======================docker打包完成======================='
	else
		echo '=======================docker打包失败======================='
		if [[ $IS_ROLLBACK -lt 1 ]];then
				bash $EASY_CI_HOME/auto_rollback.sh $PROJECT_LANG $IMAGE_NAME $PORT_MAPPING $CONTAINER_NAME $LOG_PATH $IS_ROLLBACK $TO_LIST $GIT_URL $PROJECT_PATH $IS_LOCAL $REMOTE_USER $REMOTE_PASSWORD
		else
			if [[ $TO_LIST != '' ]];then
				LOG=$LOG_PATH
				bash $EASY_CI_HOME/sendmail.sh $TO_LIST 系统错误:$CONTAINER_NAME-$(date "+%Y%m%d%H%M%S") $LOG
				if [[ `echo $?` -eq 0 ]];then
					echo '邮件发送成功'
				else
					echo '邮件发送失败'
				fi
			fi
			echo '系统错误'
			exit 0
		fi
		exit 0
	fi
	
else

	echo '======================加入Dockerfile文件===================='
	cp $EASY_CI_HOME/Dockerfile-vue .
	mv Dockerfile-vue Dockerfile
	

	echo "server {
    listen  ${port[1]}    ;
    location / {
        root   /usr/share/nginx/html;
        index  index.html index.htm;
		}
    }" > default.conf

	echo '=======================vue环境安装开始======================='
	sudo /usr/local/bin/npm install
	if [[ `echo $?` -eq 0 ]];then
		echo '=======================vue环境安装完成======================='
	else
		echo '=======================vue环境安装失败======================='
		if [[ $IS_ROLLBACK -lt 1 ]];then
				bash $EASY_CI_HOME/auto_rollback.sh $PROJECT_LANG $IMAGE_NAME $PORT_MAPPING $CONTAINER_NAME $LOG_PATH $IS_ROLLBACK $TO_LIST $GIT_URL $PROJECT_PATH $IS_LOCAL $REMOTE_USER $REMOTE_PASSWORD
		else
			if [[ $TO_LIST != '' ]];then
				LOG=$LOG_PATH
				bash $EASY_CI_HOME/sendmail.sh $TO_LIST 系统错误:$CONTAINER_NAME-$(date "+%Y%m%d%H%M%S") $LOG 
				if [[ `echo $?` -eq 0 ]];then
					echo '邮件发送成功'
				else
					echo '邮件发送失败'
				fi
			fi
			echo '系统错误'                  
			exit 0
		fi
		exit 0
	fi
	
	echo '=======================vue打包开始======================='
	sudo /usr/local/bin/npm run build
	if [[ `echo $?` -eq 0 ]];then
		echo '=======================vue打包完成======================='
	else
		echo '=======================vue打包失败======================='
		if [[ $IS_ROLLBACK -lt 1 ]];then
				bash $EASY_CI_HOME/auto_rollback.sh $PROJECT_LANG $IMAGE_NAME $PORT_MAPPING $CONTAINER_NAME $LOG_PATH $IS_ROLLBACK $TO_LIST $GIT_URL $PROJECT_PATH $IS_LOCAL $REMOTE_USER $REMOTE_PASSWORD
		else
			if [[ $TO_LIST != '' ]];then
				LOG=$LOG_PATH
				bash $EASY_CI_HOME/sendmail.sh $TO_LIST 系统错误:$CONTAINER_NAME-$(date "+%Y%m%d%H%M%S") $LOG 
				if [[ `echo $?` -eq 0 ]];then
					echo '邮件发送成功'
				else
					echo '邮件发送失败'
				fi
			fi
			echo '系统错误'
			exit 0
		fi
		exit 0
	fi
	
	echo '=======================docker打包开始======================='
	sudo docker build -t $2 .
	if [[ `echo $?` -eq 0 ]];then
		echo '=======================docker打包完成======================='
	else
		echo '=======================docker打包失败======================='
		if [[ $IS_ROLLBACK -lt 1 ]];then
				bash $EASY_CI_HOME/auto_rollback.sh $PROJECT_LANG $IMAGE_NAME $PORT_MAPPING $CONTAINER_NAME $LOG_PATH $IS_ROLLBACK $TO_LIST $GIT_URL $PROJECT_PATH $IS_LOCAL $REMOTE_USER $REMOTE_PASSWORD
		else
			if [[ $TO_LIST != '' ]];then
				LOG=$LOG_PATH
				bash $EASY_CI_HOME/sendmail.sh $TO_LIST 系统错误:$CONTAINER_NAME-$(date "+%Y%m%d%H%M%S") $LOG
				if [[ `echo $?` -eq 0 ]];then
					echo '邮件发送成功'
				else
					echo '邮件发送失败'
				fi
			fi
				echo '系统错误'
				exit 0
		fi
		exit 0
	fi
fi

#ip=`ifconfig ens192 | grep "inet" | awk '{ print $2}' | awk -F: '{print $1}' | grep 192`
#echo 本机ip:$ip
###系统部署阶段	

echo '本地部署开始'
echo '=======================容器运行开始========================'
sudo docker run --net=host -d --name $CONTAINER_NAME -p $PORT_MAPPING $IMAGE_NAME
if [[ `echo $?` -eq 0 ]];then
	resule=`sudo docker ps | grep $IMAGE_NAME|awk '{print  $1}'|sed 's/%//g'`
	sleep 3
	cid=`sudo docker ps -q -f status=running -f name=$CONTAINER_NAME`
	if [[ $resule != '' ]] && [[ $cid != '' ]];then
		sudo docker ps | grep $IMAGE_NAME
		echo '=======================容器运行完成========================'
		IMAGE_ARR=(${IMAGE_NAME//// })
		if [[ ${#IMAGE_ARR[*]} -gt 0 ]];then
			echo '=================开始推送镜像到docker私服=================='
			sudo docker push $IMAGE_NAME
			echo '========================推送镜像完成======================='
		fi
		
		if [[ $IS_ROLLBACK -eq 0 ]];then
			echo '=======================构建成功=======================' 
			echo '=====================构建tag push开始===================='
			git tag success-$(date "+%Y%m%d%H%M")
			git push origin success-$(date "+%Y%m%d%H%M")
			if [[ `echo $?` -eq 0 ]];then
				echo '====================构建tag push成功=================='
			else
				echo '====================构建tag push失败=================='
			fi
			buildstatus='build success'
			echo '本地构建成功'
		else
			echo '=======================回滚成功=======================' 
			echo '=====================回滚tag push开始===================='
			git tag rollback-$(date "+%Y%m%d%H%M")
			git push origin rollback-$(date "+%Y%m%d%H%M")
			if [[ `echo $?` -eq 0 ]];then
					echo '====================回滚tag push成功=================='
			else
					echo '====================回滚tag push失败=================='
			fi
			buildstatus='rollback success'
			echo '本地回滚成功'
		fi
	else
		echo "容器启动失败，执行回滚"
			if [[ $IS_ROLLBACK -lt 1 ]];then
					bash $EASY_CI_HOME/auto_rollback.sh $PROJECT_LANG $IMAGE_NAME $PORT_MAPPING $CONTAINER_NAME $LOG_PATH $IS_ROLLBACK $TO_LIST $GIT_URL $PROJECT_PATH $IS_LOCAL $REMOTE_USER $REMOTE_PASSWORD
			else
				if [[ $TO_LIST != '' ]];then
					LOG=$LOG_PATH
					bash $EASY_CI_HOME/sendmail.sh $TO_LIST 系统错误:$CONTAINER_NAME-$(date "+%Y%m%d%H%M%S") $LOG 
					if [[ `echo $?` -eq 0 ]];then
						echo '邮件发送成功'
					else
						echo '邮件发送失败'
					fi
				fi
				###系统清理阶段
				echo '=======================删除旧的构建========================'
				bash $EASY_CI_HOME/auto_destroy.sh $IMAGE_NAME
				echo '=========================删除成功=========================='
				echo ''
				echo '系统错误'
			fi
			exit 0
	fi		
else 
	echo "容器创建失败，执行回滚"
	if [[ $IS_ROLLBACK -lt 1 ]];then
			bash $EASY_CI_HOME/auto_rollback.sh $PROJECT_LANG $IMAGE_NAME $PORT_MAPPING $CONTAINER_NAME $LOG_PATH $IS_ROLLBACK $TO_LIST $GIT_URL $PROJECT_PATH $IS_LOCAL $REMOTE_USER $REMOTE_PASSWORD
	else
		if [[ $TO_LIST != '' ]];then
			LOG=$LOG_PATH
			bash $EASY_CI_HOME/sendmail.sh $TO_LIST 系统错误:$CONTAINER_NAME-$(date "+%Y%m%d%H%M%S") $LOG 
			if [[ `echo $?` -eq 0 ]];then
				echo '邮件发送成功'
			else
				echo '邮件发送失败'
			fi
		fi
		###系统清理阶段
		echo '=======================删除旧的构建========================'
		bash $EASY_CI_HOME/auto_destroy.sh $IMAGE_NAME
		echo '=========================删除成功=========================='
		echo ''
		echo '系统错误'
	fi
	exit 0	
fi 
		

echo '开始执行远程部署'
echo "清除本地容器开始"
t=`sudo docker ps -a | grep $IMAGE_NAME|awk '{print  $1}'|sed 's/%//g'`;
if [[ $t ]];then
	sudo docker stop  $t
	echo "停止容器成功"
	sudo docker rm $t
	echo "删除容器成功"
fi
echo "清除本地容器完成"


echo "连接远程服务器,执行命令"
connect=`ssh -o StrictHostKeyChecking=no $IS_LOCAL echo success`
if [[ $connect == 'success' ]];then 
	echo '连接成功'
	echo "启动docker服务"
	ssh -o StrictHostKeyChecking=no $IS_LOCAL "systemctl start docker"
	echo '拉取私服镜像'
	ssh -o StrictHostKeyChecking=no $IS_LOCAL "sudo docker pull $IMAGE_NAME"
	if [[ `echo $?` -eq 0 ]];then
		echo '拉取镜像成功'
		echo "=======================删除旧容器======================="
		t=`ssh -o StrictHostKeyChecking=no $IS_LOCAL sudo docker ps -a | grep $CONTAINER_NAME | awk '{print \$1}'| sed 's/%//g'`;
		if [[ $t ]];then
			ssh -o StrictHostKeyChecking=no $IS_LOCAL "sudo docker stop  $t"
			echo "停止容器成功"
			ssh -o StrictHostKeyChecking=no $IS_LOCAL "sudo docker rm $t"
			echo "删除容器成功"
		fi
		echo "=======================删除无用镜像======================="
		ssh -o StrictHostKeyChecking=no $IS_LOCAL 'docker images|grep none|awk "{print $3}"|xargs docker rmi'

		echo '查看远程端口占用'
		portuse=`ssh -o StrictHostKeyChecking=no $IS_LOCAL netstat -tunlp | grep ${port[0]}`
		if [[ $portuse == '' ]];then
			echo 远程${port[0]}端口未被占用	
			echo '容器运行开始'
			ssh -o StrictHostKeyChecking=no $IS_LOCAL "sudo docker run --net=host -d --name $CONTAINER_NAME -p $PORT_MAPPING $IMAGE_NAME"
			if [[ `echo $?` -eq 0 ]];then 
				echo '退出远程服务器'
				if [[ $TO_LIST != '' ]];then
					LOG=$LOG_PATH
					if [[ $buildstatus == 'build success' ]];then
						title='容器远程部署成功'
					else
						title='容器远程回滚成功'
					fi
					bash $EASY_CI_HOME/sendmail.sh $TO_LIST $title:$CONTAINER_NAME-$(date "+%Y%m%d%H%M%S") $LOG 
					if [[ `echo $?` -eq 0 ]];then
						echo '邮件发送成功'
					else
						echo '邮件发送失败'
					fi
				fi
				echo $title
			else
				echo '系统清理开始'
				echo "=======================删除旧容器======================="
				t=`ssh -o StrictHostKeyChecking=no $IS_LOCAL sudo docker ps -a | grep $CONTAINER_NAME | awk '{print \$1}'| sed 's/%//g'`;
				if [[ $t ]];then
					ssh -o StrictHostKeyChecking=no $IS_LOCAL "sudo docker stop  $t"
					echo "停止容器成功"
					ssh -o StrictHostKeyChecking=no $IS_LOCAL "sudo docker rm $t"
					echo "删除容器成功"
				fi
				echo "=======================删除无用镜像======================="
				ssh -o StrictHostKeyChecking=no $IS_LOCAL 'docker images|grep none|awk "{print $3}" |xargs docker rmi'
				echo '系统清理结束'
				if [[ $TO_LIST != '' ]];then
					LOG=$LOG_PATH
					bash $EASY_CI_HOME/sendmail.sh $TO_LIST 容器远程部署失败:$CONTAINER_NAME-$(date "+%Y%m%d%H%M%S") $LOG 
					if [[ `echo $?` -eq 0 ]];then
						echo '邮件发送成功'
					else
						echo '邮件发送失败'
					fi
				fi
				echo '容器远程部署失败'
				echo '退出远程服务器'
			fi
			exit 0
		else
			echo '远程端口已被占用'
			if [[ $TO_LIST != '' ]];then
				bash $EASY_CI_HOME/sendmail.sh $TO_LIST 远程端口已被占用:$CONTAINER_NAME-$(date "+%Y%m%d%H%M%S") $LOG_PATH
				if [[ `echo $?` -eq 0 ]];then
					echo '邮件发送成功'
				else
					echo '邮件发送失败'
				fi
				exit 0
			fi
			exit 0
		fi
	else
		echo '拉取失败，更改配置'
		dockerhub=${IMAGE_ARR[0]}
		ssh -o StrictHostKeyChecking=no $IS_LOCAL "sudo echo -e '{ \"registry-mirrors\":[\"https://registry.docker-cn.com\"],\"insecure-registries\":[\"$dockerhub\"] }' > /etc/docker/daemon.json"
		echo '重载配置'
		ssh -o StrictHostKeyChecking=no $IS_LOCAL "sudo systemctl daemon-reload"
		echo '重启docker服务'
		ssh -o StrictHostKeyChecking=no $IS_LOCAL "sudo systemctl restart docker"
		echo '重启docker容器'
		ssh -o StrictHostKeyChecking=no $IS_LOCAL "sudo docker ps -aq | xargs docker start"
		echo '拉取私服镜像'
		ssh -o StrictHostKeyChecking=no $IS_LOCAL "sudo docker pull $IMAGE_NAME"
		if [[ `echo $?` -eq 0 ]];then
			echo '拉取镜像成功'
			echo "=======================删除旧容器======================="
			t=`ssh -o StrictHostKeyChecking=no $IS_LOCAL sudo docker ps -a | grep $CONTAINER_NAME | awk '{print \$1}'| sed 's/%//g'`;
			if [[ $t ]];then
				ssh -o StrictHostKeyChecking=no $IS_LOCAL "sudo docker stop  $t"
				echo "停止容器成功"
				ssh -o StrictHostKeyChecking=no $IS_LOCAL "sudo docker rm $t"
				echo "删除容器成功"
			fi
			echo "=======================删除无用镜像======================="
			ssh -o StrictHostKeyChecking=no $IS_LOCAL 'docker images|grep none|awk "{print $3}" |xargs docker rmi'
			
			echo '查看远程端口占用'
			portuse=`ssh -o StrictHostKeyChecking=no $IS_LOCAL netstat -tunlp | grep ${port[0]}`
			if [[ $portuse == '' ]];then
				echo 远程${port[0]}端口未被占用			
				echo '容器运行开始'
				ssh -o StrictHostKeyChecking=no $IS_LOCAL "sudo docker run --net=host -d --name $CONTAINER_NAME -p $PORT_MAPPING $IMAGE_NAME"
				if [[ `echo $?` -eq 0 ]];then
					echo '退出远程服务器'
					if [[ $TO_LIST != '' ]];then
						LOG=$LOG_PATH
						if [[ $buildstatus == 'build success' ]];then
							title='容器远程部署成功'
						else
							title='容器远程回滚成功'
						fi
						bash $EASY_CI_HOME/sendmail.sh $TO_LIST $title:$CONTAINER_NAME-$(date "+%Y%m%d%H%M%S") $LOG 
						if [[ `echo $?` -eq 0 ]];then
							echo '邮件发送成功'
						else
							echo '邮件发送失败'
						fi
					fi
					echo $title
				else
					echo '退出远程服务器'
					if [[ $TO_LIST != '' ]];then
						LOG=$LOG_PATH
						bash $EASY_CI_HOME/sendmail.sh $TO_LIST 容器远程部署失败:$CONTAINER_NAME-$(date "+%Y%m%d%H%M%S") $LOG 
						if [[ `echo $?` -eq 0 ]];then
							echo '邮件发送成功'
						else
							echo '邮件发送失败'
						fi
					fi
					echo '容器远程部署失败'
				fi
				exit 1
			else
				echo '远程端口已被占用'
				if [[ $TO_LIST != '' ]];then
					bash $EASY_CI_HOME/sendmail.sh $TO_LIST 远程端口已被占用:$CONTAINER_NAME-$(date "+%Y%m%d%H%M%S") $LOG_PATH
					if [[ `echo $?` -eq 0 ]];then
						echo '邮件发送成功'
					else
						echo '邮件发送失败'
					fi
					exit 0
				fi
				exit 0
			fi	
		else
			echo '重启docker容器'
			ssh -o StrictHostKeyChecking=no $IS_LOCAL "sudo docker ps -aq | xargs docker start"
			echo '退出远程服务器'
			if [[ $TO_LIST != '' ]];then
				LOG=$LOG_PATH
				bash $EASY_CI_HOME/sendmail.sh $TO_LIST 镜像拉取失败，远程启动失败:$CONTAINER_NAME-$(date "+%Y%m%d%H%M%S") $LOG 
				if [[ `echo $?` -eq 0 ]];then
					echo '邮件发送成功'
				else
					echo '邮件发送失败'
				fi
			fi
			echo '镜像拉取失败，远程启动失败'
			exit 1
		fi
		exit 0
	fi
	exit 0
else
	if [[ $TO_LIST != '' ]];then
		LOG=$LOG_PATH
		bash $EASY_CI_HOME/sendmail.sh $TO_LIST 远程服务器连接失败:$CONTAINER_NAME-$(date "+%Y%m%d%H%M%S") $LOG 
		if [[ `echo $?` -eq 0 ]];then
			echo '邮件发送成功'
		else
			echo '邮件发送失败'
		fi
	fi
	echo '远程服务器连接失败'
	exit 1
fi
exit 0