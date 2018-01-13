Project Sunshine from Google Developer's Challenge 2017/2018

The original source code is licensed by Google, but all the changes are made by myself as
a challenge to learn Kotlin with RxJava and other awesome libraries. 

For instance, I've swapped the original weather API for the Weatherbit one, when testing, please 
insert your own API key there. Then I've removed the content provider and replaced it with Realm 
database. And lastly, I've put all the networking into Retrofit for easy maintenance and scalability.

For your own implementation, create a gradle.properties file, put a variable `MyOpenWeatherMapApiKey="YourKeyHere"` there and it should work fine :)