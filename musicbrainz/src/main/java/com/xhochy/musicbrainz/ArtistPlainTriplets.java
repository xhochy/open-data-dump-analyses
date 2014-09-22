package com.xhochy.musicbrainz;

import java.io.IOException;
import java.util.StringTokenizer;

import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;

public class ArtistPlainTriplets
{
    public static class TokenizerMapper extends Mapper<Object, Text, Text, IntWritable>{

        private final static IntWritable one = new IntWritable(1);
        private Text word = new Text();

        public void map(Object key, Text value, Context context) throws IOException, InterruptedException {
            String artistName = value.toString().split("\t")[2];
            StringTokenizer st = new StringTokenizer(artistName);
            while (st.hasMoreTokens()) {
                String token = st.nextToken().replaceAll("[^A-Za-z]", "").toLowerCase();
                if (token.length() <= 3) {
                    word.set(token);
                    context.write(word, one);
                } else {
                    for (int i = 0; i < token.length() - 3; i++) {
                        word.set(token.substring(i, i+3));
                        context.write(word, one);
                    }
                }
            }
        }
    }
}

