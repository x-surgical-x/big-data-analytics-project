import java.lang.Exception;
import java.io.IOException;
import java.lang.StringBuilder;
import org.apache.commons.csv.*;
import java.util.Iterator;

import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Mapper;

public class FBIDataETLMapper extends Mapper<LongWritable, Text, Text, Text> {
private static final int FBI_DATA_FIELDS = 24;
private static final String[] match_tokens = {" of " , "police", "county", "sheriff", "department", "public", "safety", "dept", "city", "agency", "office", "parish"};

@Override
public void map(LongWritable key, Text value, Context context) throws IOException, InterruptedException {
        String line = value.toString();
        CSVParser parser;
        CSVRecord record;
        try{
            parser = CSVParser.parse(line, CSVFormat.EXCEL);
            Iterator<CSVRecord> iterator = parser.iterator();
            if(iterator.hasNext())
                record = iterator.next();
            else{
                //No such element exception
                return;
            }
        }catch(IOException e){
            return;
        }
        String[] fields = line.split(",");
        String new_key = "", new_value = "";
        if(record.size()<=10){
            System.out.println(line);
            return;
        }
        if(record.size() <= FBI_DATA_FIELDS){
            new_value = "";
            String agency = record.get(0);
            String state = record.get(1).toUpperCase();
            if(state.equalsIgnoreCase("state")){
                return;
            }
            String[] agency_fields = agency.toLowerCase().split("\\s+");
            new_key = state;
            outerloop:
            for(String agency_field: agency_fields){
                for(String match_token: match_tokens){
                    if(agency_field.equalsIgnoreCase(match_token)){
                        break outerloop;
                    }
                }
                new_key = new_key + "-" + agency_field;
            }
            StringBuilder sbStr = new StringBuilder();
            sbStr.append("data:"+agency.replace(","," "));
            for(int i=2; i<record.size(); i++){
                sbStr.append(",").append(record.get(i).replace(",",""));
            }
            new_value = sbStr.toString();
        }else{
            new_value = "county:";
            String delimiter = "$";
            String state = record.get(1).toUpperCase();
            String county = record.get(2).toLowerCase();
            String agency = record.get(3).toLowerCase();
            String population = delimiter + record.get(35);
            if(state.equalsIgnoreCase("state")){
                return;
            }
            String[] agency_fields = agency.toLowerCase().split("\\s+");
            new_key = state;
            outerloop:
            for(String agency_field: agency_fields){
                for(String match_token: match_tokens){
                    if(agency_field.equalsIgnoreCase(match_token)){
                        break outerloop;
                    }
                }
                new_key = new_key + "-" + agency_field;
            }
            new_value += county;
            new_value += population;
        }
        context.write(new Text(new_key), new Text(new_value));
    }
}
