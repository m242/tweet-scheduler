Tweet Scheduler
===============

See [Tweet Scheduler in action](http://tweet-scheduler.co).

Tweet Scheduler is a simple application that lets you put tweets into a queue
for future broadcast. There are no limitations set upon the end user by the
application, simply due to the changing nature of Twitter's API usage limits.
It's expected that the end user won't schedule 100 tweets to all go out at
the same time.

The application is written in Scala, using the Play Framework, and is designed
to be extremely lightweight. Data storage is done using CouchDB.


Requirements
------------

* [SBT 0.12+](http://www.scala-sbt.org/)
* [PlayFramework 2.1.0+](http://www.playframework.com/)
* [CouchDB 1.2+](http://couchdb.apache.org/)

Tweet Scheduler can run on extremely lightweight hardware. A small EC2
instance should be more than enough to run the application server and CouchDB,
as well as Nginx if you want a web front-end. The expected bottleneck should
be IO to Twitter.


Installation
------------

### Create the CouchDB database

Create a database in CouchDB, and create this design document:

```
{
   "_id": "_design/tweets",
   "language": "javascript",
   "views": {
       "scheduled": {
           "map": "function(doc) {\n  if(doc.posted === false) emit(doc.timestamp, doc);\n}"
       },
       "scheduledByUser": {
           "map": "function(doc) {\n  if(doc.posted === false) emit(doc.userid, doc);\n}"
       }
   }
}
```

### Create the web application

Clone the repository into a local directory.

You should make sure to modify your application.conf to put in a secret key
(you can create one with "play new testapp") and put in your Twitter API key
and secret. Also configure the location of your CouchDB instance.

You should then run "play dist". This will give you a zipfile of the web
app, compiled and ready to go. Run the app per the Play Framework instructions.