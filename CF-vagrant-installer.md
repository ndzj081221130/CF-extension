《cf-vagrant-installer安装文档》
===============================


参考地址：

[cf-vagrant][1].

[cf-vagrant-installer][2].

[google-group][3].





host machine 
-----------------

（即实验机器，Ubuntu 12.04）


Install过程
------------



首先安装git，

```

 sudo apt-get upgrade

 sudo apt-get install git


```

可能会出现缺少liberror-curl的包，可以手动下载[8]，然后dpkg -i 安装。




主要是遵循github上的steps
首先下载vagrant安装包，
Download it from http://www.vagrantup.com (version 1.2 or higher)
Install required plugins: 

```
 vagrant plugin install vagrant-berkshelf
 
 vagrant plugin install vagrant-omnibus

```
[安装ruby1.9.3][5]
Ruby 1.9.3

然后clone cf-vagrant-installer project

```

  git clone https://github.com/Altoros/cf-vagrant-installer.git
  
  cd cf-vagrant-installer	

  rake host:bootstrap 

```

  这里是执行“git submodule update --init --recursive”
最大的问题是git clone时会end of file，超时之类的。需要翻墙

下面登录vagrant

```
  vagrant up
  
  vagrant ssh
  
  
```

install ruby 1.9.3 : [参考][5]

install git : 

```

  sudo apt-get install git 
  
```
  
install jvm : [参考][6]

install maven : 

```
  sudo apt-get install maven
  
```
  
make : 

```
  apt-get install build-essential

```


连上vagrant虚拟机后，最关键的一步来了！


```

  cd /vagrant 
  
  sudo apt-get install golang-go
  
  gem install bundle
  
  apt-get install zip

```


** before this, exe something[参考][7] **

```
  rake cf:bootstrap 
  
```
 （在执行这一步之前，需要保证ruby，java，mvn等都已安装）
不知道为什么git的文档上没有提及这一步，我也是在google groups上发现的.这一步应该是在第一次agrant up时就执行的，但是 由于各种问题，失败了，所以需要手动执行
如果不执行这一步，在下面的./start.sh时，会报错
" initctl Unknown job: cf "

 
现在你就可以启动你本地的CloudFoundry了。

```
  cd /vagrant
  
  ./start.sh
  
  ./stop.sh
  
  ./status.sh

```


现在终于可以去本地跑我的app啦~
这个是bin/init_cf_cli脚本中执行的命令
cf target api.run.pivotal.io (这个target到官方的cloud Foundry)
这里我们应该

```
  cf target http://api.192.168.12.34.xip.io:8181
  
  cf login --username admin --password password
  
  cf create-org myorg
  
  cf create-space myspace
  
  cf switch-space myspace
  
  cd test/helloworld
  
  cf push

```


先记录到这里，因为我跑本地app还有点问题，虽然push成功了，但是进度一直是0%，不知道什么原因，我来试试，再更新blog！


安装Warden环境
-----------------------




这里离成功还是很远的啊
#### 1 首先是warden需要setup，安装warden前准备工作：debootstrap，quota，


[参考][4]

```
  sudo apt-get install debootstrap
  
  sudo apt-get install quota

```


#### 2 setup warden container, 进入目录warden/warden执行
 
```
 sudo bundle exec rake setup[config/linux.yml] 

```


 这里可以将warden/config/linux.yml安装stemcell的路径改为/var/warden/rootfs，否则每次关闭vagrant，stemcell就没了。
 
rake setup在/tmp/warden目录下，使用debootstrap和chroot工具创建了一个linux系统。
(ubuntu12.04是不需要安装linux-image，这里跟有些教程不同。如果是ubuntu10.04的镜像，需要安装backport-natty)
http://kvz.io/blog/2013/01/16/vagrant-tip-keep-virtualbox-guest-additions-in-sync/
安装linux-header和virtual-box-guest

```

  sudo apt-get install linux-headers-‘uname -r’ 

  vagrant plugin installvagrant-vbguest
  
  gem install plugin vagrant-vbguest


```

然后ssh进虚拟机就有共享目录了。(最好就是不要安装linux镜像)

然后可以在warden/warden路径下开启warden server，并且可以开启warden client，与warden server进行交互。
如果在cf中启动warden，记得去掉bin/warden中的sudo。
在bin/warden第4行，删除rbenv

```
  sudo bundle exec rake warden:start[config/linux.yml]

```





before going on, 
we should install something in warden container
refer to [this][]


#### 3 这里启动warden后可能会报错 

quotaon:Mountpointor device)/not found or has no quota enabled

解决方法，重启quota，这个是网上给的解决方法，我这里出现这个错误应该是warden中没有安装quota。还是需要quotaon -a的。

```
  quotaoff –a 
  
  quotacheck –avugm
  
  quotaon -a   

```



#### 4 如果cf-warden.log日志提示没有quota，iptables，gcc，zip。

我是sudo chroot 到stemcell中手动安装的
首先

```
  apt-get update 

```


然后 

