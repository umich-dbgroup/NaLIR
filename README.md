## Running NaLIR 

### Downloads

First, download necessary jars from:

https://s3.amazonaws.com/umdb-users/cjbaik/nalir_jars.tar.gz

and unzip it into `NaLIR/lib`.

Second, download the SQL files needed from:

https://s3.amazonaws.com/umdb-users/cjbaik/mas.sql

and load it into a running MySQL database on your machine.

Also, load `setup_mas.sql` in the root project folder into MySQL as well, which adds some additional features to the database that are needed to execute it.

### Configuration

There are some hard-coded paths (to schema information and the like) in the original code that need to be modified. Executing it will give you the errors that will point you in the right direction, but at the very least, the following should be modified for your local machine:

* `architecture/CommandInterface.java`
    * Line 51: path to the corresponding file on your machine
* `architecture/Experiments.java`
    * Lines 29-31: your MySQL configuration info
    * Line 53: path to the corresponding file on your machine
    * Lines 59-70: setting the correct query to run the test for
* `rdbms/RDBMS.java`
    * Lines 22-24: your MySQL configuration info
* `rdbms/SchemaGraph.java`
    * Line 33: path to corresponding file on your machine
    * Line 81: path to corresponding file on your machine

### Execute

You can either spin up an Apache Tomcat Server (the configuration should be setup for IntelliJ IDEA Ultimate Edition currently) and head to `/nalir.jsp` in your browser, or, alternatively, execute `Experiments.java` with some modification to run your tests (not recommended).

## Questions?

Contact cjbaik at umich dot edu
