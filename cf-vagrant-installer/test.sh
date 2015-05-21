#ifc = `ifconfig`
#echo $ifc

arg=`/sbin/ifconfig  |grep "inet addr"| cut -f 2 -d ":"|cut -f 1 -d " "`
ipaddr=$arg
echo $ipaddr
tmp=""
for line in $arg
do
  tmp=$line
  echo $tmp
done

echo $tmp
