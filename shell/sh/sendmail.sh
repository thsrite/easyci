#收件邮箱列表
TO_LIST=$1
#邮件标题
MAIL_TITLE=$2
#附件地址
LOG_PATH=$3

fromAdd="mingbyte"
tolist="$TO_LIST"
cclist=""
subject="=?UTF-8?B?`echo $MAIL_TITLE | base64`?="
data=`cat $LOG_PATH`
(
echo "From: $fromAdd"
echo "To: $tolist"
echo "Cc: $cclist"
echo "Subject: $subject"
echo "MIME-Version: 1.0"
echo 'Content-Type: multipart/mixed; boundary="GvXjxJ+pjyke8COw"'
#echo "Content-Disposition: inline"
echo
echo "--GvXjxJ+pjyke8COw"
echo "Content-Type: text/html; charset=US-ASCII"
echo "Content-Disposition: inline"
echo
echo "<h1>Please check the attachment log.</h1>"
echo
echo "--GvXjxJ+pjyke8COw"
echo "Content-Type: text/plain; charset=US-ASCII;"
echo "Content-Disposition: attachment;filename="build.log""
echo 
echo "$data"
echo
echo "--GvXjxJ+pjyke8COw"
) | /usr/lib/sendmail -t
exit 0
