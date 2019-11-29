#-g 项目的git地址
#-p docker容器运行的端口映射关系
#-l 项目的语言：java or vue
#-m 接收邮件的邮箱
#-h help
#-d docker私服配置
#-e easy-ci脚本路径
#-s 日志地址配置
#-w 项目存放地址配置
#-n 项目日志名称
#-i 是否本地部署
#-u 远程用户名
#-r 远程密码
while getopts "g:p:l:m:h:d:e:s:w:n:i:u:r:" opt
do
    case $opt in
        g) GIT_URL=$OPTARG ;;
        p) PORT_MAPPING=$OPTARG ;;
        l) PROJECT_LANG=$OPTARG ;;
	m) TO_LIST=$OPTARG ;;
	d) DOCKER_HUB=$OPTARG ;;
        e) EASY_CI=$OPTARG ;;
	s) LOGS_PATH=$OPTARG ;;
	w) WORK_PATH=$OPTARG ;;
	n) LOG_NAME=$OPTARG ;;
	i) IS_LOCAL=$OPTARG ;;
	u) REMOTE_USER=$OPTARG ;;
	r) REMOTE_PASSWORD=$OPTARG ;;
	\?) 
                echo "Usage: args [-g] [-p] [-l] [-m] [-d] [-e] [-s] [-w] [-n] [-i] [-u] [-r]"
                echo "-g means git-url: git@****/***.git"
                echo "-p means port-mapping: 5000:3999"
                echo "-l means project language：java or vue"
		echo "-m means mail receiver list: *****@163.com"
		echo "-d means docker hub url:192.168.8.10:5000"
		echo "-e means EASY_CI_HOME"
		echo "-s means logs path"
		echo "-w means workspace path"
		echo "-n means project logs name"
		echo "-i means local or remote deploy ip"
		echo "-u means ssh remote ip username"
		echo "-r means ssh remote ip password"
                exit 1;;
        h)
                echo "Usage: args [-g] [-p] [-l] [-m] [-d] [-e] [-s] [-w] [-n] [-i] [-u] [-r]"
                echo "-g means git-url: git@****/***.git"
                echo "-p means port-mapping: 5000:3999"
                echo "-l means project language：java or vue"
                echo "-m means mail receiver list: *****@163.com"
		echo "-d means docker hub url:192.168.8.10:5000"
		echo "-e means EASY_CI_HOME"
		echo "-s means logspath"
		echo "-w means workspace path"
		echo "-n means project logs name"
		echo "-i means local or remote deploy ip"
		echo "-u means ssh remote ip username"
		echo "-r means ssh remote ip password"
                exit 1;;
    esac
done

#环境配置
#EASY_CI_HOME环境变量设置
export EASY_CI_HOME=$EASY_CI

#以下是提取git项目名操作
#1.将git url以/分割成数组
GIT_ARR=(${GIT_URL//// })
#2.获取数组的数量
GIT_ARR_LENGTH=${#GIT_ARR[*]}
#3.获取数组最后一个值的索引
GIT_ARR_LAST_INDEX=`expr $GIT_ARR_LENGTH - 1`
#4.根据索引获取数组最后一个值
GIT_ARR_LAST=${GIT_ARR[$GIT_ARR_LAST_INDEX]}
#5.将数组最后一个值以.分割，出去.git
#6.CONTAINER_NAME为默认容器名称
CONTAINER_NAME=(${GIT_ARR_LAST//./ })
#7.镜像名称为docker私服/容器名称,私服地址192.168.8.10:5000。
#如果没有docker私服、则IMAGE_NAME=$CONTAINER_NAME
if [[ $DOCKER_HUB != '' ]];then
	IMAGE_NAME=$DOCKER_HUB/$CONTAINER_NAME
else
	IMAGE_NAME=$CONTAINER_NAME
fi

#日志路径：绝对路径
LOG_PATH=$LOGS_PATH/$CONTAINER_NAME/$LOG_NAME
#是否回滚成功
IS_ROLLBACK=0
	
rm -rf $LOG_PATH
#端口映射关系
port=(${PORT_MAPPING//:/ })

echo '查看本地端口占用'
portuse=`netstat -tunlp | grep ${port[0]}`
if [[ $portuse == '' ]];then

	echo 本地${port[0]}端口未被占用
	echo '部署执行开始'
	
	#工作目录没有则创建
	if [[ ! -d $WORK_PATH ]];then
		mkdir $WORK_PATH
	fi
	#进入工作目录进行后续操作
	cd $WORK_PATH
	#判断日志路径是否存在，没有则创建
	if [[ ! -d $LOGS_PATH/$CONTAINER_NAME ]];then
		mkdir -p $LOGS_PATH/$CONTAINER_NAME
	fi
	#判断项目文件是否存在，有则删除
	if [[ -d $CONTAINER_NAME ]];then
		echo "文件夹已存在，删除旧文件"
		sudo rm -rf $CONTAINER_NAME
	fi

	#构建步骤
	echo '==============开始拉取代码============='
	git clone $GIT_URL
	status=`echo $?`
	echo 错误码:$status
	if [[ ! $status -eq 0 ]];then
		echo '==============拉取代码失败============='
		exit 1;
	fi
	echo '==============拉取代码结束============='
	cd $CONTAINER_NAME

	bash $EASY_CI_HOME/auto_build.sh $PROJECT_LANG $IMAGE_NAME $PORT_MAPPING $CONTAINER_NAME $LOG_PATH $IS_ROLLBACK $TO_LIST $GIT_URL $WORK_PATH/$CONTAINER_NAME $IS_LOCAL $REMOTE_USER $REMOTE_PASSWORD | tee $LOG_PATH
else
	echo '本地端口已被占用'
	if [[ $TO_LIST != '' ]];then
		bash $EASY_CI_HOME/sendmail.sh $TO_LIST 端口已被占用:$CONTAINER_NAME-$(date "+%Y%m%d%H%M%S") $LOG_PATH
		if [[ `echo $?` -eq 0 ]];then
			echo '邮件发送成功'
		else
			echo '邮件发送失败'
		fi
		exit 0
	fi
	exit 0
fi
