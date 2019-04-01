import picocli.CommandLine;

import java.security.MessageDigest;
import java.util.concurrent.Callable;
import picocli.CommandLine.*;

@Command(description = "Prints the checksum (MD5 by default) of a file to STDOUT.",
        name = "checksum", mixinStandardHelpOptions = true, version = "checksum 3.0")
class InteractiveOption implements Callable<Void> {

    @Option(names = {"-a", "--algorithm"}, description = "MD5, SHA-1, SHA-256, ...")
    private String algorithm;

    @Option(names = {"-o", "--only"}, description = "Only", interactive = true)
    private String only;

    public static void main(String[] args) throws Exception {
        CommandLine.call(new InteractiveOption(), args);
    }

    @Override
    public Void call() throws Exception {
        if (algorithm != null ){
            System.out.println(algorithm);
        }
        if(only != null){
            System.out.println(only);
        }
        return null;
    }
}