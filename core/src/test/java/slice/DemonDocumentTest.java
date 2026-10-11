package slice;
import dev.DemonDocument;
import com.google.gson.*;
import java.nio.file.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import static org.junit.jupiter.api.Assertions.*;
class DemonDocumentTest {
 @TempDir Path directory;
 @Test void originalDefinitionsAndUnknownFieldsRoundTrip()throws Exception{
  DemonDocument editor=new DemonDocument(ContentBandTest.catalog());
  for(String name:new String[]{"JJJ","Ambassador","Sirabhorn"}){
   JsonObject original=ContentBandTest.catalog().object("demons/"+name+".JSON");
   original.add("futureAuthoredField",JsonParser.parseString("{\"script\":[1,\"keep me\"],\"optional\":null}"));
   Path path=directory.resolve(name+".json");editor.save(path,original.toString());assertEquals(original,editor.open(path));
  }
 }
 @Test void invalidReferenceCannotOverwriteValidFile()throws Exception{
  DemonDocument editor=new DemonDocument(ContentBandTest.catalog());JsonObject raw=ContentBandTest.catalog().object("demons/JJJ.JSON");Path path=directory.resolve("demon.json");editor.save(path,raw.toString());String before=Files.readString(path);raw.addProperty("basicAttack","does_not_exist");assertThrows(IllegalArgumentException.class,()->editor.save(path,raw.toString()));assertEquals(before,Files.readString(path));
 }
 @Test void learnLevelsAndDeclaredCapacityValidate(){DemonDocument editor=new DemonDocument(ContentBandTest.catalog());JsonObject raw=ContentBandTest.catalog().object("demons/JJJ.JSON");raw.addProperty("learnableCapacity",0);assertThrows(IllegalArgumentException.class,()->editor.parse(raw.toString(),"test"));raw.addProperty("learnableCapacity",1);raw.getAsJsonArray("learnableSkills").get(0).getAsJsonObject().addProperty("level",1);assertThrows(IllegalArgumentException.class,()->editor.parse(raw.toString(),"test"));}
 @Test void deferredMovesAreReportedButPreserved(){DemonDocument editor=new DemonDocument(ContentBandTest.catalog());JsonObject raw=ContentBandTest.catalog().object("demons/Sirabhorn.JSON");assertFalse(editor.runtimeDiagnostics(raw).isEmpty());assertEquals(raw,editor.parse(editor.format(raw),"Sirabhorn"));}
}
