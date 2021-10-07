## Running NaLIR 

### Downloads

First, download necessary jars from:

https://drive.google.com/file/d/1GT3xc_h23Rv36bkWu5zxIKwSwU9oX-gS/view?usp=sharing

and unzip it into `NaLIR/lib`.

Second, download the SQL files needed from:

https://drive.google.com/file/d/1xN1RW_q99kK5nhm5KSGftaHMMvgemvSu/view?usp=sharing

and load it into a running MySQL database on your machine.

Also, load `setup_mas.sql` in the root project folder into MySQL as well, which adds some additional features to the database that are needed to execute it.

### Configuration

There are some hard-coded paths (to schema information and the like) in the original code that need to be modified. Executing it will give you the errors that will point you in the right direction, but at the very least, the following should be modified for your local machine:

* `architecture/CommandInterface.java`
    * Line 52: path to the corresponding file on your machine
* `rdbms/RDBMS.java`
    * Lines 22-24: your MySQL configuration info
* `rdbms/SchemaGraph.java`
    * Line 33: path to corresponding file on your machine
    * Line 81: path to corresponding file on your machine

### Execute

You can either:
* Spin up an Apache Tomcat Server (the configuration should be setup for IntelliJ IDEA Ultimate Edition currently) and head to `/nalir.jsp` in your browser at the right port
* Execute `CommandInterface.java` using some of the following example commands to use it interactively
    * `#useDB mas` - initial setup, loads the MAS database
    * `#query return me the homepage of PVLDB` - run a query
* Execute `Experiments.java` with some modification to run your tests

## Questions?

Contact cjbaik at umich dot edu