```
  apt-get install quota gcc iptables zip

```


#### 5 如果warden.log还是提示setup脚本中没有就iptables或者quota，


那么在脚本运行setup前，强制设置path，或者是在所有出现quota和iptables的地方加上/sbin/

比如在warden/root/linux/setup.sh第56，61，63行填上/sbin/quota,或者最root/linux/setup.sh执行./net.sh前，强制设置环境变量

export PATH=/sbin:/bin:/usr/bin:/usr/local/bin:/usr/sbin
应该是设置root/linux/skeleton中到setup.sh或者修改iptables。
在root/linux/skeleton/start.sh执行./net.sh和./bin/wshd之前加上环境变量export PATH=/sbin:/bin:/usr/bin:/usr/local/bin:/usr/sbin.

#### 6 如果warden启动后，日志中有execvp：No such file or directory错误，应该是路径中没有加入/usr/sbin。
这个有点奇怪，因为stemcell中的path路径是复制的我的虚拟机的path路径，应该是加入了这个/usr/sbin的
于是在warden/warden/src/closefds/closefds.c中
main函数，
在execvp(argv[1],&argv[1])之前
添上：

```
	char * path = getenv("PATH");

	char * tmp = ":/usr/sbin";

	strcat(path,tmp);

	setenv("PATH",path,1);
	
```

重新编译closefds

####7 如果warden启动后，日志报错stdin: is not a tty 。


这个一般是在cf push的时候，当前面的iptables，quota的问题都解决后可能出现的问题。 
msg n
With:

tty -s && mesg n
In file /root/.profile.

#### 8 在dea_ng/go/src/direcotryserver/log.go第7行加上指针* 

这显然是个bug，但是开源项目中没有修改，只是在issues中有人提出来了。
这个也很奇怪，编译的时候不能加*，但是运行的时候需要？？？
并在status.sh中加上sudo

#### 9  测试dea_ng和dir_server的启动环境:
bin/test confit/dea.yml
应该会报错：
+ set -e
+ cd go
+ bin/go test -i directoryserver
src/common/logger.go:4:2: import"github.com/cloudfoundry/gosteno": cannot find package
src/common/config.go:5:2: import "launchpad.net/goyaml":cannot find package
在go/目录下执行
sudo bin/go get github.com/cloudfoundry/gosteno
sudo bin/go get launchpad.net/goyaml
(可能需要sudo apt-get install bzr)
再次bin/test config/dea.yml
安装dir_server:


```
  bundle exec rake dir_server:install

```
最后，启动dea_ng和dir_server:

```
  bin/dea config/dea.yml 

  bundle exec rake dir_server:run[config/dea.yml]
  
```

#### 10 如果出现安装gem失败，不妨删除根目录下的.gem文件夹。再试试。

（遇到marshaldata too short，删除.gem，重新执行一遍）

#### 11如果遇到error:cannot  loadsuch file -- zlib ， 安装一下zlib1-dev

```
  sudo apt-get install zlib1g-dev

```


#### 12 cf push时，会从amazon s3服务器上下载所需要的buildpack，感谢我们GFW，我每次都timeout，


于是只能增加延时，其实也没用了。就把包都拖回本地搞了。

####13 修改nodejs应用。

有问题？监听3000号端口，改为process.env.VCAP_APP_PORT

####14 修改cloud-controller.yml中的max_staging_runtime:48000

修改health-manager中的cc-partition，将ng改为default。这个还不知道是不是有用。

修改dea.yml中的localhost_route，改为8.8.8.8好像是不让Health Manager去检查本地的应用的状态。
否则会出现droplet.unhealthy的问题.

修改dea.yml中的max_staging_duration:28000


#### Error
目前成功的在本地搭建起了CloudFoundry实例，可以cf push nodejs应用和spring应用。
ruby应用还有点问题，php的应用push成功，但是instance的状态一直是down的，还需要再研究一下。

 push java应用出现oom , 是因为CF不支持带有命令行读入操作的应用
 
在dea.log中出现droplet.unhealthy
在warden.log中，link返回结果1 （出错）
然后warden接收到DestroyRequest
执行stop.sh
killing oom-notifier process
执行destroy.sh,删除handle



[1]:http://blog.cloudfoundry.com/2013/06/27/installing-cloud-foundry-on-vagrant/#comment-342193

[2]:https://github.com/Altoros/cf-vagrant-installer
[3]:https://groups.google.com/a/cloudfoundry.org/forum/#!msg/vcap-dev/LH_dex3apHg/skEBS1gnfgwJ
[4]:http://docs.cloudfoundry.com/docs/running/architecture/warden.html
[5]:http://blog.csdn.net/nju08cs/article/details/12857247
[6]:http://blog.csdn.net/nju08cs/article/details/12859679
[7]:http://blog.csdn.net/nju08cs/article/details/12887249
[8]:http://packages.ubuntu.com/vivid/all/liberror-perl/download
[9]:http://blog.csdn.net/nju08cs/article/details/13703481