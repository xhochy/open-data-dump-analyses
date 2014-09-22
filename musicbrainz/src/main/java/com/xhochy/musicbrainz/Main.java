package com.xhochy.musicbrainz;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;

public class Main {

    /**
     * Selector method to instantiate the correct job.
     */
    public static void main(String[] args) throws Exception {
        if (args.length == 0) {
            System.err.println("You need to specify at leat the job name!");
            System.exit(1);
        }

        Configuration conf = new Configuration();

        String jobname = args[0];
        Job job = Job.getInstance(conf, jobname);

        if (jobname.equals("artistnames")) {
            job.setJarByClass(ArtistNames.class);
            job.setMapperClass(ArtistNames.TokenizerMapper.class);
            job.setCombinerClass(ArtistNames.IntSumReducer.class);
            job.setReducerClass(ArtistNames.IntSumReducer.class);
            job.setOutputKeyClass(Text.class);
            job.setOutputValueClass(IntWritable.class);
        } else {
            System.err.println("Jobname not found!");
            System.exit(1);
        }

        FileInputFormat.addInputPath(job, new Path(args[1]));
        FileOutputFormat.setOutputPath(job, new Path(args[2]));
        System.exit(job.waitForCompletion(true) ? 0 : 1);
    }
}
