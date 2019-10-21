echo '0.关闭防火墙'
systemctl stop firewalld
systemctl disable firewalld
setenforce 0

echo '1.安装wget'
yum install wget -y

echo '2.下载jdk1.8'
wget --no-check-certificate --no-cookies --header "Cookie: oraclelicense=accept-securebackup-cookie" http://download.oracle.com/otn-pub/java/jdk/8u131-b11/d54c1d3a095b4ff2b6607d096fa80163/jdk-8u131-linux-x64.tar.gz
echo 'jdk解压'
tar -zxvf jdk-8u131-linux-x64.tar.gz -C /home
echo 'jdk重命名'
mv /home/jdk1.8.0_131/ /home/jdk
echo '设置jdk环境变量'
echo 'export JAVA_HOME=/home/jdk
export JRE_HOME=${JAVA_HOME}/jre
export CLASSPATH=.:${JAVA_HOME}/lib:${JRE_HOME}/lib
export PATH=${JAVA_HOME}/bin:$PATH' >> /etc/profile
echo '重置环境变量'
source /etc/profile
echo '验证jdk'
java -version

echo '3.安装docker'
yum install -y https://download.docker.com/linux/centos/7/x86_64/stable/Packages/docker-ce-18.03.1.ce-1.el7.centos.x86_64.rpm;systemctl start docker;systemctl enable docker;docker -v
echo '验证docker'
docker -v

echo '4.docker私服安装'
docker pull registry
echo 'docker私服启动'
docker run -d -v /data/registry:/var/lib/registry -p 5000:5000 --restart=always --name qdockerhub registry:latest
echo 'docker daemon配置文件修改'
echo { '"registry-mirrors"':['"https://registry.docker-cn.com"'],'"insecure-registries"':['"localhost:5000"'] } > /etc/docker/daemon.json
systemctl daemon-reload
echo 'docker服务重启'
systemctl restart docker
echo '验证docker私服'
curl http://127.0.0.1:5000/v2/_catalog
docker ps 

#echo '5.安装docker-compose'
#curl -L https://github.com/docker/compose/releases/download/1.14.0/docker-compose-`uname -s`-`uname -m` > /usr/local/bin/docker-compose
#sudo chmod +x /usr/local/bin/docker-compose
#echo '验证docker-compose'
#docker-compose -v

echo '6.安装git'
yum install -y git
echo '验证git'
git --version

echo '7.安装maven'
wget http://mirrors.tuna.tsinghua.edu.cn/apache/maven/maven-3/3.6.1/binaries/apache-maven-3.6.1-bin.tar.gz
echo 'maven解压'
tar -zxvf apache-maven-3.6.1-bin.tar.gz -C /home
echo 'maven重命名'
mv /home/apache-maven-3.6.1 /home/maven
echo '设置maven环境变量'
echo 'export MAVEN_HOME=/home/maven
export PATH=$MAVEN_HOME/bin:$PATH' >> /etc/profile
echo '重载环境变量'
source /etc/profile
echo '验证maven'
mvn -v

echo '8.安装nginx'
sudo yum install epel-release -y
sudo yum install nginx -y

echo '9.安装nodejs'
wget https://nodejs.org/dist/v9.8.0/node-v9.8.0-linux-x64.tar.xz -P /home
echo 'nodejs解压'
tar -xvf /home/node-v9.8.0-linux-x64.tar.xz -C /home
echo 'nodejs重命名'
mv /home/node-v9.8.0-linux-x64 /home/node
echo 'nodejs软连接创建'
sudo ln -s /home/node/bin/node /usr/local/bin/      
sudo ln -s /home/node/bin/npm /usr/local/bin/
sudo ln -s /home/node/bin/node /usr/bin
echo 'nodejs验证'
node -v
npm -v
npm config set registry https://registry.npm.taobao.org

echo '10.安装network-tools'
yum install -y net-tools

echo '11.创建easyci工作目录'
if [[ -d /home/easyci ]];then 
	rm -rf /home/easyci
	mkdir -p /home/easyci
else 
	mkdir -p /home/easyci
fi

echo '12.安装mysql5.7'
yum -y install http://dev.mysql.com/get/mysql-community-release-el7-5.noarch.rpm
yum -y install mysql mysql-devel mysql-server mysql-utilities
echo '启动mysql'
systemctl start mysqld
systemctl enable mysqld
echo '设置mysql密码为123456'
mysqladmin -u root password 123456
echo '导入数据库表'
mysql -uroot -p123456 < work/easy-ci.sql

echo '13.拷贝shell脚本'
\cp -r sh /home/easyci/
\cp -r startEasyCi.sh stopEasyCi.sh /home/easyci/
chmod a+x /home/easyci/*

echo '14.安装前端程序'
\cp -r -f work/dist/* /usr/share/nginx/html/
\cp -r -f work/nginx.conf /etc/nginx/

echo '15.安装后端程序'
\cp -r -f work/*.jar /home/easyci/

echo '========================系统安装完成======================='
echo '================访问路径为http://本机ip:81================='
echo '===========本系统日志路径为/home/easyci/nohup.out=========='
echo '===============项目部署日志路径为/home/log/================'
echo '=============项目工作路径为/home/workspace/================'
echo '========前端静态文件存放路径为/usr/share/nginx/html========'
echo '=========nginx配置文件路径为/etc/nginx/nginx.conf=========='
echo '==============数据库连接为127.0.0.1::3306=================='
echo '======本机占用端口为3306、9875、5000、22、25、323、81======'
echo '=========由于本系统需要在本地打包测试容器运行情况=========='
echo '===============容器运行良好再发布到其他服务器=============='
echo '====================请不要使用上述端口====================='
echo '+++++++++++++++++++++++++++++++++++++++++++++++++++++++++++'
echo '+++++++++++++++++++++++++++++++++++++++++++++++++++++++++++'
echo '++++++++++++++系统配置文件路径为/home/easyci+++++++++++++++'
echo '+++++++++++系统开启/home/easyci/startEasyCi.sh+++++++++++++'
echo '+++++++++++系统关闭/home/easyci/stopEasyCi.sh++++++++++++++'
echo '+++++++++++++++++++++++++++++++++++++++++++++++++++++++++++'
echo '+++++++++++++++++++++++++++++++++++++++++++++++++++++++++++'
echo '！！！！！！！！！系统配置文件不可删除！！！！！！！！！！ '
echo '！！！！！！！！！系统配置文件不可删除！！！！！！！！！！ '
echo '！！！！！！！！！系统配置文件不可删除！！！！！！！！！！ '
echo '+++++++++++++++++++++++++++++++++++++++++++++++++++++++++++'
echo '+++++++++++++++++++++++++++++++++++++++++++++++++++++++++++'
