# soap4j
This is a conversion of the old IBM SOAP contribution to Apache into a Maven project. The code is based on Apache SOAP (WS) 1.2.

I needed this code for use in another project which was being upgraded to Maven, and needed this code as a dependency so I decided to
convert it to Maven as well. There are a number of dependencies which are very old that are included in the **lib/** directory. They are
not generally available on a global Maven repository so they could not be included as dependencies.

### note:
You may receive warnings from Maven when you compile the code because it does not like the dependencies being defined in the code base.
