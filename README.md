# Vocabulary

Build with mvn clean package

Distribution is available in target/appassembler. To run, execute vocabulary port dataFolder
- port is the HTTP port to listen to (typically 80 or 8080). If not specified, defaults to 80.
- dataFolder indicates the directory where the data is saved. If not specified, defaults to data. In this directory, the vocabulary list is stored in words.txt and will be created if it does not exist.

words.txt is a simple text file structured as a pipe-separated format:

 # Comments start with a dash<br>
 # The initial ! indicates language names<br>
 !French|English<br>
 Bonjour|Hello<br>
 Monde|World<br>
 <br>
 # Several possibilities may exist<br>
 Fort,Solide|Strong<br>
 Bleu|Blue,Rookie,Bruise<br>
 <br>
 # Additional remarks can be specified<br>
 Matraque,Association|Club|Different meaning for verb and noun<br>
