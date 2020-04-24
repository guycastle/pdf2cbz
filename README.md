# PDF 2 CBZ
Small util to convert PDF comic books to CBZ format

## Build

Create an MSI installer with:

```bash
sbt windows:packageBin
```

Create a fat JAR which can be converted to an executable file with [Launch4J](http://launch4j.sourceforge.net):

```bash
sbt assembly
```

## Run

Run the executable from the command line. You have the following command line options:

* `-s`: Source directory (defaults to the current directory)
* `-d`: Delete the original PDF after a successful conversion (if omitted the file will not be deleted)
* `-r`: Recursively look for PDF files in subfolders