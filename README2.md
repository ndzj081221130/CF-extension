CF-extesion使用文档
====================

前言
------------------
本文为CF-extesion的使用文档，该平台实现了对应用动态更新的支持



源码下载
---------------
  首先从[gitlab][6]上下载源码,如果是从别人处copy的源码，一定要注意文件夹以及可执行文件的权限问题。否则部署应用的时候，会出现没有权限等问题。
  
  
host machine环境设置
-------------------
 
我们的实验环境是Ubuntu 12.04，如果需要使用git，则需要先下载git

```
sudo apt-get install git 

```

从gitlab上下载的源码，需要使用[Vagrant][1]和[VirtualBox][2]工具，开启一个虚拟机

首先下载vagrant安装包，
Download it from http://www.vagrantup.com (version 1.2 or higher)
Install required plugins: 

```
 vagrant plugin install vagrant-berkshelf
 
 vagrant plugin install vagrant-omnibus

```




根目录下的VagrantFile设置了虚拟机的内存、cpu、使用的虚拟机镜像等信息，可做相应的修改

启动虚拟机
------------------------

```
vagrant up

```

vagrant up需要联网进行，因为有一些下载工作。


此时如果是第一次创建虚拟机，那么需要较长的时间，首先是从远程下载一个虚拟机镜像，然后根据VagrantFile中的设置新建一个Ubuntu系统的虚拟机实例。

当vagrant up成功时，vagrant ssh进入虚拟机。

#### 如果报错chef-golang没有访问权限，则执行./rm.sh
这个错误的原因，可能是因为vagrant使用chef工具安装go-lang失败。

进入虚拟机
-------------------


```
vagrant ssh

```


安装虚拟机环境：git、ruby、java等
-------------------------

#### 1 首先安装git，

```

 sudo apt-get upgrade

 sudo apt-get install git


```
可能会出现缺少liberror-curl的包，可以手动下载[8]，然后dpkg -i 安装。


####  2 接着安装ruby

安装rbenv

```
git clone git://github.com/sstephenson/rbenv.git ~/.rbenv
git clone git://github.com/sstephenson/ruby-build.git ~/.rbenv/plugins/ruby-build
git clone git://github.com/jamis/rbenv-gemset.git  ~/.rbenv/plugins/rbenv-gemset
git clone git://github.com/sstephenson/rbenv-gem-rehash.git ~/.rbenv/plugins/rbenv-gem-rehash

```
下面 将 rbenv 加入到 $PATH 里

```
vi ~/.bash_profile
```

输入的内容：

```
export PATH="$HOME/.rbenv/bin:$PATH"
eval "$(rbenv init -)"
```
最后执行

```
source ~/.bash_profile
```

安装ruby

```
rbenv install --list  # 列出所有 ruby 版本
rbenv install 1.9.3-p392     # 安装 1.9.3-p392

 ```
 设置版本
 
 ```		
rbenv global 1.9.3-p392      # 默认使用 1.9.3-p392
rbenv shell 1.9.3-p392    # 当前的 shell 使用 1.9.3-p392, 会设置一个 RBENV_VERSION 环境变量
rbenv local 1.9.3-p392      # 当前目录使用 ruby1.9.3, 会生成一个 .rbenv-version 文件

```


#### 3 安装Java

[下载][4],如果下载的版本名为jdk-7u40-linux-x64.tar.gz

```
sudo mkdir /usr/lib/jvm
sudo tar zxvf jdk-7u40-linux-x64.tar.gz -C /usr/lib/jvm
cd /usr/lib/jvm/
sudo mv jdk1.7.0/ java-7-sun 

```

设置环境变量
vim ~/.bash_profile 


```
export JAVA_HOME=/usr/lib/jvm/java-7-sun  
export JRE_HOME=${JAVA_HOME}/jre  
export CLASSPATH=.:${JAVA_HOME}/lib:${JRE_HOME}/lib  
export PATH=${JAVA_HOME}/bin:$PATH 
source ~/.bash_profile  


```

#### 4 install maven : 

```
  sudo apt-get install maven
  
```
  
#### 5 安装make : 

```
  apt-get install build-essential

```

#### 6 安装zip、golang，curl等，


```

  cd /vagrant 
  
  sudo apt-get install golang-go
  
  gem install bundle
  
  apt-get install zip
	
  apt-get install curl	
```

#### 7 执行Rake脚本

```
  rake cf:bootstrap 
  
```
 （在执行这一步之前，需要保证ruby，java，mvn等都已安装）

如果执行失败，可以修复错误后，多次执行该脚本

执行成功后，安装就告一段落


设置warden容器
---------------------------
[参考][5]



#### 1 首先是warden需要setup，安装warden前准备工作：debootstrap，quota，




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

###如果因为更新系统导致/vagrant不见了

```

  sudo apt-get install linux-headers-‘uname -r’ 

  vagrant plugin installvagrant-vbguest
  
  gem install plugin vagrant-vbguest


```

然后ssh进虚拟机就有共享目录了。(最好就是不要安装linux镜像)


