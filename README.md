# Cappuccino Objective-J Intellij Idea Plugin
This repository holds the proof of concept code for a cappuccino objective-j language plugin.

This plugin is mostly proof of concept, and not guaranteed to work properly, or at all.

As of right now, there is mostly working selector, variable name and function name suggestions.
Selector suggestions do not reference type, as type inference is outside the scope of this plugin. 
In cases where type is not specifically declared, selectors for any class will appear.

Selector rename is brute force meaning any method throughout the project with the same selector will be renamed
despite whether or not this is desired.

Use at your own risk

This project is public because I won't have a chance to work on it for a while. Maybe someone else will

This project was meant to be a test at writing a language plugin. 
It has since spiraled out of control, so despite their necessity, 
there are almost no comments, and the code is poorly written and disorganized.

Possible Future
Improve code comments and quality
Infer variable types
Scope suggestions by import
Method completion for protocol implementation
Error on incomplete protocol implementation

###Install Instructions
If you're feeling brave, download the "Cappuccino Objective-J Plugin.jar" file in the plugin folder in this repository, and install it into intellij by going to Preferences(or Settings)->plugins->"install plugin from disk", Select the jar, and enjoy