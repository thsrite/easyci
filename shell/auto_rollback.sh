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
IS_ROLLBACK=`expr $6 + 1`
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

echo "=======================开始执行回滚========================"

echo '=======================删除旧的构建========================'
bash $EASY_CI_HOME/auto_destroy.sh $IMAGE_NAME
echo '======================旧的构建删除成功===================='

tag=(`git tag`)
echo "检测到的git tag版本为：" ${tag[*]}

maxtag=(${tag[0]//-/ })
maxnum=${maxtag[1]}

for i in ${!tag[*]};do
	nexttag=(${tag[$i]//-/ })
	nextnum=${nexttag[1]}

	if [[ $maxnum -lt $nextnum ]];then
		maxnum=$nextnum
	fi
done
echo "检测到最新的git tag版本为：" rc_$maxnum 
echo "拉取上一次构建成功的tag代码"

if [[ `git checkout rc_$maxnum` ]];then
	echo "拉取代码失败，请手动选择代码构建"
else
	echo "拉取代码成功"
	bash $EASY_CI_HOME/auto_build.sh $PROJECT_LANG $IMAGE_NAME $PORT_MAPPING $CONTAINER_NAME $LOG_PATH $IS_ROLLBACK $TO_LIST $GIT_URL $PROJECT_PATH $IS_LOCAL $REMOTE_USER $REMOTE_PASSWORD
	exit 0
fi
exit 0
