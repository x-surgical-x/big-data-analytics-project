import java.io.IOException;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;

public class UCRCityCrimeDataRunner {
public static void main(String[] args) throws Exception {
        if (args.length != 2) {
                System.err.println("Usage: UCRCityCrimeDataRunner <input path> <output path>");
                System.exit(-1);
        }
        Job job = new Job();
        job.setJarByClass(UCRCityCrimeDataRunner.class);
        job.setJobName("UCR data ETL");

        job.setNumReduceTasks(1); // 1 Reduce task

        FileInputFormat.addInputPath(job, new Path(args[0]));
        FileOutputFormat.setOutputPath(job, new Path(args[1]));

        job.setMapperClass(UCRCityCrimeDataMapper.class);
        job.setReducerClass(UCRCityCrimeDataReducer.class);

        try{
            job.addFileToClassPath(new Path("/user/ssg441/lib/commons-csv-1.4.jar"));
        }catch(IOException e){
            //do nothing
        }

        job.setOutputKeyClass(Text.class);
        job.setOutputValueClass(Text.class);
        System.exit(job.waitForCompletion(true) ? 0 : 1);
    }
}
