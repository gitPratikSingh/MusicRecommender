#!/bin/bash
# Install Spark on CentOS 7
yum install java -y
java -version

yum install wget -y
wget http://downloads.typesafe.com/scala/2.11.7/scala-2.11.7.tgz
tar xvf scala-2.11.7.tgz
sudo mv scala-2.11.7 /usr/lib
sudo ln -s /usr/lib/scala-2.11.7 /usr/lib/scala
export PATH=$PATH:/usr/lib/scala/bin
scala -version

wget http://d3kbcqa49mib13.cloudfront.net/spark-1.6.0-bin-hadoop2.6.tgz
tar xvf spark-1.6.0-bin-hadoop2.6.tgz

export SPARK_HOME=$HOME/spark-1.6.0-bin-hadoop2.6
export PATH=$PATH:$SPARK_HOME/bin

firewall-cmd --permanent --zone=public --add-port=6066/tcp
firewall-cmd --permanent --zone=public --add-port=7077/tcp
firewall-cmd --permanent --zone=public --add-port=8080-8081/tcp
firewall-cmd --reload

echo 'export PATH=$PATH:/usr/lib/scala/bin' >> .bash_profile
echo 'export SPARK_HOME=$HOME/spark-1.6.0-bin-hadoop2.6' >> .bash_profile
echo 'export PATH=$PATH:$SPARK_HOME/bin' >> .bash_profile
