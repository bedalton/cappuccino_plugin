# Cappuccino Objective-J Intellij Idea Plugin
This repository holds the proof of concept code for a cappuccino objective-j language plugin.

This plugin is mostly proof of concept, and not guaranteed to work properly, or at all.

### Working
- Non-Intelligent method call completion
- Variable name autocompletion
- Variable, Function and Method declaration resolution (CMD+click)
- Syntax Highlighting
- Invalid method call selector warnings

### Notes
- Variable type resolution is not fully/or correctly implemented, 
so intellisense may suggest methods that do not exist for the given variable
- Suggestions do not account for import, suggesting things not imported or in scope
- This plugin requires the original source files for the cappuccino framework to work which can be downloaded
  from the Cappuccino github page([link](https://github.com/cappuccino/cappuccino "Cappuccino's GitHub Page"))

### Possible Future
- Improve code comments and quality
- Add tests
- Infer variable types
- Scope suggestions by import
- Warn when using classes or methods not imported, with appropriate import action
- Method completion for protocol implementation
- Error on incomplete protocol implementation

### Note on Quality
This project was meant to be a test at writing a language plugin, and as a way to learn
more about programming in general, as I don't have much experience. 
It has since spiraled out of control, so despite their necessity, 
there are almost no comments, and the code is poorly written and disorganized.

### Install Instructions
Download the "Cappuccino Objective-J Plugin.jar" file in the plugin folder in this repository, and install it into intellij by going to Preferences(or Settings)->plugins->"install plugin from disk", Select the jar, and enjoy