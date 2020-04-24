# PDF 2 CBZ
Small util to convert PDF comic books to CBZ format.

## Build

Create an MSI installer with (requires the [Wix Toolset](https://wixtoolset.org/) to be installed):

```bash
sbt windows:packageBin
```

Create a fat JAR which can be converted to an executable file with [Launch4J](http://launch4j.sourceforge.net):

```bash
sbt assembly
```

## Run

Run the executable from the command line. You have the following command line options:

* `-s`: Source directory (defaults to the current directory).
* `-d`: Delete the original PDF after a successful conversion (if omitted the file will not be deleted).
* `-r`: Recursively look for PDF files in subfolders.
* `-m`: Only extract images, not text. If your PDF contains only images, i.e. the text is not separate from the image, use this option for conversion as it is faster.
* `-i`: Specify the DPI of the converted images. Only applicable when the `-m` flag **is not** set.