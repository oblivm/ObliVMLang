mkdir -p bin
find java/ -name "*.java" > source.txt;
javac -cp bin:lib/* -d bin @source.txt;