#### 3 运行warden-server
然后可以在warden/warden路径下开启warden server，并且可以开启warden client，与warden server进行交互。
如果在cf中启动warden，记得去掉bin/warden中的sudo。
在bin/warden第4行，删除rbenv

```
  sudo bundle exec rake warden:start[config/linux.yml]

```

#### 4 在warden container中安装软件

warden是cloud foundry v2中用来进行进程、资源隔离的一个重要组件，但是由于warden本身比较复杂，因此我在warden的setup，start，以及生成的stemcell中花费了大量的时间。warden setup时，会使用debootstrap，从宿主机器上克隆一个stemcell的镜像。

  我的问题在于，虽然环境变量成功clone了，但是文件共享这里却出了问题。
比如镜像挂载，等等。所以我宿主机器的软件，在stemcell中都没有，很多需要手动安装
首先进入/var/warden/rootfs

```
cd /var/warden/rootfs
sudo chroot .
 
```

进入warden container容器中安装软件：zip，quota，gcc，wget, ruby，jvm


```
apt-get update 
apt-get install zip
apt-get install quota
apt-get install gcc
apt-get install wget
apt-get install curl



```

后面两个要使用源码安装。在stemcell中用rbenv安装ruby时，总是会出问题，于是只能源码安装了，在安装前，必须安装一些依赖库。


安装ruby前，先安装yaml，否则报错


```




    $ wget http://pyyaml.org/download/libyaml/yaml-0.1.4.tar.gz

    $ tar xzvf yaml-0.1.4.tar.gz

    $ cd yaml-0.1.4

    $ ./configure --prefix=/usr/local

    $ make

    $ make install

```

然后安装ruby


```

    #下载ruby-1.9.3-p392.tar.gz

    wget http://ftp.ruby-lang.org/pub/ruby/1.9/ruby-1.9.3-p392.tar.gz

    #解压，tar -xzvf ruby-1.9.3-p392.tar.gz

    cd ruby-1.9.3-p392

    ./configure --prefix=/usr --enable-shared --disable-install-doc --with-opt-dir=/usr/local/lib

    #编译并安装，即
    make && make install
    
```


启动Cloud Foundry各组件
----------------------------

现在你就可以启动你本地的CloudFoundry了。

```
  cd /vagrant
  
  ./start.sh #启动各组件
  
  ./stop.sh #停止组件运行
  
  ./status.sh #查看当前组件运行状态

```

运行各个组件后，通过status查看其状态，当所有的组件都运行成功后，说明Cloud Foundry平台搭建成功。



创建Cloud Foundry用户
------------------------

下面是创建用户，运行bin/init_cf_cli脚本中执行的命令


```
  cf target http://api.192.168.12.34.xip.io:8181
  
  cf login --username admin --password password
  
  cf create-org myorg
  
  cf create-space myspace
  
  cf switch-space myspace
  
  cd test/helloworld-jsonrpc
  
  cf push

```


生成tuscany-buildpack
--------------------


我们部署的实验环境是Tuscany应用，因此需要创建tuscany-buildpack
####  由于cf rake:bootstrap时，dea的buildpack目录会被更新，因此原来的tuscany-buildpack将被删除，。我们可以从shell目录下将tuscany-buildpack拷贝到dea_ng/buildpacks/vendor目录下

我们在dea_ng/buildpacks/vendor/目录下创建tuscany-buildpack，用于部署tuscany应用。由于tuscany源码在运行后，shell模块会等待用户输入，而这里会导致部署在CF上的应用因长时间没有响应而crash，而报错则会指导oom。因此，我们对Tuscany源码的shell部署进行了修改，注释掉等待用户输入的部分。

并且为tuscany应用定制buildpack打包机制。类似于tomcat，首先下载java源码、部署java运行环境，然后下载修改后的tuscany源码，部署tuscany环境，主要是设置JAVA_HOME,TUSCANY_HOME。然后启动tusncay，运行tuscany.sh app.

下载源码时，需要从服务器拖，或者在warden-container处建立一个cache，我们选择在cache处建一个下载资源的缓存池。

```
cd /var/warden/rootfs
sudo chroot .
mkdir -p /var/vcap/packages/buildpack_cache



```
我们在这个缓存目录下存放java-jdk.jar, tuscany.jar,



部署应用
-----------------
现在可以去本地的app

为了识别出当前应是tuscany应用，我们在helloworld-jsonrpc目录下建立version.tuscany文件，指明使用的tuscany源码版本。

```
cd /vagrant/test/helloworld-jsonrpc
mvn install -DskipTests (跳过测试，因为tuscany版本的问题，有一些测试用例跑不过去)
cf psuh 

```

实验是
可以直接运行./push.sh
或者进入每个目录运行部署命令cf push


通过cf apps查看应用的状态。


通过curl http://someuser:somepassword@10.0.2.15:8081/routes，查看应用是否成功在路由器上注册。这里，如果返回的是{}，说明注册失败。（这里是一个bug，尚未修复）。只能手动kill掉router进程，在重新运行。


