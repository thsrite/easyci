#镜像名称
IMAGE_NAME=$1

echo "=======================删除旧容器======================="
t=`sudo docker ps -a | grep $IMAGE_NAME|awk '{print  $1}'|sed 's/%//g'`;
if [[ $t ]];
then
sudo docker stop  $t
echo "停止容器成功"
sudo docker rm $t
echo "删除容器成功"
fi

echo "=======================删除无用镜像======================="
docker images|grep none|awk '{print $3}'|xargs docker rmi

echo "=================删除多余tag，保留最新三个================"
tagnum=`git tag | wc -l`;
tag=(`git tag`)
a=0
echo "当前tag数目为：" $tagnum "个"
until [[ $tagnum -lt 3 ]]
do
   echo $a 准备删除tag：${tag[$a]}
   git tag -d ${tag[$a]}
   echo "=======================本地删除成功======================="
   git push origin :refs/tags/${tag[$a]}
   echo "=======================远程删除成功======================="
   a=`expr $a + 1`
   tagnum=`expr $tagnum - 1`
done
exit 0
