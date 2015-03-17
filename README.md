# BierApp-Android
Drink management system for student houses. This is the client (POS) part.

## Introduction
This projected is a functional point-of-sale (POS) Android application that can be used in conjunction with [BierApp-Server](https://github.com/basilfx/BierApp-Server). It has the following features:

* Full product catalog.
* Easy access with offline support.
* Support for guests that can drink/consume on the account of others.
* Optional XP system.
* Transaction editor for advanced transactions

## Screenshots
![Login screen](https://raw.github.com/basilfx/BierApp-Android/master/docs/screenshots/home.png)

![Dashboard](https://raw.github.com/basilfx/BierApp-Android/master/docs/screenshots/search.png)

![Statistics](https://raw.github.com/basilfx/BierApp-Android/master/docs/screenshots/editor.png)

## Requirements
* Eclipse 4
* Android Tools
* BierApp Server

## Installation
* Install the dependencies.
* Import this project into Eclipse
* Configure the endpoint in `BierAppApplication.java`, together with API key and catch URL.

## Todo
There is a lot of work to do to make it a better and more manageable system. Nonetheless, it is a working product that has served 3000+ transactions without problems.

* English translation
* Faster API responses (currently +/- 5 seconds when saving a transaction.)

## License
See the `LICENSE` file (GPLv3 license). You may change the code freely, but any change must be made available to the public.

This project makes use of the following libraries:

* [Google Guava](https://code.google.com/p/guava-libraries/)
* [Google GSON](https://code.google.com/p/google-gson/)
* [Universal ImageLoader](https://github.com/nostra13/Android-Universal-Image-Loader)
* [ORMLite](http://ormlite.com/)
* [AdapterKit](https://github.com/mobsandgeeks/adapter-kit)