```
ps ax|grep router #然后删除router进程，一般有两个
cd /vagrant/gorouter
sudo ./bin/router -c =/vagrant/custom_config_files/gorouter/gorouter.yml


```

再次查看在router上注册的应用。此时应用有多个应用成功注册。



测试动态更新
---------------
运行HTTP中对Portal的定期访问，并且执行auth更新操作。


```
java -jar papa.jar

cd /vagrant/dea_ng/lib/dea/conup_test
ruby update_hello.rb

```


常见错误
----------------------

##1  push java应用出现oom 

 是因为CF不支持带有命令行读入操作的应用

tuscany的源码支持从命令行读入用户输入的命令，这会导致该进程阻塞。而CF认为这样的阻塞是不合理的，因此会kill掉当前进程。

将tuscany源码中的shell模块的

##2 安装gem失败
删除根目录下的.gem文件夹。再试试。（遇到marshaldata too short，删除.gem，重新执行一遍）


## 3 启动warden后可能会报错 

quotaon:Mountpointor device)/not found or has no quota enabled

解决方法，重启quota，这个是网上给的解决方法，我这里出现这个错误应该是warden中没有安装quota。还是需要quotaon -a的。

```
  quotaoff –a 
  
  quotacheck –avugm
  
  quotaon -a   

```


## 4 error:cannot  loadsuch file -- zlib 


安装一下zlib1-dev

```
sudo apt-get install zlib1g-dev
```


## 5 bundle install 遇到 ExtensionBuildError问题时


尝试一下安装ruby-dev，或者ruby1.8-dev或者ruby-1.9dev，这取决于你出问题的ruby版本，这里我使用的是

```
sudo apt-get install ruby1.9.1-dev

```

我是在本地化CloudFoundry时，遇到各种gem install问题的

安装bcrypt-ruby失败
尝试解决1：rm –rfvendor/bundle
 
atomic安装失败
安装ruby1.9.1-dev，

```
sudo apt-get install ruby1.9.1-dev

```

安装到nokogiri时，遇到“libxml2is missing”

再执行

```
sudo apt-get install libxslt-dev libxml2-dev 

```


## 6 cc在mysql2失败
```
sudo apt-get install libmysql-ruby libmysqlclient-dev

```
结果pg失败，

```
sudo apt-get install libpq-dev

```
sqlite3失败


```
sudo apt-get install libsqlite3-dev

```



## 7 CFoundry::AppPackageInvalid: 150001: The app package is invalid: failed repacking application

可能是warden容器中没有安装zip

sudo chroot .到stemcell中手动安装的
首先
 apt-get update 
然后 

```
apt-get install quota gcc iptables zip

```



## 8 install vagrant vagrant-berkshelf plugin 失败

主要是nokogiri安装失败，解决如下：


```

sudo apt-get install zlib1g-dev lib64z1-dev

NOKOGIRI_USE_SYSTEM_LIBRARIES=1 vagrant plugin install vagrant-berkshelf


```

## 9 gorouter启动失败

第一次连接NATS server时，是因为nats还未启动成功，因此dial tcp 127.0.0.1:4222 : connection regused，这个属于正常行为。
如果连接失败。每隔1s会发起一次连接请求。


但第二次连接时，报错json: cannot unmarshal null into Go value of type bool，应该是哪里传了一个空指针。

log发现，host，user，pass都是正确的。

在执行r.natsClient.RunWithDefaults(host,user,pass)报错。

重启，重新启动都失败。

src/router/logger.go:4: import /vagrant/gorouter/pkg/linux_amd64/github.com/cloudfoundry/gosteno.a: object is [linux amd64 go1.0.3 X:none] expected [linux amd64 go1 X:none]

重新生成router可执行文件

```
 ./bin/go  install router/router

```

这里应该是go版本的问题 ， 粗暴解决

```
go install -a -v all

```




## 10 warden容器抛出oom错误

有可能真是oom，那么此时就需要增加内存

能正常cf push nodejs应用(设置内存限制256M)，java-web应用(设置内存限制512M)

然后跑php，jboss/ejb， tuscany/sca应用时，就开始报oom了。

 
 
1 扩大Vagrant虚拟机内存
2 扩大应用内存：无效
3 debug。。。


这里是因为CF不支持部署等待命令行输入的应用






-----------------



遇到错误多查看日志
------------------------

/vagrant/logs主要查看dea和warden的日志

/tmp/dea_ng/目录是dea_ng部署的临时文件夹，也可以查看部署的droplet等

可以通过warden-client查看warden-server是否运行正常



[1]:http://www.vagrantup.com
[2]:http://www.virtualbox.org
[3]:http://packages.ubuntu.com/vivid/all/liberror-perl/download
[4]:http://www.oracle.com/technetwork/java/javase/downloads/index.html?ssSourceSiteId=ocomen
[5]:http://docs.cloudfoundry.com/docs/running/architecture/warden.html
[6]:http://lab.artemisprojects.org/jiezhang/CF-extension
