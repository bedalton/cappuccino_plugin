# Objective-J Language plugin
Provides basic autocomplete functionality for selector methods and variable names

#### Note
- Variable type resolution is not accurately implemented which causes suggestion of any selector even if not applicable to variable
- This plugin requires the original source files for the cappuccino framework to work which can be downloaded
from the Cappuccino github page([link](https://github.com/cappuccino/cappuccino "Cappuccino's GitHub Page"))

#### Working
- Non-Intelligent method call selector completion
- Variable name completion
- Variable, Function and Method declaration resolution
- Syntax Highlighting
- Invalid method call selector warnings

#### Note
* Variable type resolution is not accurately implemented which causes suggestion of any selector even if not applicable to variable

#### Future
- Add rename support for method selectors (removed original implementation due to problems with rename scope)
- Restrict suggestions to imported files
- Implement variable type resolution to provide better autocompletion suggestions
- Use framework decompiler to allow plugin to work without requiring source