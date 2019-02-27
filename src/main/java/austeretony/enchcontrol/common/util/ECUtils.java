package austeretony.enchcontrol.common.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.io.Writer;
import java.util.List;
import java.util.TreeMap;

import org.apache.commons.io.IOUtils;

import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;

public class ECUtils {

    private final static TreeMap<Integer, String> ROMAN_NUMERALS = new TreeMap<Integer, String>();

    static {
        ROMAN_NUMERALS.put(1000, "M");
        ROMAN_NUMERALS.put(900, "CM");
        ROMAN_NUMERALS.put(500, "D");
        ROMAN_NUMERALS.put(400, "CD");
        ROMAN_NUMERALS.put(100, "C");
        ROMAN_NUMERALS.put(90, "XC");
        ROMAN_NUMERALS.put(50, "L");
        ROMAN_NUMERALS.put(40, "XL");
        ROMAN_NUMERALS.put(10, "X");
        ROMAN_NUMERALS.put(9, "IX");
        ROMAN_NUMERALS.put(5, "V");
        ROMAN_NUMERALS.put(4, "IV");
        ROMAN_NUMERALS.put(1, "I");
    }

    public static JsonElement getInternalJsonData(String path) throws IOException {	
        JsonElement rawData = null;		
        try (InputStream inputStream = ECUtils.class.getClassLoader().getResourceAsStream(path)) {				    	
            rawData = new JsonParser().parse(new InputStreamReader(inputStream, "UTF-8"));  
        }
        return rawData;
    }

    public static JsonElement getExternalJsonData(String path) throws IOException {		
        JsonElement rawData = null;		
        try (InputStream inputStream = new FileInputStream(new File(path))) {				    	
            rawData = new JsonParser().parse(new InputStreamReader(inputStream, "UTF-8"));  
        }		
        return rawData;
    }

    public static void createExternalJsonFile(String path, JsonElement data) throws IOException {		
        try (Writer writer = new FileWriter(path)) {    		    	        	
            new GsonBuilder().setPrettyPrinting().create().toJson(data, writer);
        }
    }

    public static void createAbsoluteJsonCopy(String path, InputStream source) throws IOException {                 
        List<String> fileData;       
        try (InputStream inputStream = source) {                                                               
            fileData = IOUtils.readLines(new InputStreamReader(inputStream, "UTF-8"));
        } 
        if (fileData != null) {              
            try (PrintStream printStream = new PrintStream(new File(path))) {                      
                for (String line : fileData)                
                    printStream.println(line);
            }
        }
    }

    public static String toRomanNumeral(int number) {
        int l = ROMAN_NUMERALS.floorKey(number);
        if (number == l)
            return ROMAN_NUMERALS.get(number);
        return ROMAN_NUMERALS.get(l) + toRomanNumeral(number - l);
    }
}
