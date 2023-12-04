# Test Case Data

The files in this folder are used to perform the integration tests. Each
test case resides in its own folder.

## Numbering Scheme

The folders are named `CaseXYZ_CCDesc` with

- X denoting the XSS vulnerability class

    - 1 = class A

    - 2 = class E

    - 3 = class H

    - 4 = class J

    - 5 = class S

    - 6 = class U

- Y denoting the sub-class minus 1 (i.e., 20x = A.1, 21x = A.2, ...)

- Z denoting the source

    - 0 = attribute

    - 1 = element

    - 2, ... = others

- CC is the class/sub-class designation without dot