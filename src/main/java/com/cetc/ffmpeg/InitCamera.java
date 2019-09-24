package com.cetc.ffmpeg;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

@Component
public class InitCamera implements CommandLineRunner{

    private static Logger logger  = LoggerFactory.getLogger(InitCamera.class);

    @Override
    public void run(String... args) throws Exception {
        String shell = "ffmpeg -rtsp_transport tcp -i 'rtsp://admin:admin1234@192.168.112.252:554/cam/realmonitor?channel=1&subtype=0' -stimeout '3000000' -vcodec copy -acodec copy -f flv -y 'rtmp://localhost:1935/hls/test'";
        String[] cmd = new String[] {"sh", "-c", shell};
        ThreadLocal<String[]> threadLocal = new ThreadLocal<>();
        new Thread(() -> {
            threadLocal.set(cmd);
            while (true) {
                try {
                    Process process = Runtime.getRuntime().exec(threadLocal.get());
                    new Thread(() -> {
                        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(process.getErrorStream()));
                        String str;
                        try {
                            logger.info("start");
                            while ((str = bufferedReader.readLine()) != null) {
                                logger.info(str);
                            }
                            logger.info("exit");
                            process.exitValue();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }).start();
                    process.waitFor();
                    logger.info("ffmpeg restart");
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }
}
