echo '启动web'
systemctl start nginx
echo '启动后台'
nohup java -jar /home/easyci/*.jar > /home/easyci/nohup.out &
echo '启动数据库'
systemctl start mysql
