# image-size
get the image size without load the entire image to memory.the java implemetation of project https://github.com/image-size/image-size

## installation
##### build from source code
```
git clone https://github.com/NorthEndCodeBase/image-size.git
cd image-size
mvn clean install
```
##### add package form repository
```
<dependency>
    <groupId>com.github.northend</groupId>
    <artifactId>image-size</artifactId>
    <version>1.0</version>
</dependency>
```
## Usage
##### get the size of arbitrary image
```
Pair<Integer,Integer> size = Image.sizeOf(new File("source.jpg"));
System.out.println(size);
```
###### result output
```$xslt
width:913 height:740
```
##### get the type of arbitrary image
```$xslt
String imageType  = Image.typeOf(new File("source.jpg"));
System.out.println(imageType);
```
###### result output
```$xslt
JPG
```
### Supported Image Format
```$xslt
JPG
PNG
ICO
BMP
GIF
PSD
```
