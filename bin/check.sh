PRG="$0"
PRG_DIR=`dirname "$PRG"`
APP_HOME=`cd "$PRG_DIR/.." ; pwd`


WORKSPACE=$1

export JAVA_HOME=/usr/java/jdk1.8.0_171
export PATH=$JAVA_HOME/bin:$PATH
export CLASSPATH=$APP_HOME/lib/*


JAVA_OPTS="-Xms128m -Xmx128m"
APP_OPTS="-Dproc.key=codecheck"
#
java $JAVA_OPTS $APP_OPTS com.sitech.csd.codecheck.sql.Check  $WORKSPACE  1>nohup.out 2>&1 &