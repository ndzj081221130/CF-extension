cd ~/Downloads
curl https://dl.bintray.com/mitchellh/vagrant/vagrant_1.7.2_x86_64.deb
curl http://download.virtualbox.org/virtualbox/4.3.28/virtualbox-4.3_4.3.28-100309~Ubuntu~precise_amd64.deb

sudo dpkg -i vagrant_1.7.2_x86_64.deb
sudo dpkg -i virtualbox-4.3_4.3.28-100309~Ubuntu~precise_amd64.deb

vagrant -v

vagrant plugin install vagrant-berkshelf
 
vagrant plugin install vagrant-omnibus

mkdir CloudFoundry
cd CloudFoundry

git clone git@lab.artemisprojects.org:jiezhang/CF-extension.git

cd CF-extension/cf-vagrant-installer


#启动虚拟机，第一次需要较长时间
vagrant up

vagrant ssh


sudo apt-get upgrade

 sudo apt-get install git



git clone git://github.com/sstephenson/rbenv.git ~/.rbenv
git clone git://github.com/sstephenson/ruby-build.git ~/.rbenv/plugins/ruby-build
git clone git://github.com/jamis/rbenv-gemset.git  ~/.rbenv/plugins/rbenv-gemset
git clone git://github.com/sstephenson/rbenv-gem-rehash.git ~/.rbenv/plugins/rbenv-gem-rehash



cat /vagrant/shell/rbenv.source >> ~/.bash_profile

source ~/.bash_profile

rbenv install --list  # 列出所有 ruby 版本
rbenv install 1.9.3-p392     # 安装 1.9.3-p392

rbenv global 1.9.3-p392      # 默认使用 1.9.3-p392
rbenv shell 1.9.3-p392    # 当前的 shell 使用 1.9.3-p392, 会设置一个 RBENV_VERSION 环境变量
rbenv local 1.9.3-p392      # 当前目录使用 ruby1.9.3, 会生成一个 .rbenv-version 文件


cd /vagrant

curl http://download.oracle.com/otn-pub/java/jdk/8u45-b14/jdk-8u45-linux-x64.tar.gz

sudo mkdir /usr/lib/jvm
sudo tar zxvf jdk-8u45-linux-x64.tar.gz -C /usr/lib/jvm
cd /usr/lib/jvm/
sudo mv jdk1.8.0/ java-8-sun 

cat /vagrant/shell/java.source >> ~/.bash_profile

source ~/.bash_profile


#安装maven make zip
 

sudo apt-get install maven

sudo  apt-get install build-essential

sudo apt-get install golang-go
  
  gem install bundle
  
  apt-get install zip
	
  apt-get install curl	

#执行Rakefile
  rake cf:bootstrap 


#安装warden container

sudo apt-get install debootstrap
  
sudo apt-get install quota

# setup warden


cd /vagrant/warden/warden

# 此操作时间较长

sudo bundle exec rake setup[config/linux.yml]

# 登录warden容器 安装
cd /var/warden/rootfs
sudo chroot .


apt-get update
apt-get install zip
apt-get install quota
apt-get install gcc
apt-get install wget
apt-get install curl

# install ruby
wget http://pyyaml.org/download/libyaml/yaml-0.1.4.tar.gz

tar xzvf yaml-0.1.4.tar.gz

cd yaml-0.1.4

./configure --prefix=/usr/local

make

make install



#下载ruby-1.9.3-p392.tar.gz

wget http://ftp.ruby-lang.org/pub/ruby/1.9/ruby-1.9.3-p392.tar.gz

#解压，tar -xzvf ruby-1.9.3-p392.tar.gz

cd ruby-1.9.3-p392

./configure --prefix=/usr --enable-shared --disable-install-doc --with-opt-dir=/usr/local/lib

#编译并安装，即
make && make install


exit

cd /vagrant


./start.sh #启动各组件

# ./stop.sh #停止组件运行

./status.sh #查看当前组件运行状态

# 登录并部署

cf target http://api.192.168.12.34.xip.io:8181

cf login --username admin --password password

cf create-org myorg

cf create-space myspace

cf switch-space myspace

cd test/helloworld-jsonrpc

cf push



















