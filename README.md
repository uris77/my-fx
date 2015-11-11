# my-fx

A project that displays a bug in fx-clj. When we run the uberjar, and we double click on another window (the terminal for example), the application crashes.

Steps to replicate:

```
lein uberjar
java -jar target/my-fx.jar
```

Then double click on the terminal or open another tab or double click on any other window.


![Crash Demo](https://github.com/uris77/my-fx/blob/master/fx-crash.gif)

Copyright © 2015 FIXME

Distributed under the Eclipse Public License either version 1.0 or (at
your option) any later version.
