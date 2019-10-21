sudo echo "grant_type=password&username=$1&password=$2" > /root/auth.txt
sudo curl --data @/root/auth.txt --request POST $3/oauth/token
