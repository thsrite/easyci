echo '停止web'
systemctl stop nginx
echo '停止后台'
ps -ef | grep jar | awk '{print $2}' | head -n 1 | xargs kill -9
t=`netstat -tunlp | grep -E '9875|81'`
if [[ $t=='' ]];then
	echo '系统停止成功'
else
	echo '系统停止失败'
fi
