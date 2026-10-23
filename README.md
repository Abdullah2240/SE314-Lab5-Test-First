# SE-314 Lab 7: Social Network Analysis

This project continues the PS1 Twitter work from Lab 5. `SocialNetwork.java`
infers follows from `@`-mentions and ranks users by follower count.

Run `run-tests.ps1` from PowerShell to compile the project and run the Extract,
Filter, and SocialNetwork JUnit tests. Use `-SocialOnly` for the Lab 7 tests.
The script uses the Java 8 JDK in the adjacent Lab 4 folder when it is present;
otherwise it uses `java` and `javac` from `PATH`.

`Main.java` normally requests tweets from the MIT sample server. To run it
without that server, compile the project with `run-tests.ps1`, then run:

```powershell
powershell -ExecutionPolicy Bypass -File .\run-main.ps1 -SampleFile .\sample-tweets.json
```

The JSON file is a small local fixture for checking the program flow. It is
not a live Twitter sample.
