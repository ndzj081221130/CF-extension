#!/bin/sh 

 apt-get update 
apt-get install -y quota gcc iptables zip wget curl


wget http://pyyaml.org/download/libyaml/yaml-0.1.4.tar.gz  
  
      tar xzvf yaml-0.1.4.tar.gz  
  
     cd yaml-0.1.4  
  
      ./configure --prefix=/usr  
  
      make  
  
      make install  

cd ..

wget http://ftp.ruby-lang.org/pub/ruby/1.9/ruby-1.9.3-p392.tar.gz 
tar -xzvf ruby-1.9.3-p392.tar.gz 
cd  ruby-1.9.3-p392 
    ./configure --prefix=/usr --enable-shared --disable-install-doc --with-opt-dir=/usr/local/lib  

make 

 make install 
