package content;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
/** Desktop tool reader: configured assets root, project assets, working assets, then packaged resources. */
public final class ContentFiles {
    private ContentFiles() {}
    public static String read(String path){
        for(Path root:new Path[]{Path.of(System.getProperty("guise.assets","assets")),Path.of(".")}){
            Path file=root.resolve(path);if(Files.isRegularFile(file))try{return Files.readString(file);}catch(IOException e){throw new IllegalArgumentException(file.toString(),e);}
        }
        try(InputStream stream=ContentFiles.class.getClassLoader().getResourceAsStream(path)){
            if(stream==null)throw new IllegalArgumentException("Missing content "+path+"; set -Dguise.assets=/path/to/assets");
            return new String(stream.readAllBytes(),StandardCharsets.UTF_8);
        }catch(IOException e){throw new IllegalArgumentException(path,e);}
    }
}
