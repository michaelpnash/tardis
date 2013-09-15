matrix-decider
==============

Simple webapp for making rational decisions amongst several competing alternatives.

A [running version can be found here](http://50.116.19.42:9000/) if you'd like to experiment.

Building:

1. Clone the repository
2. cd into the repo's directory
3. issue ./sbt to launch sbt's interactive mode, or use ./sbt {cmd} to run a specific command and exit.
4. From within sbt, use "run" to start the application running on the default port (9000).
5. Use sbt dist to create a zip file containing a runnable version of the application. When you unzip the resulting file, the "start" script can be used to launch the application.

Matrix decider uses a built-in HSQLDB database - by default, this will use an in-memory instance of Hypersonic, meaning your data will go away when you shut down the application.

If you specify -Ddb=file:/somedir/matrix as a system property when running, HSQL will use the specified directory, and the specified name (matrix, in this case) as the database name. You can also use the Server-mode of HSQL if you're running multiple instances of Matrix-Decider and need them to talk to the same data store.

Usage:

When you go to the running application's starting page in your browser, you will be greeted by an explanatory page, and prompted for a username. Enter "guest" to see a demonstration decision, or enter a new username to create your own decisions. There is no security (at the moment), so proceeed at your own risk!

Once you've entered a username, you'll be prompted with a list of your saved decisions (if any), and a prompted for a name for a new decision. Either click on an existing decision to work with it, or enter a new name to add one to the list.

When you've selected a decision, you will be prompted for Alternatives and Criteria, along with an explanation of the meaning of both items.

As you change the rankings of each alternative for each criteria, or as you change the importance of different criteria, the alternatives will be shown in order of preference, with the most suitable alternative at the top of the list, the leas suitable at the bottom.

Feedback is most welcome, and I plan a number of new features and extensions to the app over time!

Enjoy!

A blog post explaining how and why Matrix Decider was made is now [available here](http://php.jglobal.com/blog/?p=1377)

