JDKPATH = /Library/Java/JavaVirtualMachines/jdk1.7.0_79.jdk/Contents/Home
LIBPATH = .:..
CLASSPATH = $(LIBPATH)
BINPATH = $(JDKPATH)/bin
JAVAC = $(JDKPATH)/bin/javac -classpath $(CLASSPATH)
JAVA  = $(JDKPATH)/bin/java  -classpath $(CLASSPATH)

# test buffer manager

BMTest:BMTest.java
	$(JAVAC) TestDriver.java BMTest.java

bmtest: BMTest
	$(JAVA) tests.BMTest

# test disk manager

DBTest:DBTest.java
	$(JAVAC) TestDriver.java DBTest.java

dbtest: DBTest
	$(JAVA) tests.DBTest

# test heapfile 

HFTest:HFTest.java
	$(JAVAC) TestDriver.java HFTest.java

hftest: HFTest
	$(JAVA) tests.HFTest

# test btree

BTTest:BTTest.java
	$(JAVAC) TestDriver.java BTTest.java

bttest: BTTest
	$(JAVA) tests.BTTest

# test index

IndexTest:IndexTest.java
	$(JAVAC) TestDriver.java IndexTest.java

indextest: IndexTest
	$(JAVA) tests.IndexTest




