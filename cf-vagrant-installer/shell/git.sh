#!/bin/sh 


sudo apt-get install -y git
sudo apt-get install -y golang-go
sudo apt-get install -y zip
sudo apt-get install -y build-essential
sudo apt-get install -y maven

git clone git://github.com/sstephenson/rbenv.git ~/.rbenv

git clone git://github.com/sstephenson/ruby-build.git ~/.rbenv/plugins/ruby-build

git clone git://github.com/jamis/rbenv-gemset.git  ~/.rbenv/plugins/rbenv-gemset

git clone git://github.com/sstephenson/rbenv-gem-rehash.git ~/.rbenv/plugins/rbenv-gem-rehash


