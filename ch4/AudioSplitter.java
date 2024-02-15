public class AudioSplitter {
	public static void main(String[] args) {
    	String inputFilePath = "path/to/file/sample.mp3";
    	String outputDirectory = "path/to/folder/";
    	int segmentDurationInSeconds = 600; // 10 minutes in seconds

    	try (FFmpegFrameGrabber grabber = new FFmpegFrameGrabber(inputFilePath)) {
        	grabber.start();

        	long totalDurationInSeconds = (long) grabber.getLengthInTime() / 1000000; // Convert microseconds to seconds
        	double frameRate = grabber.getFrameRate();

        	long segmentStartTime = 0;
        	long segmentEndTime;
        	int segmentNumber = 1;

        	while (segmentStartTime < totalDurationInSeconds) {
            	String outputFilePath = outputDirectory + "segment_" + segmentNumber + ".mp3";

            	try (FFmpegFrameRecorder recorder = new FFmpegFrameRecorder(outputFilePath, 0)) {
                	recorder.setAudioChannels(2);
                	recorder.setAudioCodecName("libmp3lame"); // Set the audio codec to MP3
                	recorder.setAudioBitrate(192000); // Adjust bitrate as needed
                	recorder.setSampleRate(44100); // Adjust sample rate as needed
                	recorder.setFrameRate(frameRate);
                	recorder.setFormat("mp3"); // Set the output format to MP3
                	recorder.start();

                	segmentEndTime = Math.min(segmentStartTime + segmentDurationInSeconds, totalDurationInSeconds);

                	grabber.setTimestamp(segmentStartTime * 1000000); // Set the grabber's timestamp to the start time in microseconds

                	while (grabber.getTimestamp() / 1000000 < segmentEndTime) {
                    	recorder.record(grabber.grabSamples());
                	}
            	}

            	segmentStartTime = segmentEndTime;
            	segmentNumber++;
        	}
    	} catch (IOException e) {
        	e.printStackTrace();
    	}
	}
}
