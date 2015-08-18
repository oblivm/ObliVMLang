mkdir -p flexsc-bin;
mkdir -p to-run;
java -cp bin\;lib/* com.oblivm.compiler.cmd.Cmd -o ./flexsc-bin/ $1;
find flexsc-bin -name "*.java" > source.txt;
javac -cp to-run\;lib/* -d to-run @source.txt;
